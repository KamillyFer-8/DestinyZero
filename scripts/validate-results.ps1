param(
    [string]$ResultsPath = 'results',
    [string]$ReproductionPath = ''
)
$ErrorActionPreference = 'Stop'
$culture = [System.Globalization.CultureInfo]::InvariantCulture
function Number($value) { return [double]::Parse($value, $culture) }
function Check($condition, $message) { if (-not $condition) { throw $message } }
function Close($actual, $expected, $label) {
    Check ([double]::IsFinite($actual)) "Nonfinite $label"
    Check ([Math]::Abs($actual - $expected) -le (1e-9 * [Math]::Max(1, [Math]::Abs($expected)))) "Mismatch: $label"
}
function Average($values) { return ($values | Measure-Object -Average).Average }
function Median($values) {
    $sorted = @($values | Sort-Object)
    $middle = [int][Math]::Floor($sorted.Count / 2)
    if ($sorted.Count % 2 -eq 1) { return $sorted[$middle] }
    return ($sorted[$middle - 1] + $sorted[$middle]) / 2
}
function FormatNumber($value) { return (Number $value).ToString('F4', $culture) }

$raw = @(Import-Csv -LiteralPath (Join-Path $ResultsPath 'raw-results.csv'))
$summary = @(Import-Csv -LiteralPath (Join-Path $ResultsPath 'summary.csv'))
$report = Get-Content -LiteralPath (Join-Path $ResultsPath 'experiment-report.md') -Raw -Encoding utf8
$strategies = @('ORIGINAL_ORDER', 'NEAREST_NEIGHBOR', 'PRIORITY')
$sizes = @(10, 25, 50, 100, 250)
Check ($raw.Count -eq 300) 'Expected 300 raw rows'
Check ($summary.Count -eq 18) 'Expected 18 summary rows'
Check (-not (Test-Path -LiteralPath (Join-Path $ResultsPath 'failure.txt'))) 'Failure artifact exists'
Check ($report.Contains('Scenarios analyzed: 100') -and $report.Contains('Route executions: 300')) 'Report counts'
Check ($report.Contains('Warm-up calls per strategy/scenario: 5; timed repetitions: 11.')) 'Report timing settings'

foreach ($size in $sizes) {
    foreach ($seed in 1..20) {
        $id = "n$size-seed$seed"
        $rows = @($raw | Where-Object scenario_id -eq $id)
        Check ($rows.Count -eq 3) "Scenario count: $id"
        $baselineRows = @($rows | Where-Object strategy -eq 'ORIGINAL_ORDER')
        Check ($baselineRows.Count -eq 1) "Baseline count: $id"
        $baseline = Number $baselineRows[0].total_distance
        $highCount = [int]$baselineRows[0].high_priority_count
        foreach ($strategy in $strategies) {
            $matches = @($rows | Where-Object strategy -eq $strategy)
            Check ($matches.Count -eq 1) "Strategy count: $id/$strategy"
            $row = $matches[0]
            Check ([int]$row.seed -eq $seed -and [int]$row.dataset_size -eq $size) "Metadata: $id"
            Check ((Number $row.total_distance) -ge 0 -and (Number $row.execution_time_ns) -ge 0) "Negative metric: $id"
            Close (Number $row.difference_from_baseline) ((Number $row.total_distance) - $baseline) "$id difference"
            if ($baseline -eq 0) { Check ($row.percentage_difference -eq '') "$id undefined percentage" }
            else { Close (Number $row.percentage_difference) (100 * ((Number $row.total_distance) - $baseline) / $baseline) "$id percentage" }
            Check ([int]$row.high_priority_count -eq $highCount -and $highCount -ge 0 -and $highCount -le $size) "$id HIGH count"
            if ($highCount -eq 0) {
                Check ($row.average_high_priority_position -eq '' -and $row.high_priority_first_third_percentage -eq '') "$id absent priority"
            } else {
                $position = Number $row.average_high_priority_position
                $third = Number $row.high_priority_first_third_percentage
                Check ($position -ge 1 -and $position -le $size -and $third -ge 0 -and $third -le 100) "$id priority ranges"
                if ($strategy -eq 'PRIORITY') {
                    Close $position (($highCount + 1) / 2) "$id priority position"
                    Close $third (100 * [Math]::Min($highCount, [Math]::Ceiling($size / 3)) / $highCount) "$id priority first third"
                }
            }
        }
    }
}

foreach ($strategy in $strategies) {
    foreach ($size in @('ALL', '10', '25', '50', '100', '250')) {
        $matches = @($summary | Where-Object { $_.strategy -eq $strategy -and $_.dataset_size -eq $size })
        Check ($matches.Count -eq 1) "Summary key: $strategy/$size"
        $s = $matches[0]
        $rows = @($raw | Where-Object { $_.strategy -eq $strategy -and ($size -eq 'ALL' -or $_.dataset_size -eq $size) })
        Check ([int]$s.scenario_count -eq $rows.Count) "Summary count: $strategy/$size"
        $distances = @($rows | ForEach-Object { Number $_.total_distance })
        $times = @($rows | ForEach-Object { Number $_.execution_time_ns })
        $differences = @($rows | ForEach-Object { Number $_.difference_from_baseline })
        Close (Number $s.mean_distance) (Average $distances) 'mean distance'
        Close (Number $s.median_distance) (Median $distances) 'median distance'
        Close (Number $s.min_distance) (($distances | Measure-Object -Minimum).Minimum) 'min distance'
        Close (Number $s.max_distance) (($distances | Measure-Object -Maximum).Maximum) 'max distance'
        Close (Number $s.mean_difference) (Average $differences) 'mean difference'
        Close (Number $s.mean_execution_time_ns) (Average $times) 'mean time'
        Close (Number $s.median_execution_time_ns) (Median $times) 'median time'
        $better = 0; $equal = 0; $worse = 0
        foreach ($row in $rows) {
            $difference = Number $row.difference_from_baseline
            $baseline = (Number $row.total_distance) - $difference
            if ([Math]::Abs($difference) -le 1e-9 * [Math]::Max(1, [Math]::Abs($baseline))) { $equal++ }
            elseif ($difference -lt 0) { $better++ } else { $worse++ }
        }
        Check ([int]$s.better -eq $better -and [int]$s.equal -eq $equal -and [int]$s.worse -eq $worse) 'Outcome counts'
        Close (Number $s.win_percentage) (100 * $better / $rows.Count) 'win percentage'
        foreach ($mapping in @(
            @('percentage_difference', 'mean_percentage_difference', 'percentage_scenario_count'),
            @('average_high_priority_position', 'mean_high_priority_position', 'priority_scenario_count'),
            @('high_priority_first_third_percentage', 'mean_high_priority_first_third_percentage', 'priority_scenario_count')
        )) {
            $values = @($rows | Where-Object { $_.($mapping[0]) -ne '' } | ForEach-Object { Number $_.($mapping[0]) })
            Check ([int]$s.($mapping[2]) -eq $values.Count) 'Valid metric count'
            if ($values.Count -eq 0) { Check ($s.($mapping[1]) -eq '') 'Missing aggregate' }
            else { Close (Number $s.($mapping[1])) (Average $values) $mapping[1] }
        }
        $distanceLine = '| ' + $strategy + ' | ' + $size + ' | ' + $s.scenario_count + ' | ' +
            ((@('mean_distance', 'median_distance', 'min_distance', 'max_distance', 'mean_difference', 'mean_percentage_difference') |
                ForEach-Object { FormatNumber $s.$_ }) -join ' | ') + ' | ' + $s.better + ' / ' + $s.equal + ' / ' + $s.worse + ' | ' + (FormatNumber $s.win_percentage) + ' |'
        Check ($report.Contains($distanceLine)) "Report distance row: $strategy/$size"
        $priorityLine = '| ' + $strategy + ' | ' + $size + ' | ' + (FormatNumber $s.mean_execution_time_ns) + ' | ' +
            (FormatNumber $s.median_execution_time_ns) + ' | ' + $s.priority_scenario_count + ' | ' +
            (FormatNumber $s.mean_high_priority_position) + ' | ' + (FormatNumber $s.mean_high_priority_first_third_percentage) + ' |'
        Check ($report.Contains($priorityLine)) "Report priority row: $strategy/$size"
    }
}

if ($ReproductionPath -ne '') {
    $repeat = @(Import-Csv -LiteralPath (Join-Path $ReproductionPath 'raw-results.csv'))
    Check ($repeat.Count -eq $raw.Count) 'Reproduction row count'
    $columns = @($raw[0].PSObject.Properties.Name | Where-Object { $_ -ne 'execution_time_ns' })
    for ($i = 0; $i -lt $raw.Count; $i++) {
        foreach ($column in $columns) { Check ($raw[$i].$column -eq $repeat[$i].$column) "Reproduction: row $i/$column" }
    }
}
Write-Output 'PASS: 100 scenarios, 300 raw rows, 18 summary rows; all aggregates and report tables validated.'
if ($ReproductionPath -ne '') { Write-Output 'PASS: every non-timing raw field matches the independent Java reproduction.' }

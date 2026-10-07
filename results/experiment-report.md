# DestinyZero — Experiment Report

Scenarios analyzed: 100

Route executions: 300

Dataset sizes: [10, 25, 50, 100, 250]

Seeds: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20]

Generated deterministically by Java from all configured scenarios.

## Methodology

Uniform integer coordinates [0, 1000] and equally probable LOW/NORMAL/HIGH categories. java.util.Random(seed); draw order per delivery: x, y, priority. Origin (0, 0), open route, Euclidean distance in simulation units. Same input for every strategy. Warm-up calls per strategy/scenario: 5; timed repetitions: 11. Each raw timing is the median of planning calls, including internal copies and validation. Even-count timing medians are truncated to whole nanoseconds. Dataset generation, output validation, metric calculation and file I/O are excluded. Strategies run in registered order. JVM, GC and order effects remain; this is not a scientific benchmark.

Runtime: Java 21.0.12.1; VM: Java HotSpot(TM) 64-Bit Server VM; OS: Windows 11 amd64; processors available: 8.

Difference = strategy minus baseline; negative means shorter. Mean percentage is the mean of paired scenario percentages, not a percentage of pooled means. Undefined percentages (zero baseline) are excluded, with valid counts in summary.csv. Ties use abs(difference) <= 1e-9 * max(1, abs(baseline)).

HIGH positions start at 1; first third uses ceil(n/3). Priority means give each scenario with HIGH deliveries equal weight; scenarios without HIGH are excluded, with valid counts in summary.csv. ALL pools sizes equally by scenario and must be read alongside size-specific results.

## Distance by strategy and dataset size

| Strategy | Size | Scenarios | Mean | Median | Min | Max | Mean difference | Mean difference % | Better / Equal / Worse | Win % |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|---:|
| ORIGINAL_ORDER | ALL | 100 | 45264.6106 | 25655.3444 | 3019.6292 | 139176.0634 | 0.0000 | 0.0000 | 0 / 100 / 0 | 0.0000 |
| ORIGINAL_ORDER | 10 | 20 | 5376.9149 | 5551.5609 | 3019.6292 | 6477.3971 | 0.0000 | 0.0000 | 0 / 20 / 0 | 0.0000 |
| ORIGINAL_ORDER | 25 | 20 | 12801.1380 | 12921.4547 | 9264.7982 | 15228.5535 | 0.0000 | 0.0000 | 0 / 20 / 0 | 0.0000 |
| ORIGINAL_ORDER | 50 | 20 | 25796.4419 | 25655.3444 | 22553.5700 | 28987.0448 | 0.0000 | 0.0000 | 0 / 20 / 0 | 0.0000 |
| ORIGINAL_ORDER | 100 | 20 | 51720.4582 | 51892.5824 | 48837.5333 | 57935.2768 | 0.0000 | 0.0000 | 0 / 20 / 0 | 0.0000 |
| ORIGINAL_ORDER | 250 | 20 | 130628.0998 | 130529.7163 | 121871.0730 | 139176.0634 | 0.0000 | 0.0000 | 0 / 20 / 0 | 0.0000 |
| NEAREST_NEIGHBOR | ALL | 100 | 7410.7071 | 6405.9123 | 1815.4575 | 15794.3932 | -37853.9035 | -72.2050 | 100 / 0 / 0 | 100.0000 |
| NEAREST_NEIGHBOR | 10 | 20 | 2627.9318 | 2673.8244 | 1815.4575 | 3084.5018 | -2748.9830 | -50.3053 | 20 / 0 / 0 | 100.0000 |
| NEAREST_NEIGHBOR | 25 | 20 | 4479.5312 | 4487.7610 | 3825.8786 | 5063.2434 | -8321.6068 | -64.6148 | 20 / 0 / 0 | 100.0000 |
| NEAREST_NEIGHBOR | 50 | 20 | 6486.5709 | 6405.9123 | 5572.8630 | 7808.9284 | -19309.8711 | -74.7059 | 20 / 0 / 0 | 100.0000 |
| NEAREST_NEIGHBOR | 100 | 20 | 9071.7044 | 8956.4234 | 8401.4988 | 10367.9702 | -42648.7538 | -82.4216 | 20 / 0 / 0 | 100.0000 |
| NEAREST_NEIGHBOR | 250 | 20 | 14387.7971 | 14297.7977 | 13451.0097 | 15794.3932 | -116240.3027 | -88.9776 | 20 / 0 / 0 | 100.0000 |
| PRIORITY | ALL | 100 | 45483.4144 | 26214.3316 | 3496.2148 | 140402.8244 | 218.8038 | 0.8482 | 37 / 0 / 63 | 37.0000 |
| PRIORITY | 10 | 20 | 5314.7040 | 5235.1914 | 3496.2148 | 7122.8066 | -62.2109 | 0.1840 | 9 / 0 / 11 | 45.0000 |
| PRIORITY | 25 | 20 | 12972.5003 | 12738.7929 | 10505.7719 | 15598.8208 | 171.3623 | 1.9738 | 5 / 0 / 15 | 25.0000 |
| PRIORITY | 50 | 20 | 26006.9430 | 26214.3316 | 22673.0485 | 30259.3183 | 210.5010 | 1.1439 | 8 / 0 / 12 | 40.0000 |
| PRIORITY | 100 | 20 | 51883.0765 | 50685.7181 | 46679.8437 | 56646.4959 | 162.6183 | 0.4383 | 6 / 0 / 14 | 30.0000 |
| PRIORITY | 250 | 20 | 131239.8480 | 130858.4853 | 123802.4832 | 140402.8244 | 611.7482 | 0.5011 | 9 / 0 / 11 | 45.0000 |

## Performance and priority

| Strategy | Size | Mean time ns | Median time ns | Scenarios with HIGH | Mean HIGH position | Mean HIGH in first third % |
|---|---:|---:|---:|---:|---:|---:|
| ORIGINAL_ORDER | ALL | 2983.0000 | 2450.0000 | 100 | 44.1797 | 34.5275 |
| ORIGINAL_ORDER | 10 | 1445.0000 | 950.0000 | 20 | 6.0464 | 35.1429 |
| ORIGINAL_ORDER | 25 | 1980.0000 | 2200.0000 | 20 | 12.5585 | 37.7429 |
| ORIGINAL_ORDER | 50 | 2620.0000 | 2600.0000 | 20 | 25.7423 | 33.5511 |
| ORIGINAL_ORDER | 100 | 2680.0000 | 2300.0000 | 20 | 50.6065 | 33.5243 |
| ORIGINAL_ORDER | 250 | 6190.0000 | 4750.0000 | 20 | 125.9447 | 32.6762 |
| NEAREST_NEIGHBOR | ALL | 140946.0000 | 40750.0000 | 100 | 44.6300 | 32.2203 |
| NEAREST_NEIGHBOR | 10 | 14800.0000 | 11100.0000 | 20 | 5.7949 | 29.2262 |
| NEAREST_NEIGHBOR | 25 | 13105.0000 | 15000.0000 | 20 | 13.5462 | 32.6861 |
| NEAREST_NEIGHBOR | 50 | 35900.0000 | 39450.0000 | 20 | 25.4722 | 32.8217 |
| NEAREST_NEIGHBOR | 100 | 95030.0000 | 65300.0000 | 20 | 50.7594 | 33.8075 |
| NEAREST_NEIGHBOR | 250 | 545895.0000 | 522500.0000 | 20 | 127.5775 | 32.5598 |
| PRIORITY | ALL | 16951.0000 | 13300.0000 | 100 | 15.3300 | 94.8420 |
| PRIORITY | 10 | 9735.0000 | 8750.0000 | 20 | 2.3250 | 93.8571 |
| PRIORITY | 25 | 8220.0000 | 7800.0000 | 20 | 4.6750 | 94.7667 |
| PRIORITY | 50 | 15425.0000 | 14400.0000 | 20 | 9.0000 | 93.3330 |
| PRIORITY | 100 | 16580.0000 | 13500.0000 | 20 | 17.4000 | 95.7110 |
| PRIORITY | 250 | 34795.0000 | 23150.0000 | 20 | 43.2500 | 96.5422 |

## Limitations

Nearest Neighbor is a greedy heuristic without global optimality guarantees. Priority advances urgent deliveries; shortest distance is not its only goal. Fictional coordinates omit streets, traffic and road restrictions. Results do not establish real logistics savings. Routes and priority metrics reproduce with the same seeds/configuration; timings vary by hardware and environment.

## Evidence

[Individual executions](raw-results.csv) → [Java aggregates](summary.csv) → this deterministic report. All metrics and tables are generated by the Java application.

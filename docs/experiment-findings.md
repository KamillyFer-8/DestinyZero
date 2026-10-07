# Achados determinísticos — Fase 5

Experimento final executado em 7 de outubro de 2026: 100 cenários, 300 execuções lógicas, tamanhos 10/25/50/100/250 e seeds 1 a 20. Aquecimento: 5 chamadas; medição: 11 repetições. Todos os cenários foram mantidos. Estes são resultados de simulações controladas, não evidência de economia logística real.

Valores abaixo extraídos de results/summary.csv, grupo ALL. Diferença percentual é a média das porcentagens pareadas por cenário. Distâncias em unidades fictícias.

| Estratégia | Distância média | Distância mediana | Diferença média % | Menor / Igual / Maior que baseline | Posição média HIGH | HIGH no primeiro terço % |
|---|---:|---:|---:|---|---:|---:|
| ORIGINAL_ORDER | 45264.61 | 25655.34 | 0.00 | 0 / 100 / 0 | 44.18 | 34.53 |
| NEAREST_NEIGHBOR | 7410.71 | 6405.91 | -72.21 | 100 / 0 / 0 | 44.63 | 32.22 |
| PRIORITY | 45483.41 | 26214.33 | 0.85 | 37 / 0 / 63 | 15.33 | 94.84 |

## Por tamanho

| Entregas | Diferença média NN % | Diferença média Priority % | Mediana baseline ns | Mediana NN ns | Mediana Priority ns |
|---:|---:|---:|---:|---:|---:|
| 10 | -50.31 | 0.18 | 950.00 | 11100.00 | 8750.00 |
| 25 | -64.61 | 1.97 | 2200.00 | 15000.00 | 7800.00 |
| 50 | -74.71 | 1.14 | 2600.00 | 39450.00 | 14400.00 |
| 100 | -82.42 | 0.44 | 2300.00 | 65300.00 | 13500.00 |
| 250 | -88.98 | 0.50 | 4750.00 | 522500.00 | 23150.00 |

## Interpretação baseada nos dados

- Nearest Neighbor reduziu distância em todos os cenários desta configuração. A redução percentual média cresceu com o tamanho do dataset. Isso não prova ótimo global nem garante vitória em outras entradas; o teste unitário mantém um contraexemplo.
- Priority antecipou HIGH: sua posição média diminuiu e sua presença no primeiro terço aumentou em relação à baseline. A distância foi menor em alguns cenários e maior em outros; comparar somente distância ignoraria o objetivo dessa estratégia.
- Priority não colocou necessariamente todas as HIGH no primeiro terço: quando há mais HIGH do que posições disponíveis nesse trecho, o percentual fica abaixo de 100 mesmo com todas as HIGH agrupadas no começo.
- No grupo de 10 entregas, Priority teve diferença absoluta média negativa, mas diferença percentual média positiva. Isso é possível porque cada percentual é dividido pela baseline do próprio cenário; a média dos percentuais não é a porcentagem da diferença entre médias.
- Nos maiores datasets, o custo medido de Nearest Neighbor aumentou substancialmente. Tempos não foram estritamente crescentes em todos os grupos: a média de NN com 25 entregas ficou abaixo da de 10. Aquecimento, JIT, GC e ruído impedem interpretar esta execução como benchmark rigoroso ou como prova empírica de complexidade.
- Os grupos ALL misturam tamanhos diferentes; posições e distâncias gerais devem ser lidas junto com os resultados por tamanho. Seeds repetidas entre tamanhos compartilham prefixos, sem alegação de independência estatística.

## Validação e cadeia de evidências

scripts/validate-results.ps1 verificou todos os pares tamanho/seed/estratégia, diferenças pareadas, faixas e fórmulas da estratégia Priority, contagens de métricas válidas e todas as métricas agregadas. As tabelas do relatório foram conferidas contra summary.csv. A validação independente recalcula valores para conferência; a fonte dos resultados publicados continua sendo o Java.

Uma segunda execução Java com a configuração idêntica, em target/phase5-reproduction, produziu exatamente os mesmos campos brutos não relacionados a tempo. Tempos podem variar. Os resultados finais originais em results/ foram preservados; a reprodução é temporária e ignorada pelo Git.

Comandos executados:

```powershell
java -cp target/classes io.github.destinyzero.DestinyZero --full results
java -cp target/classes io.github.destinyzero.DestinyZero --full target/phase5-reproduction
& ./scripts/validate-results.ps1 -ResultsPath results -ReproductionPath target/phase5-reproduction
```

Para repetir, compile primeiro com mvn test e use outra pasta nova. Os três arquivos gerados pelo Java são results/raw-results.csv, results/summary.csv e results/experiment-report.md. Este documento contém comentários técnicos fundamentados nas métricas Java. O relatório da aplicação é produzido deterministicamente.

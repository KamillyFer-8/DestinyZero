# DestinyZero

Delivery Route Strategy Simulator

DestinyZero é uma aplicação Java que simula e compara estratégias de organização de entregas usando os mesmos dados. Aplica orientação a objetos e o Strategy Pattern, separa os algoritmos dos cálculos de métricas e salva os resultados em CSV e Markdown.

Projeto desenvolvido por Kamilly Araújo como parte do Bootcamp Java com IA, promovido pelo Itaú em parceria com a DIO, com foco na aplicação prática de Java, orientação a objetos, padrões de projeto, testes e desenvolvimento de software.

O DestinyZero expande o desafio com uma simulação experimental de estratégias de organização de entregas. Esta implementação é de minha autoria, Kamilly Araújo e não possui afiliação comercial nem endosso do Itaú ou da DIO.

## Objetivo

**Como diferentes estratégias de organização de entregas afetam a distância total da rota e o custo computacional da execução?**

A comparação também considera a antecipação de entregas prioritárias, permitindo observar o trade-off entre reduzir distância e atender prioridades.

## Tecnologias

- Java 21: records, interfaces, coleções e implementação dos algoritmos.
- Maven: compilação e execução dos testes.
- JUnit 5: testes unitários e integração da execução com exportação.
- Git: versionamento do projeto.

A aplicação não possui dependências externas em runtime. No pom.xml, JUnit Jupiter 5.11.4 é dependência somente de testes.

## Como funciona

Para cada tamanho e seed configurados, a aplicação gera um cenário e executa todas as estratégias sobre a mesma entrada imutável. Depois, calcula as métricas, consolida os resultados e grava os arquivos. O fluxo abaixo mostra como essas responsabilidades se organizam:

```text
ExperimentConfig → ExperimentRunner
                         ↓
                 DatasetGenerator → Scenario
                         ↓
               RouteComparisonService
                         ↓
                   RouteStrategy
                  ↙      ↓       ↘
           Original    Nearest   Priority
                         ↓
               ComparisonReport / RouteResult
                         ↓
                 ResultAggregator
                         ↓
                   ResultsWriter
                         ↓
         raw-results.csv / summary.csv / experiment-report.md
```

O cálculo de distância pertence a DistanceCalculator; as métricas de prioridade pertencem a PriorityMetricsCalculator. Assim, os algoritmos ordenam entregas e os serviços calculam e apresentam seus resultados.

## Strategy Pattern

RouteStrategy oferece o contrato name() e plan(origin, deliveries). Cada implementação deve preservar origem e todas as entregas, sem modificar a entrada. O serviço de comparação verifica esse contrato antes de aceitar as métricas.

O Strategy Pattern mantém cada algoritmo em sua própria classe, sob uma interface comum. Para adicionar uma estratégia, basta implementar o contrato e registrá-la na aplicação, sem modificar o motor de comparação. Essa organização exige que todas as implementações respeitem o mesmo contrato.

| Implementação | Regra | Tempo | Espaço auxiliar |
|---|---|---|---|
| OriginalOrderStrategy | Preserva a sequência recebida; baseline de distância | O(n), incluindo cópia e validação | O(n) |
| NearestNeighborStrategy | A partir da posição atual, seleciona o destino restante mais próximo; empates exatos seguem a ordem da entrada | O(n²) | O(n) |
| PriorityStrategy | Ordena HIGH, NORMAL e LOW, preservando a ordem original dentro de cada categoria | O(n log n) | O(n) |

Nearest Neighbor é uma heurística e não garante a solução ótima global. Priority busca antecipar entregas HIGH; minimizar exclusivamente distância não é seu objetivo.

## Modelagem e distância

Location representa um ponto com identificador, nome e coordenadas finitas. Delivery associa um destino à categoria Priority. Route contém origem e sequência imutável de entregas. RouteResult reúne rota, distância, tempo e métricas da comparação. Os records mantêm dados imutáveis; os serviços encapsulam os cálculos.

IDs vazios, valores obrigatórios ausentes e IDs de entrega duplicados são rejeitados. Entregas diferentes podem compartilhar coordenadas. Uma rota vazia tem distância zero; uma entrega inclui somente o trecho da origem até o destino.

A simulação utiliza coordenadas cartesianas fictícias e distância euclidiana, calculada com Math.hypot:

```text
d = √((x₂ − x₁)² + (y₂ − y₁)²)
```

As rotas começam em (0, 0) e terminam na última entrega, sem retorno ao depósito. As unidades de distância são arbitrárias. Os pontos não são coordenadas geográficas, não representam ruas e não incluem trânsito nem tempo real de deslocamento. Distância euclidiana não equivale a distância rodoviária.

## Metodologia experimental

O experimento utiliza tamanhos 10, 25, 50, 100 e 250, com seeds 1 a 20 para cada tamanho:

```text
5 tamanhos × 20 cenários = 100 cenários
100 cenários × 3 estratégias = 300 execuções lógicas
```

DatasetGenerator usa java.util.Random(seed) e sorteia, nessa ordem, x, y e prioridade para cada entrega. Coordenadas são inteiros uniformes de 0 a 1000 inclusive; LOW, NORMAL e HIGH têm a mesma probabilidade. Cada cenário é gerado uma vez e todas as estratégias recebem a mesma entrada imutável.

As seeds e a configuração foram definidas antes da execução, e nenhum cenário foi excluído para favorecer uma estratégia. Com a mesma seed, os cenários maiores começam com as mesmas entregas dos menores. Por isso, esses resultados não são tratados como amostras independentes para inferência estatística.

### Medição de tempo

Por estratégia e cenário, são feitas 5 chamadas de aquecimento e 11 repetições medidas com System.nanoTime(). A mediana representa o tempo de cada execução lógica no CSV; repetições não geram linhas adicionais.

A medição inclui plan(), com cópia e validação internas. Geração do dataset, validação externa da saída, cálculo de métricas e escrita dos arquivos ficam fora desse intervalo. O summary agrega a média e a mediana dos tempos individuais, em nanossegundos.

Os tempos dependem do ambiente e podem variar, especialmente em operações pequenas. A ordem fixa das estratégias, o JIT e a coleta de lixo também podem afetar as medições. **Este experimento não é um benchmark científico.**

## Métricas

| Métrica | Interpretação |
|---|---|
| Distância total | Soma dos trechos na ordem produzida pela estratégia |
| Diferença absoluta com sinal | Distância da estratégia menos distância da baseline; negativa indica rota menor |
| Diferença percentual | 100 × diferença / distância da baseline |
| Estatísticas de distância | Média, mediana, mínimo e máximo por estratégia, no geral e por tamanho |
| Menor / igual / maior | Contagem de cenários conforme a comparação com a baseline; percentual de vitórias também é calculado |
| Posição média HIGH | Média das posições das entregas HIGH, começando em 1 |
| HIGH no primeiro terço | Percentual das HIGH nas primeiras ceil(n/3) posições |
| Tempo de execução | Mediana das repetições por cenário, depois agregada por grupo |

Baseline zero torna a diferença percentual ausente. Sem HIGH, as métricas de prioridade ficam ausentes. Campos ausentes são vazios nos CSVs e excluídos das médias, com contagens de valores válidos no summary.

A diferença percentual média é a média das porcentagens pareadas por cenário; não é a porcentagem da diferença entre médias gerais. Métricas agregadas de prioridade dão peso igual a cada cenário válido. Os grupos ALL misturam tamanhos e devem ser lidos junto com os resultados por tamanho.

Empates de distância usam abs(diferença) <= 1e-9 × max(1, abs(baseline)). Essa tolerância não modifica as escolhas de destino da heurística.

## Resultados experimentais

Os arquivos finais registram 100 cenários, 300 execuções e 18 grupos consolidados. As tabelas abaixo foram extraídas de results/summary.csv, com duas casas decimais somente na apresentação. Os CSVs preservam os valores completos.

### Comparação geral

Distâncias em unidades fictícias; diferença percentual média calculada por cenário.

| Estratégia | Distância média | Mediana | Diferença média % | Menor / Igual / Maior |
|---|---:|---:|---:|---|
| ORIGINAL_ORDER | 45264.61 | 25655.34 | 0.00 | 0 / 100 / 0 |
| NEAREST_NEIGHBOR | 7410.71 | 6405.91 | -72.21 | 100 / 0 / 0 |
| PRIORITY | 45483.41 | 26214.33 | 0.85 | 37 / 0 / 63 |

### Prioridade

| Estratégia | Posição média HIGH | HIGH no primeiro terço % | Cenários válidos |
|---|---:|---:|---:|
| ORIGINAL_ORDER | 44.18 | 34.53 | 100 |
| NEAREST_NEIGHBOR | 44.63 | 32.22 | 100 |
| PRIORITY | 15.33 | 94.84 | 100 |

### Distância por tamanho

| Entregas | Média Original Order | Média Nearest Neighbor | Média Priority | Diferença NN % | Diferença Priority % |
|---:|---:|---:|---:|---:|---:|
| 10 | 5376.91 | 2627.93 | 5314.70 | -50.31 | 0.18 |
| 25 | 12801.14 | 4479.53 | 12972.50 | -64.61 | 1.97 |
| 50 | 25796.44 | 6486.57 | 26006.94 | -74.71 | 1.14 |
| 100 | 51720.46 | 9071.70 | 51883.08 | -82.42 | 0.44 |
| 250 | 130628.10 | 14387.80 | 131239.85 | -88.98 | 0.50 |

### Tempo observado por tamanho

Medianas dos tempos individuais dos cenários, em nanossegundos, da execução registrada em results/. O ambiente registrado no relatório é Java 21.0.12.1, Windows 11 amd64 e oito processadores disponíveis à JVM.

| Entregas | Original Order ns | Nearest Neighbor ns | Priority ns |
|---:|---:|---:|---:|
| 10 | 950.00 | 11100.00 | 8750.00 |
| 25 | 2200.00 | 15000.00 | 7800.00 |
| 50 | 2600.00 | 39450.00 | 14400.00 |
| 100 | 2300.00 | 65300.00 | 13500.00 |
| 250 | 4750.00 | 522500.00 | 23150.00 |

## Principais observações

- Nearest Neighbor apresentou menor distância nos 100 cenários executados. A redução percentual média cresceu de 50,31% com 10 entregas para 88,98% com 250. Isso não demonstra ótimo global nem garante o mesmo resultado em outras entradas; a suíte inclui um teste com um contraexemplo.
- Priority reduziu a posição média das HIGH de 44,18 para 15,33 e elevou sua presença no primeiro terço de 34,53% para 94,84%. Sua distância foi menor em 37 cenários e maior em 63; o trade-off precisa ser avaliado conforme o objetivo.
- Mesmo agrupando todas as HIGH no início, Priority não garante que todas caibam no primeiro terço: o grupo pode conter mais entregas prioritárias do que posições nesse trecho.
- Para 10 entregas, Priority teve distância média ligeiramente menor, mas diferença percentual média positiva. Cada porcentagem usa a baseline do próprio cenário, então a média das porcentagens pode ter sinal diferente da diferença entre as médias de distância.
- Nearest Neighbor apresentou maior tempo medido nos maiores conjuntos de entregas. Porém, a média do tempo com 25 entregas ficou abaixo da média com 10; os tempos não mostram crescimento em todos os tamanhos nem comprovam a complexidade do algoritmo.

Essas observações se limitam à simulação executada. Não demonstram redução de custo ou de tempo de entrega em logística real.

## Arquivos de resultados

| Arquivo | Conteúdo |
|---|---|
| [raw-results.csv](results/raw-results.csv) | Uma linha por execução lógica: cenário, seed, tamanho, estratégia, distância, tempo, diferenças e prioridade |
| [summary.csv](results/summary.csv) | Estatísticas gerais e por tamanho, comparações com baseline, tempos e contagens de métricas válidas |
| [experiment-report.md](results/experiment-report.md) | Relatório determinístico gerado pelo Java com configuração, metodologia, ambiente, tabelas e limitações |

O relatório reúne todos os grupos, incluindo mínimos e máximos. Uma reprodução independente confirmou os dados brutos, exceto os tempos, que variam conforme o ambiente. Os arquivos finais originais foram preservados. O script scripts/validate-results.ps1 confere as agregações e as tabelas.

## Testes

A suíte possui **40 testes executados com sucesso**, sem falhas ou erros, em mvn test. Não há afirmação de percentual de cobertura.

Os testes verificam distância euclidiana, coordenadas negativas, rota vazia e entrega única; contratos e comportamento das três estratégias; desempates e preservação da entrada; baseline, diferenças percentuais e métricas ausentes; regras de prioridade; geração determinística; estatísticas, agregação, CSVs e relatório; rejeição de entradas e saídas inválidas; registro de falhas e contagem de chamadas medidas.

## Como executar

### Pré-requisitos

JDK 21 e Maven 3.9 ou superior no PATH. Git é necessário para clonar. A primeira compilação baixa plugins Maven e JUnit; testes e experimentos não acessam serviços externos.

### Clonar

Repositório: [KamillyFer-8/DestinyZero](https://github.com/KamillyFer-8/DestinyZero).

```powershell
git clone https://github.com/KamillyFer-8/DestinyZero.git DestinyZero
cd DestinyZero
```

### Testar e compilar

```powershell
mvn test
```

### Executar a demonstração

```powershell
java -cp target/classes io.github.destinyzero.DestinyZero --demo target/readme-demo
```

A demo executa 4 cenários, tamanhos 10 e 25, seeds 1 e 2, com 2 chamadas de aquecimento e 3 repetições por estratégia. Ela valida o uso da aplicação e não substitui o experimento final.

### Executar o experimento completo

```powershell
java -cp target/classes io.github.destinyzero.DestinyZero --full target/readme-experiment
```

A aplicação imprime o relatório e grava os três arquivos no diretório informado: target/readme-demo para a demo e target/readme-experiment para o experimento completo. Use uma pasta nova em cada execução; pastas com resultados anteriores são recusadas. Falhas interrompem o experimento e geram failure.txt. Os resultados finais estão em results/.

### Validar os resultados

No PowerShell, confira os arquivos finais e, opcionalmente, compare seus campos determinísticos com os da reprodução:

```powershell
& ./scripts/validate-results.ps1 -ResultsPath results
& ./scripts/validate-results.ps1 -ResultsPath results -ReproductionPath target/readme-experiment
```

Dados e métricas de rota são reproduzíveis com a mesma configuração; tempos variam por hardware e ambiente.

## Estrutura do projeto

```text
src/
├── main/java/io/github/destinyzero/
│   ├── DestinyZero.java
│   ├── domain/
│   ├── strategy/
│   ├── service/
│   ├── dataset/
│   ├── experiment/
│   └── report/
└── test/java/io/github/destinyzero/
    ├── domain/
    ├── strategy/
    ├── service/
    └── experiment/
results/
├── raw-results.csv
├── summary.csv
└── experiment-report.md
docs/
scripts/validate-results.ps1
pom.xml
README.md
.gitignore
```

## Limitações

O experimento usa coordenadas fictícias e distância euclidiana. Não considera ruas reais, trânsito, janelas de entrega, capacidade de veículos ou restrições rodoviárias. Uma distância menor na simulação não implica menor tempo de deslocamento real.

Nearest Neighbor é uma heurística e não garante a solução ótima global. Priority busca antecipar entregas prioritárias. Os tempos dependem do ambiente e não constituem um benchmark científico. As conclusões se limitam aos cenários e às condições desta simulação.

## Possíveis evoluções

Novas estratégias sob o mesmo contrato, modelos alternativos de distância, visualização de rotas e benchmarking dedicado com JMH são possibilidades futuras, não funcionalidades implementadas.

## Autoria

Kamilly Araújo

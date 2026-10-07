# Arquitetura do DestinyZero

## Escopo e fluxo

Aplicação Java 21 para comparar estratégias de roteamento, executar experimentos controlados e gerar métricas e relatórios deterministicamente.

Dataset → RouteStrategy → RouteComparisonService → RouteResult → ExperimentRunner
→ raw-results.csv → summary.csv → experiment-report.md.

## Domínio e cálculo

Records representam dados imutáveis: Location, Delivery, Route, PriorityMetrics, RouteResult e ComparisonReport. Priority define categorias explicitamente. A rota é aberta, com origem e entregas ordenadas; totalDistance é calculado por DistanceCalculator, evitando armazenar um total redundante no domínio.

Coordenadas finitas, identidade não vazia e unicidade das entregas são validadas. Entregas diferentes podem compartilhar destino. Listas são copiadas e imutáveis; as estratégias não modificam a entrada.

## Strategy

RouteStrategy define name() e plan(origin, deliveries). OriginalOrderStrategy mantém a ordem, NearestNeighborStrategy escolhe o vizinho mais próximo a partir da posição atual, e PriorityStrategy ordena HIGH/NORMAL/LOW de forma estável. Empates exatos de distância mantêm a ordem da entrada.

Sem Strategy, algoritmos poderiam virar condicionais no motor. A interface separa comportamento e permite registrar novas implementações sem modificar o motor. O custo é manter contratos consistentes entre classes. Uma lista explícita resolve a composição; Factory não se justifica atualmente.

Nearest Neighbor custa O(n²), é guloso e não garante ótimo global. A baseline custa O(n), incluindo cópia e validação. Priority custa O(n log n). Todos usam espaço auxiliar O(n). O teste com origem 0 e destinos [-2, 1, 3] demonstra rota gulosa de 8 unidades contra 7 da baseline.

## Comparação

RouteComparisonService inclui a baseline automaticamente e aplica estratégias alternativas à mesma entrada imutável. Valida origem e todas as entregas na saída. Tempo mede somente plan(), incluindo validações internas, após aquecimento e com repetições; o resultado bruto guarda a mediana.

Distância, diferenças com sinal e percentual são calculadas pelo Java. PriorityMetricsCalculator calcula posição média de HIGH e presença no primeiro terço. Métricas indefinidas usam OptionalDouble e são exportadas como campos vazios. Definições, pesos e tolerâncias estão no README e no relatório.

## Experimentos e persistência

DatasetGenerator usa Random(seed), com ordem fixa x/y/prioridade e coordenadas inteiras [0,1000]. Scenario mantém a entrada imutável. ExperimentConfig fixa tamanhos, seeds e parâmetros de tempo e rejeita duplicações.

ExperimentRunner percorre a configuração e reutiliza RouteComparisonService, sem duplicar algoritmos. ResultAggregator calcula Statistics e SummaryRow em grupos ALL e por tamanho. ResultsWriter usa exclusivamente esses resultados para CSVs e Markdown. A CLI oferece --demo e --full com diretório explícito.

Pastas com artefatos antigos são recusadas. Falhas são registradas em failure.txt e interrompem a execução. Arquivos são escritos com CREATE_NEW. Uma falha na exportação não representa experimento válido.

## Evidências existentes

O experimento final executou 100 cenários e 300 execuções lógicas, com 18 linhas consolidadas. Seeds 1 a 20 para tamanhos 10,25,50,100,250; aquecimento 5 e repetições 11. Nenhuma seed, algoritmo ou critério foi alterado para melhorar resultados depois de conhecer os dados.

Os resultados em results/ foram validados por scripts/validate-results.ps1. Uma reprodução Java em target/phase5-reproduction confirmou todos os campos brutos não relacionados a tempo. Os CSVs finais permanecem preservados.

O relatório gerado pelo Java apresenta métricas, comparações, prioridade, tempo e metodologia. docs/experiment-findings.md registra comentários técnicos de revisão apoiados na evidência; não substitui o relatório da aplicação.

## Próximas entregas

Revisão da documentação e publicação no GitHub após revisão do usuário. Não criar commits automaticamente nesta alteração de escopo.

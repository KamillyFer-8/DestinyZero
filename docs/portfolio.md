# DestinyZero no portfólio

## Descrição curta

Simulador Java 21 que aplica Strategy para comparar três formas de organizar entregas, com datasets reproduzíveis, testes automatizados e relatórios determinísticos.

## Descrição com evidência experimental

Desenvolvi o DestinyZero para comparar estratégias de roteamento em simulações controladas. Apliquei orientação a objetos e Strategy, implementei testes JUnit 5 e executei 100 cenários com 300 execuções lógicas. Nearest Neighbor apresentou redução percentual média de 72,21% por cenário frente à ordem original. Priority posicionou, em média por cenário, 94,84% das entregas HIGH no primeiro terço. Resultados brutos, agregações e metodologia são persistidos pela aplicação Java em CSV e Markdown.

## Como explicar em entrevista

- O motor recebe implementações de RouteStrategy por composição, sem condicionais por algoritmo.
- Todos os algoritmos usam a mesma entrada imutável, permitindo comparações pareadas.
- Nearest Neighbor é uma heurística gulosa; há teste demonstrando que pode perder para a baseline.
- Priority tem objetivo distinto; sua avaliação combina distância e antecipação das entregas HIGH.
- Seeds fixas reproduzem pontos e métricas; tempo computacional depende do ambiente.
- A medição inclui aquecimento e repetições, mas não é benchmark científico.
- Os números são rastreáveis a results/raw-results.csv e results/summary.csv; não demonstram economia logística real.

Não afirmar que a heurística encontra a rota ótima ou que esses percentuais representam redução em entregas reais.

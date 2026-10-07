# Revisão final local

Revisão em 7 de outubro de 2026.

- Compilação limpa e suíte JUnit: 40 testes, nenhuma falha ou erro.
- CLI executada com classpath somente target/classes, sem dependências de runtime.
- Reprodução completa em target/final-review: 100 cenários e 300 execuções.
- scripts/validate-results.ps1 validou as 300 linhas brutas, 18 grupos consolidados e todas as tabelas do relatório final.
- Todos os campos brutos determinísticos coincidiram na reprodução; tempos não são considerados determinísticos.
- Os CSVs finais existentes em results/ foram preservados.
- Nenhuma integração externa está presente na aplicação.
- Dados de resultados são fictícios; não incluem credenciais ou dados pessoais de entregas.
- Repositório GitHub: https://github.com/KamillyFer-8/DestinyZero.

## Comandos utilizados

```powershell
mvn clean test -B -ntp
java -cp target/classes io.github.destinyzero.DestinyZero --full target/final-review
& ./scripts/validate-results.ps1 -ResultsPath results -ReproductionPath target/final-review
```

Para repetir o experimento, usar outro nome de pasta ou recompilar com clean antes de repetir o mesmo nome.

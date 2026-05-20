# Conventional Commits

## Formato

```
<type>(<scope>): <descrição curta no imperativo>

[corpo opcional]

[rodapé: BREAKING CHANGE ou closes #issue]
```

## Types

| Type | Quando usar |
|---|---|
| `feat` | Nova funcionalidade |
| `fix` | Correção de bug |
| `docs` | Documentação |
| `style` | Formatação (sem mudança de lógica) |
| `refactor` | Refatoração sem feat/fix |
| `test` | Testes |
| `chore` | Build, deps, config |
| `perf` | Melhoria de performance |
| `ci` | CI/CD |
| `build` | Sistema de build |

## Scopes

`backend`, `frontend`, `docker`, `ai`, `map`, `search`, `geocoding`, `ci`

## Exemplos

```
feat(backend): add hexagonal domain model for Attraction entity
feat(ai): integrate Claude Sonnet adapter with prompt caching
feat(map): implement Leaflet map with OpenStreetMap tiles
feat(search): add IBGE city autocomplete with debounce
fix(geocoding): handle Nominatim timeout with Resilience4j retry
chore(docker): add multi-stage Dockerfile for backend and frontend
ci: add GitHub Actions workflow for build validation
```

# 🗺️ TurismoSearch

Buscador de atrações turísticas com IA (Claude Sonnet), Spring Boot 3 + Angular + Leaflet/OpenStreetMap.

## Stack

| Camada | Tecnologia |
|---|---|
| Backend | Java 21 · Spring Boot 3.3 · Arquitetura Hexagonal |
| IA | Anthropic Claude Sonnet (com Prompt Caching) |
| Geocoding | Nominatim / OpenStreetMap |
| Cidades BR | IBGE API |
| Cache | Redis |
| Frontend | Angular 19 · Leaflet · Design Neobrutalist |
| Infra | Docker · Docker Compose |

## Rodando localmente

```bash
cp .env.example .env
# Edite .env e adicione sua ANTHROPIC_API_KEY
docker compose up --build
```

- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Conventional Commits

Este projeto usa [Conventional Commits](https://www.conventionalcommits.org/):

```
feat(backend): add hexagonal domain model
fix(ai): handle Claude API timeout
chore(docker): add multi-stage Dockerfile
```

## Estrutura

```
turismo-search/
├── backend/     # Spring Boot 3 + Arquitetura Hexagonal
├── frontend/    # Angular 19 + Neobrutalism
├── docker-compose.yml
└── docker-compose.prod.yml
```

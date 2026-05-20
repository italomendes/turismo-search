# 🤖 Claude Code Handoff — TurismoSearch

Este arquivo contém todas as instruções para continuar o desenvolvimento desta aplicação usando o **Claude Code CLI** (`claude` no terminal).

---

## Pré-requisitos instalados

- Java 25 (compatível com Java 21 target no pom.xml)
- Maven 3.9.x
- Node.js v22 + npm 10
- Angular CLI (`npm install -g @angular/cli`)
- Docker Desktop
- Git + GitHub CLI (`gh`)

---

## Estado atual do projeto

**Repositório:** https://github.com/italomendes/turismo-search  
**Branch ativa:** `develop`

### ✅ Concluído
- Repositório GitHub criado com branch `develop`
- `.gitignore`, `README.md`, `.env.example` criados
- CI/CD com GitHub Actions (`.github/workflows/ci.yml`)
- Template de PR e convenção de commits
- **Backend Spring Boot 3.5 gerado** com arquitetura hexagonal:
  - Domain: `Attraction`, `City`, `Coordinates`, `SearchQuery`, `AttractionCategory`
  - Ports inbound: `SearchAttractionsUseCase`, `GetCitiesUseCase`, `GetAttractionDetailsUseCase`
  - Ports outbound: `AiRecommendationPort`, `GeocodingPort`, `CityRepositoryPort`
  - Application Services: `SearchAttractionsService`, `GetCitiesService`
  - DTOs request/response completos
  - Adapter Claude AI (`ClaudeAiAdapter`, `ClaudePromptBuilder`) com Prompt Caching
  - Adapter Geocoding Nominatim (`NominatimGeocodingAdapter`)

### 🔄 Em andamento / Pendente
- [ ] Adapter IBGE (`IbgeApiAdapter`)
- [ ] Infrastructure: `WebClientConfig`, `CacheConfig`, `ClaudeProperties`, `application.yml`
- [ ] REST Controllers (`AttractionController`, `CityController`)
- [ ] Exception Handler global
- [ ] **Frontend Angular** (ainda não criado)
- [ ] **Dockerfiles** e `docker-compose.yml`
- [ ] **Conventional Commits** (commitlint + husky)
- [ ] **Push para GitHub**

---

## Como continuar com Claude Code CLI

### 1. Instalar Claude Code

```bash
npm install -g @anthropic/claude-code
```

### 2. Abrir o projeto

```bash
cd ~/projects/turismo-search
claude
```

### 3. Prompt para continuar o desenvolvimento

Cole este prompt no Claude Code:

```
Continue o desenvolvimento do projeto TurismoSearch que está em ~/projects/turismo-search.

O estado atual está documentado em CLAUDE_HANDOFF.md. Siga as instruções abaixo:

## O que falta implementar:

### Backend (~/projects/turismo-search/backend)

1. **IbgeApiAdapter** em `src/main/java/com/turismosearch/adapter/outbound/cities/`
   - Implementa `CityRepositoryPort`
   - Consulta https://servicodados.ibge.gov.br/api/v1/localidades/estados/{UF}/municipios
   - Cache no Redis por 7 dias (lista de cidades muda raramente)
   - DTO: `IbgeMunicipioResponse` com campos: id, nome, microrregiao.mesorregiao.UF.sigla

2. **Properties** em `src/main/java/com/turismosearch/infrastructure/properties/`:
   - `ClaudeProperties.java` — campos: apiKey, model, maxTokens, timeoutSeconds
   - `NominatimProperties.java` — campos: baseUrl, userAgent, timeoutSeconds
   - `IbgeProperties.java` — campos: baseUrl, timeoutSeconds
   - Todas com `@ConfigurationProperties` e `@Component`

3. **WebClientConfig** em `src/main/java/com/turismosearch/infrastructure/config/`:
   - Bean `claudeWebClient`: base URL https://api.anthropic.com/v1, header `x-api-key`, header `anthropic-version: 2023-06-01`, header `anthropic-beta: prompt-caching-2024-07-31`, timeout configurável
   - Bean `nominatimWebClient`: base URL de propriedade, User-Agent do Nominatim (obrigatório), delay de 1s entre requests (fair use policy)
   - Bean `ibgeWebClient`: base URL de propriedade

4. **CacheConfig** em `src/main/java/com/turismosearch/infrastructure/config/`:
   - Redis como cache store
   - Caches: "attractions" (TTL 24h), "geocoding-reverse" (TTL 7d), "geocoding-forward" (TTL 7d), "ibge-cities" (TTL 7d)
   - Serialização JSON

5. **SecurityConfig** em `src/main/java/com/turismosearch/infrastructure/config/`:
   - CSRF desabilitado
   - CORS configurado via properties
   - Endpoints `/api/**` e `/actuator/health` permitidos

6. **AttractionController** em `src/main/java/com/turismosearch/adapter/inbound/rest/`:
   - `POST /api/v1/attractions/search/by-location` — recebe `SearchByLocationRequest`, retorna `AttractionListResponse`
   - `POST /api/v1/attractions/search/by-city` — recebe `SearchByCityRequest`, retorna `AttractionListResponse`
   - Mapear domain → DTO response com um mapper privado

7. **CityController** em `src/main/java/com/turismosearch/adapter/inbound/rest/`:
   - `GET /api/v1/cities/states` — retorna lista de estados
   - `GET /api/v1/cities?stateCode=SP&query=camp&limit=10` — autocomplete

8. **GlobalExceptionHandler** em `src/main/java/com/turismosearch/adapter/inbound/rest/exception/`:
   - `@RestControllerAdvice`
   - Tratar: `MethodArgumentNotValidException` (400), `AiServiceException` (503), `AttractionNotFoundException` (404), `Exception` genérica (500)
   - Retornar JSON padronizado: `{ "error": "...", "message": "...", "timestamp": "..." }`

9. **application.yml** em `src/main/resources/`:
```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  application:
    name: turismo-search
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
  cache:
    type: redis

anthropic:
  api-key: ${ANTHROPIC_API_KEY}
  model: ${ANTHROPIC_MODEL:claude-sonnet-4-5}
  max-tokens: ${ANTHROPIC_MAX_TOKENS:4096}
  timeout-seconds: ${ANTHROPIC_TIMEOUT_SECONDS:30}

nominatim:
  base-url: ${NOMINATIM_BASE_URL:https://nominatim.openstreetmap.org}
  user-agent: ${NOMINATIM_USER_AGENT:TurismoSearch/1.0}
  timeout-seconds: ${NOMINATIM_TIMEOUT_SECONDS:10}

ibge:
  base-url: ${IBGE_BASE_URL:https://servicodados.ibge.gov.br/api/v1}
  timeout-seconds: ${IBGE_TIMEOUT_SECONDS:10}

cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:4200}

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always

resilience4j:
  retry:
    instances:
      claude-api:
        max-attempts: 3
        wait-duration: 2s
      nominatim:
        max-attempts: 2
        wait-duration: 1s
```

---

### Frontend (~/projects/turismo-search/frontend)

Gerar com:
```bash
cd ~/projects/turismo-search
ng new frontend --routing --style=scss --standalone --skip-git
cd frontend
npm install leaflet @types/leaflet
```

Componentes a criar (todos standalone):
1. `app/core/services/attraction.service.ts` — HttpClient calls para backend
2. `app/core/services/city.service.ts` — estados e autocomplete
3. `app/core/services/geolocation.service.ts` — Geolocation API browser
4. `app/features/search/search.component` — tela principal, toggle GPS/cidade
5. `app/features/map/map.component` — Leaflet com tiles Carto
6. `app/features/attractions/attraction-list.component` — grid de cards
7. `app/features/attractions/components/attraction-card/attraction-card.component`
8. `app/shared/components/loading-spinner/` e `error-banner/`

**Design Neobrutalist** — adicionar em `src/styles.scss`:
```scss
:root {
  --neo-border: 3px solid #0A0A0A;
  --neo-shadow: 5px 5px 0px #0A0A0A;
  --neo-shadow-hover: 2px 2px 0px #0A0A0A;
  --color-primary: #FFD600;
  --color-secondary: #FF4D4D;
  --color-accent: #00E5FF;
  --color-bg: #FFFEF0;
  --font-display: 'Space Grotesk', sans-serif;
}
```

---

### Docker

1. **backend/Dockerfile** — multi-stage: eclipse-temurin:21-jdk-alpine build + eclipse-temurin:21-jre-alpine runtime
2. **frontend/Dockerfile** — multi-stage: node:22-alpine build + nginx:alpine serve
3. **frontend/nginx.conf** — SPA routing + proxy /api/ para backend:8080
4. **docker-compose.yml** — serviços: redis, backend, frontend com depends_on e healthchecks
5. **docker-compose.prod.yml** — configurações de produção com restart policies e resource limits

---

### Conventional Commits + Husky

```bash
cd ~/projects/turismo-search/frontend
npm install -D @commitlint/cli @commitlint/config-conventional commitizen cz-conventional-changelog husky
npx husky init
echo "npx --no -- commitlint --edit \$1" > .husky/commit-msg
```

Criar `commitlint.config.js` na raiz do monorepo:
```js
module.exports = { extends: ['@commitlint/config-conventional'] }
```

---

### Push final para GitHub

Fazer commits separados por feature:
```bash
git add .github/ .gitignore README.md .env.example
git commit -m "chore: initial repo structure with CI and commit conventions"

git add backend/
git commit -m "feat(backend): implement hexagonal architecture with Claude AI adapter"

git add frontend/
git commit -m "feat(frontend): add Angular 19 with neobrutalist design system"

git add docker-compose.yml docker-compose.prod.yml backend/Dockerfile frontend/Dockerfile
git commit -m "chore(docker): add multi-stage Dockerfiles and compose files"

git push -u origin develop
```

---

## Variáveis de ambiente necessárias

Copie `.env.example` para `.env` e preencha:
- `ANTHROPIC_API_KEY` — obrigatório, obter em https://console.anthropic.com
- As demais têm valores padrão no `application.yml`

## Testar localmente

```bash
cp .env.example .env
# Editar .env com a ANTHROPIC_API_KEY
docker compose up --build
# Frontend: http://localhost:4200
# Backend: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

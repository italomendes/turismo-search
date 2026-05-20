# ============================================================
# Stage 1: Build Angular com Node 22
# ============================================================
FROM node:22-alpine AS builder
WORKDIR /app

# Copiar package.json e instalar dependências
COPY package*.json ./
RUN npm ci --prefer-offline

# Copiar código e compilar para produção
COPY . .
RUN npm run build -- --configuration production

# ============================================================
# Stage 2: Servir com Nginx
# ============================================================
FROM nginx:1.27-alpine AS runtime

# Copiar build do Angular
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copiar config customizada do Nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost/health || exit 1

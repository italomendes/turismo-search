# ============================================================
# Stage 1: Build com Maven + JDK 21
# ============================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copiar pom e baixar dependências (cache layer)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copiar código e compilar
COPY src ./src
RUN ./mvnw package -DskipTests -q

# ============================================================
# Stage 2: Runtime com JRE 21 (imagem menor ~200MB)
# ============================================================
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Criar usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/*.jar app.jar

USER appuser
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]

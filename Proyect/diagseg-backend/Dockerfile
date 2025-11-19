FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar con rutas COMPLETAS desde la raíz del repo
COPY Proyect/diagseg-backend/gradle gradle
COPY Proyect/diagseg-backend/gradlew .
COPY Proyect/diagseg-backend/gradle.properties .
COPY Proyect/diagseg-backend/settings.gradle.kts .
COPY Proyect/diagseg-backend/build.gradle.kts .

# Dar permisos
RUN chmod +x gradlew

# Copiar código fuente
COPY Proyect/diagseg-backend/src src

# Build
RUN ./gradlew build -x test --no-daemon

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache nmap curl

# Copiar JAR
COPY --from=build /app/build/quarkus-app/lib/ ./lib/
COPY --from=build /app/build/quarkus-app/*.jar ./
COPY --from=build /app/build/quarkus-app/app/ ./app/
COPY --from=build /app/build/quarkus-app/quarkus/ ./quarkus/

# Copiar GeoLite2
COPY --from=build /app/src/main/resources/geo ./geo/

EXPOSE 8080

ENTRYPOINT ["java", "-Dquarkus.profile=prod", "-Dquarkus.http.host=0.0.0.0", "-jar", "quarkus-run.jar"]
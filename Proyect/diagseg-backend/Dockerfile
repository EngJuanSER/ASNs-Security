FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar archivos Gradle (sin .kts, son .gradle)
COPY Proyect/diagseg-backend/gradle gradle
COPY Proyect/diagseg-backend/gradlew gradlew
COPY Proyect/diagseg-backend/gradle.properties gradle.properties
COPY Proyect/diagseg-backend/settings.gradle settings.gradle
COPY Proyect/diagseg-backend/build.gradle build.gradle
COPY Proyect/diagseg-backend/src src

# Dar permisos
RUN chmod +x gradlew

# Build
RUN ./gradlew build -x test --no-daemon

# Runtime (igual que antes)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache nmap nmap-scripts curl

COPY --from=build /app/build/quarkus-app/lib/ ./lib/
COPY --from=build /app/build/quarkus-app/*.jar ./
COPY --from=build /app/build/quarkus-app/app/ ./app/
COPY --from=build /app/build/quarkus-app/quarkus/ ./quarkus/

RUN mkdir -p geo && \
    echo "Descargando GeoLite2-City.mmdb..." && \
    curl -L "https://github.com/P3TERX/GeoLite.mmdb/raw/download/GeoLite2-City.mmdb" \
         -o geo/GeoLite2-City.mmdb && \
    ls -lh geo/GeoLite2-City.mmdb

EXPOSE 8080

ENTRYPOINT ["java", "-Dquarkus.profile=prod", "-Dquarkus.http.host=0.0.0.0", "-jar", "quarkus-run.jar"]
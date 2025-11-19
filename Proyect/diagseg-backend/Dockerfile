FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY Proyect/diagseg-backend/gradle gradle
COPY Proyect/diagseg-backend/gradlew gradlew
COPY Proyect/diagseg-backend/gradle.properties gradle.properties
COPY Proyect/diagseg-backend/settings.gradle settings.gradle
COPY Proyect/diagseg-backend/build.gradle build.gradle
COPY Proyect/diagseg-backend/src src

RUN chmod +x gradlew

RUN apk add --no-cache curl && \
    mkdir -p src/main/resources/geo && \
    echo "Descargando GeoLite2-City.mmdb en resources..." && \
    curl -L "https://github.com/P3TERX/GeoLite.mmdb/raw/download/GeoLite2-City.mmdb" \
         -o src/main/resources/geo/GeoLite2-City.mmdb && \
    ls -lh src/main/resources/geo/GeoLite2-City.mmdb

RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Ejecutar como root para nmap
USER root

RUN apk add --no-cache nmap nmap-scripts curl

COPY --from=build /app/build/quarkus-app/lib/ ./lib/
COPY --from=build /app/build/quarkus-app/*.jar ./
COPY --from=build /app/build/quarkus-app/app/ ./app/
COPY --from=build /app/build/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8080

ENTRYPOINT ["java", "-Dquarkus.profile=prod", "-Dquarkus.http.host=0.0.0.0", "-jar", "quarkus-run.jar"]
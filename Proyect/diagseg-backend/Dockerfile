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

# Descargar GeoLite2 en resources ANTES del build
RUN apk add --no-cache curl && \
    mkdir -p src/main/resources/geo && \
    echo "Descargando GeoLite2-City.mmdb en resources..." && \
    curl -L "https://github.com/P3TERX/GeoLite.mmdb/raw/download/GeoLite2-City.mmdb" \
         -o src/main/resources/geo/GeoLite2-City.mmdb && \
    ls -lh src/main/resources/geo/GeoLite2-City.mmdb

# Build (ahora incluirá GeoLite2 en el JAR)
RUN ./gradlew build -x test --no-daemon

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar nmap CON permisos de red
RUN apk add --no-cache nmap nmap-scripts curl libcap && \
    setcap cap_net_raw,cap_net_admin,cap_net_bind_service+eip /usr/bin/nmap

COPY --from=build /app/build/quarkus-app/lib/ ./lib/
COPY --from=build /app/build/quarkus-app/*.jar ./
COPY --from=build /app/build/quarkus-app/app/ ./app/
COPY --from=build /app/build/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8080

ENTRYPOINT ["java", "-Dquarkus.profile=prod", "-Dquarkus.http.host=0.0.0.0", "-jar", "quarkus-run.jar"]
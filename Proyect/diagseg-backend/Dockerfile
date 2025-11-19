FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar wrapper de Gradle (paths RELATIVOS)
COPY gradle gradle
COPY gradlew .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY build.gradle.kts .

# Dar permisos de ejecución
RUN chmod +x gradlew

# Copiar código fuente
COPY src src

# Build del proyecto
RUN ./gradlew build -x test --no-daemon

# Imagen de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar nmap
RUN apk add --no-cache nmap curl

# Copiar el JAR compilado
COPY --from=build /app/build/quarkus-app/lib/ ./lib/
COPY --from=build /app/build/quarkus-app/*.jar ./
COPY --from=build /app/build/quarkus-app/app/ ./app/
COPY --from=build /app/build/quarkus-app/quarkus/ ./quarkus/

# Copiar base de datos GeoLite2
COPY --from=build /app/src/main/resources/geo ./geo/

EXPOSE 8080

ENTRYPOINT ["java", "-Dquarkus.profile=prod", "-Dquarkus.http.host=0.0.0.0", "-jar", "quarkus-run.jar"]
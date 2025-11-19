FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copiar wrapper de Gradle
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

# ==================================
# Imagen de runtime (más liviana)
# ==================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Instalar nmap (necesario para el análisis)
RUN apk add --no-cache nmap curl

# Copiar el JAR compilado desde la imagen de build
COPY --from=build /app/build/quarkus-app/lib/ ./lib/
COPY --from=build /app/build/quarkus-app/*.jar ./
COPY --from=build /app/build/quarkus-app/app/ ./app/
COPY --from=build /app/build/quarkus-app/quarkus/ ./quarkus/

# Copiar base de datos GeoLite2
COPY --from=build /app/src/main/resources/geo ./geo/

# Exponer puerto
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-Dquarkus.profile=prod", "-Dquarkus.http.host=0.0.0.0", "-jar", "quarkus-run.jar"]
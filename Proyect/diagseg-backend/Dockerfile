FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Ajustar paths con el prefijo Proyect/diagseg-backend/
COPY Proyect/diagseg-backend/gradle gradle
COPY Proyect/diagseg-backend/gradlew .
COPY Proyect/diagseg-backend/gradle.properties .
COPY Proyect/diagseg-backend/settings.gradle.kts .
COPY Proyect/diagseg-backend/build.gradle.kts .

RUN chmod +x gradlew

COPY Proyect/diagseg-backend/src src

RUN ./gradlew build -x test --no-daemon

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache nmap curl

COPY --from=build /app/build/quarkus-app/lib/ ./lib/
COPY --from=build /app/build/quarkus-app/*.jar ./
COPY --from=build /app/build/quarkus-app/app/ ./app/
COPY --from=build /app/build/quarkus-app/quarkus/ ./quarkus/

COPY --from=build /app/src/main/resources/geo ./geo/

EXPOSE 8080

ENTRYPOINT ["java", "-Dquarkus.profile=prod", "-Dquarkus.http.host=0.0.0.0", "-jar", "quarkus-run.jar"]
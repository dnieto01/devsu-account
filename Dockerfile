# Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline -DskipTests || true
COPY src ./src
RUN mvn -q -B -DskipTests package \
    && mv target/devsu-account-*.jar /build/app.jar

# Run
FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /build/app.jar app.jar

EXPOSE 8096
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8096/ping || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]

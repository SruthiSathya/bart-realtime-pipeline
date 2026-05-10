# Stage 1: Build the jar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached layer — speeds up rebuilds)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn clean package -q -DskipTests

# Stage 2: Run the jar (smaller final image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

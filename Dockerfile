# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q dependency:go-offline
COPY src ./src
RUN mvn -q package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/target/teslo-shop-0.0.1-SNAPSHOT.jar app.jar
COPY static ./static
EXPOSE 3000 3001
ENTRYPOINT ["java", "-jar", "app.jar"]

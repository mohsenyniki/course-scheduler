# Start from a Maven image to build the JAR
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Now use a smaller image to run it
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/Course-Scheduler-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

# --- Stage 1: Build the app ---
FROM maven:3.9.7-eclipse-temurin-24 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# --- Stage 2: Create minimal runtime image ---
FROM eclipse-temurin:24-jdk

WORKDIR /app

# Copy JAR from the previous build stage
COPY --from=build /build/target/card-management-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]

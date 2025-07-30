# Stage 1: Build with Maven
FROM maven:3.9.0-eclipse-temurin-17 as builder

WORKDIR /app

# Copy only pom.xml to cache dependencies
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build the jar (skip tests to speed up)
RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Copy the jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

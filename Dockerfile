FROM maven:3.9-eclipse-temurin-21 as builder
WORKDIR /build
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /build/target/*.jar trade-system-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "trade-system-0.0.1-SNAPSHOT.jar"]
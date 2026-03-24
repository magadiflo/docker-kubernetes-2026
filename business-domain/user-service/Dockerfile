FROM eclipse-temurin:25-jdk-alpine
WORKDIR /app
COPY ./mvnw ./
COPY ./.mvn ./.mvn
COPY ./pom.xml ./
RUN sed -i -e 's/\r$//' ./mvnw && ./mvnw dependency:go-offline
COPY ./src ./src
RUN ./mvnw clean package -DskipTests
EXPOSE 8001
ENTRYPOINT ["java", "-jar", "./target/user-service-0.0.1-SNAPSHOT.jar"]

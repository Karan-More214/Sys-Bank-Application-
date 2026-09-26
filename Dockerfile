FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# application.properties is gitignored on purpose (see .gitignore) and is never
# committed, so it never exists in the build context. Generate it fresh from the
# placeholder template on every build; real values come from env vars at runtime.
RUN cp src/main/resources/application.properties.example src/main/resources/application.properties
RUN mvn -B -q clean package -DskipTests

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

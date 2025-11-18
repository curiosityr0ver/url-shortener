FROM maven:3.9.8-eclipse-temurin-21 AS build

WORKDIR /workspace

# Copy only the files needed to resolve dependencies first (leveraging Docker cache)
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw -q -B dependency:go-offline

# Now copy the rest of the source and build the application
COPY src src
RUN ./mvnw -q -B -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ARG JAR_FILE=/workspace/target/ishumehta-0.0.1-SNAPSHOT.jar
COPY --from=build ${JAR_FILE} app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]


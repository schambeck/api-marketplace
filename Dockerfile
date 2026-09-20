ARG APP=/app
ARG TARGET=${APP}/target

FROM maven:3.9.16-eclipse-temurin-21-alpine AS build
ARG APP
WORKDIR ${APP}

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw package -DskipTests
ARG TARGET
WORKDIR ${TARGET}
COPY certbot/cert.pem .
RUN java -Djarmode=layertools -jar api-marketplace*.jar extract

FROM eclipse-temurin:21-jre-alpine
ARG TARGET
COPY --from=build ${TARGET}/dependencies/ ./
COPY --from=build ${TARGET}/snapshot-dependencies/ ./
COPY --from=build ${TARGET}/spring-boot-loader/ ./
COPY --from=build ${TARGET}/application/ ./
COPY --from=build ${TARGET}/cert.pem ./
RUN keytool -import -alias gamebazaar.duckdns.org -storepass changeit -file cert.pem -noprompt -cacerts -trustcacerts
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

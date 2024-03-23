FROM eclipse-temurin:17-jre-alpine AS builder
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} app.jar
COPY classes/cert/marketplace.jks marketplace.jks
COPY classes/cert/marketplace.p12 marketplace.p12
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre-alpine
COPY --from=builder marketplace.jks ./
COPY --from=builder marketplace.p12 ./
RUN keytool -importkeystore -srckeystore marketplace.jks -destkeystore marketplace.p12 -deststoretype pkcs12
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

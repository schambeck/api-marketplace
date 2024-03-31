FROM eclipse-temurin:17-jre-alpine AS builder
ARG JAR_FILE=*.jar
COPY ${JAR_FILE} app.jar
COPY classes/cert/srv-keycloak.servebeer.com.der srv-keycloak.servebeer.com.der
RUN java -Djarmode=layertools -jar app.jar extract

FROM eclipse-temurin:17-jre-alpine
COPY --from=builder srv-keycloak.servebeer.com.der ./
RUN keytool -noprompt -cacerts -storepass changeit -trustcacerts -import -alias srv-keycloak.servebeer.com -file srv-keycloak.servebeer.com.der
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

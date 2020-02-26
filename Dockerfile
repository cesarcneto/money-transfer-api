FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/money-transfer-api
WORKDIR /home/gradle/money-transfer-api
RUN gradle build

FROM openjdk:11-jre-slim
COPY --from=builder /home/gradle/money-transfer-api/build/libs/money-transfer-api-all.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]
FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/app
WORKDIR /home/gradle/app
RUN gradle build

FROM openjdk:11-jre-slim
COPY --from=builder /home/gradle/app/build/libs/app.jar /app/app.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "app.jar"]
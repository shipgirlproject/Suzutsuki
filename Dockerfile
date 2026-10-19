FROM gradle:8.9.0-jdk22-alpine AS build

LABEL stage=build

RUN mkdir -p /root/build

WORKDIR /root/build

COPY . /root/build

RUN gradle build --no-daemon

FROM eclipse-temurin:22-jre-alpine

WORKDIR /usr/src/app

COPY --from=build /root/build/build/libs/suzutsuki*.jar ./suzutsuki.jar

ENTRYPOINT ["java", "-jar", "suzutsuki.jar"]
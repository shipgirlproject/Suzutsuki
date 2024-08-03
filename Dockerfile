FROM gradle:8.9.0-jdk22-alpine as build

LABEL stage=build

RUN mkdir -p /root/build

WORKDIR /root/build

COPY . /root/build

RUN gradle build --no-daemon

FROM openjdk:22-slim

RUN groupadd -g 101 suzutsuki && \
    useradd -r -u 101 -g suzutsuki suzutsuki && \
    mkdir -p /home/suzutsuki

COPY --from=build /root/build/build/libs/suzutsuki*.jar /home/suzutsuki/suzutsuki.jar

RUN chown -R suzutsuki:suzutsuki /home/suzutsuki

USER suzutsuki

WORKDIR /home/suzutsuki

ENTRYPOINT ["java", "-jar", "suzutsuki.jar"]
FROM ubuntu:latest

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get install -y --no-install-recommends gradle openjdk-7-jdk git unzip ssh wget

ADD server /server

CMD cd /server; ./gradlew run

EXPOSE 8080

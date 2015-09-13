FROM ubuntu:latest

ENV DEBIAN_FRONTEND noninteractive

RUN apt-get update
RUN apt-get install -y --no-install-recommends gradle openjdk-7-jdk git unzip ssh wget

ADD . /kwikshop-server

CMD cd /kwikshop-server ; git fetch ; git checkout release ; git pull ; ./gradlew run

EXPOSE 443

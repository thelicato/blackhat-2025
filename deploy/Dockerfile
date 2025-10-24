FROM debian:trixie-slim

WORKDIR /opt

RUN apt update && apt install -y unzip wget \
    && wget -O platform-tools.zip https://dl.google.com/android/repository/platform-tools-latest-linux.zip \
    && unzip platform-tools.zip \
    && rm platform-tools.zip
ENV PATH="$PATH:/opt/platform-tools"
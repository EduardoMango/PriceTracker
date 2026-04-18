FROM ubuntu:latest
LABEL authors="coding"

ENTRYPOINT ["top", "-b"]
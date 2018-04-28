FROM  ubuntu:16.04
MAINTAINER "Maxim Filatov" <info@escom-bpm.com>

ENV TERM=xterm

RUN apt-get -y update && \
    apt-get -y install mc && \
    apt-get -y install net-tools &&\
    adduser --no-create-home --disabled-password --disabled-login --gecos "" wildfly && \
    adduser --no-create-home --disabled-password --disabled-login --gecos "" mysql 
 
CMD ["bash"]
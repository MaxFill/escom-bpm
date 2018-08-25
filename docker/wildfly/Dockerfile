FROM ubuntu1604
MAINTAINER "Maxim Filatov" <info@escom-bpm.com>

ENV TERM=xterm

RUN  apt-get update && \
     apt-get -y install software-properties-common python-software-properties && \
     add-apt-repository -y ppa:webupd8team/java && \
     apt-get update 

RUN echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 select true" | debconf-set-selections && \
    echo "oracle-java8-installer shared/accepted-oracle-license-v1-1 seen true" | debconf-set-selections && \
    apt-get install -y oracle-java8-installer

RUN apt-get -y install unzip && \
    cd /opt && \
    wget http://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip && \	 
    unzip wildfly-10.1.0.Final.zip -d /opt/ && \
    ln -s wildfly-10.1.0.Final wildfly

RUN echo "mysql-server mysql-server/root_password password RTY789zxc0" | debconf-set-selections && \
    echo "mysql-server mysql-server/root_password_again password RTY789zxc0" | debconf-set-selections && \
    apt-get -y install mysql-server

RUN add-apt-repository -y ppa:libreoffice/ppa && \
    apt-get update && \
    apt-get -y install libreoffice libreoffice-style-breeze && \
    apt-get -y install unoconv

RUN add-apt-repository -y ppa:builds/sphinxsearch-rel22 && \
    apt-get -y update && \
    apt-get -y install sphinxsearch && \
    chown -R sphinxsearch /opt/sphinx && \
    chown -R sphinxsearch /var/run/sphinxsearch

ENV JAVA_HOME="/usr/lib/jvm/java-8-oracle"
ENV WILDFLY="/opt/wildfly/bin"   

COPY /mysql/*   /opt/wildfly/modules/system/layers/base/com/mysql/main/
COPY /eclipse/* /opt/wildfly/modules/system/layers/base/org/eclipse/persistence/main/
COPY /org/     /opt/wildfly/modules/org/
COPY /javax/   /opt/wildfly/modules/javax/
COPY modeshape_3_0.xsd /opt/wildfly/docs/schema/
COPY mysql.cnf /etc/mysql/conf.d/

RUN chown -R wildfly.wildfly /opt/wildfly* 

ENV LAUNCH_JBOSS_IN_BACKGROUND true
WORKDIR  /opt/wildfly/standalone
EXPOSE 8080 9990 3306 8444

CMD /etc/init.d/mysql start; service sphinxsearch start; /opt/wildfly/bin/standalone.sh --server-config=standalone-modeshape.xml;
FROM ubuntu1604

MAINTAINER "Maxim Filatov" <fms2002@mail.ru>

RUN mkdir -p /opt/wildfly/standalone/attaches && \
    mkdir -p /opt/wildfly/standalone/deployments && \
    mkdir -p /opt/sphinx/data/docs

COPY /configuration/* /opt/wildfly/standalone/configuration/ 
COPY /jasper_reports/* /opt/wildfly/standalone/jasper_reports/
COPY /mysql* /var/lib/mysql
COPY /help/* /opt/wildfly/standalone/help/
COPY escom-bpm-ear-1.0-SNAPSHOT.ear /opt/wildfly/standalone/deployments/
COPY /sphinx/sphinx.conf /etc/sphinxsearch/
COPY /sphinx/ru.pak /usr/share/sphinxsearch/dicts/

RUN chown -R wildfly.wildfly /opt/wildfly/standalone* && \
    chown -R mysql.mysql /var/lib/mysql*

VOLUME /opt/wildfly/standalone
VOLUME /var/lib/mysql
VOLUME /etc/sphinxsearch
VOLUME /opt/sphinxsearch/data/docs
VOLUME /usr/share/sphinxsearch/dicts

CMD /bin/sh

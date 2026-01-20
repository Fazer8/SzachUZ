FROM tomcat:11.0.13-jdk25

USER root

RUN apt-get update && \
    apt-get install -y ca-certificates curl iputils-ping && \
    update-ca-certificates

RUN mkdir -p /opt/szachuz/avatars

COPY ./target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]

RUN #mkdir -p /opt/szachuz/avatars

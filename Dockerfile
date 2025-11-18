FROM tomcat:11.0.13-jdk25

COPY ./target/*.war /usr/local/tomcat/webapps/ROOT.war
RUN mkdir -p /opt/szachuz/avatars
EXPOSE 8080

CMD ["catalina.sh", "run"]

RUN #mkdir -p /opt/szachuz/avatars

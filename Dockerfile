FROM tomcat:11.0.13-jdk25

COPY ./target/*.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh", "run"]

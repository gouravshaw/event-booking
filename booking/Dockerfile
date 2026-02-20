FROM tomcat:10-jdk21-temurin

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/ROOT
RUN rm -rf /usr/local/tomcat/webapps/examples
RUN rm -rf /usr/local/tomcat/webapps/docs
RUN rm -rf /usr/local/tomcat/webapps/manager
RUN rm -rf /usr/local/tomcat/webapps/host-manager

# Copy the pre-built WAR file
COPY target/*.war /usr/local/tomcat/webapps/booking.war

# Copy Firebase service account key
COPY src/main/resources/serviceAccountKey.json /usr/local/tomcat/serviceAccountKey.json

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
CMD ["catalina.sh", "run"]
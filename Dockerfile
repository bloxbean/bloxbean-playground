FROM openjdk:17.0.2-jdk
RUN apk --no-cache add curl
COPY target/bloxbean-playground-*.jar bloxbean-playground.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar bloxbean-playground.jar

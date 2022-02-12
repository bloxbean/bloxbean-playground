#FROM openjdk:17-alpine
FROM bellsoft/liberica-openjdk-alpine-musl:17
RUN apk --no-cache add curl
COPY build/libs/bloxbean-playground-*-all.jar bloxbean-playground.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify ${JAVA_OPTS} -jar bloxbean-playground.jar

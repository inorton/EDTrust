FROM openjdk:8-jdk-slim-stretch

RUN whoami ; pwd 
RUN mkdir -p /edtrust ; chmod 777 /edtrust
WORKDIR /edtrust
ADD ./build/libs/edtrust-spring-boot-0.0.1.jar .
RUN find . -type f -name 'edtrust-*.jar'

WORKDIR /data

ENTRYPOINT ["java", "-Xmx96m", "-Xms64m"]
CMD ["-jar", "/edtrust/edtrust-spring-boot-0.0.1.jar"]

EXPOSE 8082

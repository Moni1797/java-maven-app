FROM amazoncorretto:17-alpine-jdk

COPY ./target/java-maven-app-*.jar /usr/app/
WORKDIR /usr/app

CMD ["sh", "-c", "java -jar java-maven-app-*.jar"]


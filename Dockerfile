FROM amazoncorretto:17
EXPOSE 8080
COPY build/libs/Ringo-0.0.1-SNAPSHOT.jar /Ringo-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java", "-jar", "Ringo-0.0.1-SNAPSHOT.jar"]
FROM gradle:7-jdk17-focal AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

FROM gradle:7-jdk17-focal
EXPOSE 8080:8080
RUN apt-get update
RUN apt-get install gnupg -y
RUN wget -qO - https://www.mongodb.org/static/pgp/server-6.0.asc | apt-key add -
RUN echo "deb [ arch=amd64,arm64 ] https://repo.mongodb.org/apt/ubuntu focal/mongodb-org/6.0 multiverse" | tee /etc/apt/sources.list.d/mongodb-org-6.0.list
RUN apt-get update
RUN apt-get install -y mongodb-mongosh
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/genai-api.jar
ENTRYPOINT ["java","-jar","/app/genai-api.jar"]

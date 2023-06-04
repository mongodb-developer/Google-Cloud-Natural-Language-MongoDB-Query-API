FROM gradle:8-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

#Add MongoDB to the image
FROM mongo:6.0.6
EXPOSE 27017:27017
CMD ["mongod"]

#Add React UI image from local
FROM genai-ui:latest AS genai-ui

FROM gradle:8-jdk17
EXPOSE 8080:8080
RUN mkdir /app
COPY --from=build /home/gradle/src/build/libs/*.jar /app/genai-api.jar
ENTRYPOINT ["java","-jar","/app/genai-api.jar"]

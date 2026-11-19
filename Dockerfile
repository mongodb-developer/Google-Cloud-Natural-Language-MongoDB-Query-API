# ---- build stage ----
FROM gradle:7-jdk17-focal AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle buildFatJar --no-daemon

# ---- runtime stage ----
# Zulu 17.0.21.1 includes the fix for CVE-2026-114938 ("Java4Shell"), an
# out-of-bounds write in the AVX-512 Base64 decoder. Temurin has not yet
# published a patched 17 image; Zulu and Corretto have.
FROM azul/zulu-openjdk:17.0.21.1-jre

EXPOSE 8080

# mongosh is required at runtime: RunMQL.kt shells out to it via Runtime.exec.
RUN apt-get update && apt-get install -y --no-install-recommends \
        ca-certificates curl gnupg \
    && curl -fsSL https://pgp.mongodb.com/server-8.0.asc \
        | gpg --dearmor -o /usr/share/keyrings/mongodb-server-8.0.gpg \
    && echo "deb [ signed-by=/usr/share/keyrings/mongodb-server-8.0.gpg ] https://repo.mongodb.org/apt/ubuntu jammy/mongodb-org/8.0 multiverse" \
        > /etc/apt/sources.list.d/mongodb-org-8.0.list \
    && apt-get update \
    && apt-get install -y --no-install-recommends mongodb-mongosh \
    && apt-get purge -y gnupg && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /home/gradle/src/build/libs/*.jar /app/genai-api.jar
ENTRYPOINT ["java","-jar","/app/genai-api.jar"]

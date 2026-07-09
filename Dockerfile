FROM eclipse-temurin:25
RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    curl -fsSL 'https://azurecliprod.blob.core.windows.net/$root/deb_install.sh' | bash
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
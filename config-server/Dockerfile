# config-server/Dockerfile
FROM eclipse-temurin:17-jre-alpine

# Installer git (nécessaire pour cloner le repo de config)
RUN apk add --no-cache git

# Créer un utilisateur non-root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copier le JAR
COPY target/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 8888

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8888/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Xms256m", \
  "-Xmx512m", \
  "-jar", \
  "app.jar"]
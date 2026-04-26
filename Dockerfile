FROM eclipse-temurin:21-jre-alpine AS extract
WORKDIR /workspace

COPY target/*.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=extract --chown=appuser:appgroup /workspace/extracted/dependencies/ ./
COPY --from=extract --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=extract --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/ ./
COPY --from=extract --chown=appuser:appgroup /workspace/extracted/application/ ./
USER appuser

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseZGC"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

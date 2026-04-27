FROM eclipse-temurin:21-jdk-alpine AS local-build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B \
    && jar_file="$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' | sort)" \
    && test -n "$jar_file" \
    && test "$(printf '%s\n' "$jar_file" | wc -l)" -eq 1 \
    && cp "$jar_file" app.jar

FROM eclipse-temurin:21-jre-alpine AS ci-artifact
WORKDIR /workspace

COPY target/app.jar app.jar

FROM local-build AS extract-local
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM ci-artifact AS extract-ci
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM eclipse-temurin:21-jre-alpine AS runtime-base
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseZGC"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]

FROM runtime-base AS ci-runtime
COPY --from=extract-ci --chown=appuser:appgroup /workspace/extracted/dependencies/ ./
COPY --from=extract-ci --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=extract-ci --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/ ./
COPY --from=extract-ci --chown=appuser:appgroup /workspace/extracted/application/ ./
USER appuser

FROM runtime-base AS runtime
COPY --from=extract-local --chown=appuser:appgroup /workspace/extracted/dependencies/ ./
COPY --from=extract-local --chown=appuser:appgroup /workspace/extracted/snapshot-dependencies/ ./
COPY --from=extract-local --chown=appuser:appgroup /workspace/extracted/spring-boot-loader/ ./
COPY --from=extract-local --chown=appuser:appgroup /workspace/extracted/application/ ./
USER appuser

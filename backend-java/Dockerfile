FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /build

RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
COPY mvnw.cmd .

RUN mvn dependency:go-offline

COPY src src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

RUN useradd -r -u 1001 -g root appuser

COPY --from=builder /build/target/*.jar app.jar

RUN chown -R appuser:root /app
USER 1001

ENV JAVA_OPTS="\
    -XX:+UseParallelGC \
    -XX:MaxRAMPercentage=75.0 \
    -Djava.security.egd=file:/dev/./urandom \
    -Dfile.encoding=UTF-8 \
    --add-opens java.base/java.time=ALL-UNNAMED"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
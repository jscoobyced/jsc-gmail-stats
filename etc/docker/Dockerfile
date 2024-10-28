FROM eclipse-temurin:22-jdk-alpine AS builder

WORKDIR /app

ARG APP_VERSION=0.0.0

COPY src src/
COPY gradle gradle/
COPY gradlew build.gradle.kts gradle.properties settings.gradle.kts ./
RUN echo "org.gradle.daemon=false" >> gradle.properties
RUN ./gradlew build
RUN cd ./build/distributions && tar -xf jsc-gmail-stats-$APP_VERSION.tar

FROM eclipse-temurin:22-jdk-alpine AS distribution

ARG APP_VERSION=0.0.0
ENV JSC_CALLBACK_URL="http://localhost:8888/Callback"

WORKDIR /app

RUN set -x && apk update
RUN set -x && apk add --upgrade --no-cache gcompat
RUN mkdir jsc-gmail-stats
COPY --from=builder /app/build/distributions/jsc-gmail-stats-$APP_VERSION /app/jsc-gmail-stats/

COPY etc/bin/entrypoint.sh /app/
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
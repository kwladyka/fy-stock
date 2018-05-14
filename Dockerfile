# The way how Dockerfile is build saves developers time to tests changes
# Deployment on production is also faster. It can be critical if bug will be deployed.

# 1. The longest step. Prepare image with dependecies only.
# This step is time consuming, so we wan't to avoid repeating this step.
# As long as developers wouldn't change project.clj this step is in cache.

FROM clojure:lein-alpine as dependencies
WORKDIR /app
COPY project.clj .
RUN lein deps

# 2. Fast step, only copy project files
FROM dependencies as dependencies-full
WORKDIR /app
COPY . .

# 3. Tests independent from other steps
# We don't use artifacts from this step later.
# There is no risk something will pass to production from tests.
FROM dependencies-full
WORKDIR /app
RUN lein test

# 4. We build final jar file using image from step 2.
FROM dependencies-full as builder
WORKDIR /app
RUN lein uberjar

# 5. Describe how to run application
FROM anapsix/alpine-java
LABEL maintainer = "Krzysztof Władyka"
LABEL Description = "Fy! stock preview"
EXPOSE 8080
WORKDIR /app
COPY --from=builder /app/target/*-standalone.jar ./app.jar
CMD ["java", "-cp", "/app/app.jar", "clojure.main", "-m", "fy-stock.core"]

# 6. Test if application is healthly
HEALTHCHECK --interval=5m --timeout=3s --retries=3 \
    CMD curl --output /dev/null --silent --head --fail -k http://localhost:8080/healthcheck || exit 1

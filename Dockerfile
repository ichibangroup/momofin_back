# Run stage
FROM docker.io/library/eclipse-temurin:21-jre-alpine AS runner

ARG USER_NAME=advshop
ARG USER_UID=1000
ARG USER_GID=${USER_UID}

# Create user and GCP keys directory
RUN addgroup -g ${USER_GID} ${USER_NAME} \
    && adduser -h /opt/advshop -D -u ${USER_UID} -G ${USER_NAME} ${USER_NAME} \
    && mkdir -p /home/runner/work \
    && chown -R ${USER_NAME}:${USER_NAME} /home/runner/work

# Copy the service account keys and set permissions
COPY --chown=${USER_NAME}:${USER_NAME} .gcp/gcp_sa_private_key.pem /home/runner/work/
COPY --chown=${USER_NAME}:${USER_NAME} .gcp/wrongkey.pem /home/runner/work/

# Set the working directory and copy the application
USER ${USER_NAME}
WORKDIR /opt/momofin
COPY --from=builder --chown=${USER_UID}:${USER_GID} /src/momofin/build/libs/*.jar app.jar

# Set the PKEY_DIRECTORY environment variable
ENV PKEY_DIRECTORY=/home/runner/work

EXPOSE 8080

ENTRYPOINT ["java"]
CMD ["-jar", "app.jar"]
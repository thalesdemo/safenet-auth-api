FROM amazoncorretto:11

# Install the 'shadow-utils' package to get the 'groupadd' and 'useradd' utilities
# Also install jq for the x-api-key generation
RUN yum update -y && \
    yum install -y jq && \ 
    yum install -y shadow-utils && \ 
    yum clean all

# Create a new group and user with the same UID and GID as "appuser" on the host machine
ARG USER_ID=1000
ARG GROUP_ID=1000
RUN groupadd -g $GROUP_ID appuser && \
    useradd -u $USER_ID -g $GROUP_ID -m appuser

# Create new directories
RUN mkdir -p /app/config
RUN mkdir -p /app/secret
RUN mkdir -p /app/tools

# Set the working directory and copy your application files
WORKDIR /app
COPY jar/safenet-auth-api-0.0.2.jar ./safenet-auth-api.jar
COPY config/linux.ini ./config/config.ini
COPY tools/keygen-1.0.jar ./tools/keygen.jar
COPY begin.sh .
RUN chmod 700 /app/begin.sh && \
    chmod 700 /app/secret && \
    chmod 700 /app/tools && \
    chmod 700 /app/config 
RUN chmod 600 /app/tools/keygen.jar && \
    chmod 600 /app/config/config.ini && \
    chmod 600 /app/safenet-auth-api.jar

# Create the custom log directory and change ownership and permissions
RUN mkdir -p /var/log/microservice && \
    chown appuser:appuser /var/log/microservice && \
    chmod 755 /var/log/microservice

# Change the ownership of the /app directory to the non-root user
RUN chown -R appuser:appuser /app && \
    chmod 700 /app

# Set the user to run the container as
USER appuser

# Set the entrypoint

ENTRYPOINT [ "/app/begin.sh" ]
CMD ["java", "-jar", "/app/safenet-auth-api.jar"]

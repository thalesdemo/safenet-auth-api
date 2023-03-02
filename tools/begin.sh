#!/usr/bin/env bash

# Phase 1: Create or register the api key
DOCKER_API_CLIENT_KEY_PATH="/app/secret/client.key"
DOCKER_API_SERVER_KEY_PATH="/app/secret/server.key"
TOOL_LOCATION="/app/tools/keygen.jar"

if [ ! -n "$API_KEY_HASH" ]; then   
    if [ -f "$DOCKER_API_SERVER_KEY_PATH" ]; then
        source "$DOCKER_API_SERVER_KEY_PATH"
        echo -e "\033[34m[start.sh] API_KEY_HASH exists in configuration.\e[0m"
        if [ -f "$DOCKER_API_CLIENT_KEY_PATH" ]; then
            rm -f "$DOCKER_API_CLIENT_KEY_PATH"
            echo -e "\033[34m[start.sh] Removed file secret $DOCKER_API_CLIENT_KEY_PATH.\e[0m"
        fi

    else
        echo -e "\e[33m[start.sh] API_KEY_HASH does not exist in environment or in file configuration. Automatically generating keypair...\e[0m"

        json=$(java -jar $TOOL_LOCATION)
        api_key=$(echo "$json" | jq -r '.apiKey')
        api_key_hash=$(echo "$json" | jq -r '.apiKeyHash')
        echo $api_key > "$DOCKER_API_CLIENT_KEY_PATH"
        chmod 600 "$DOCKER_API_CLIENT_KEY_PATH"

        echo -e "\e[1;31m[start.sh] For security reasons, retrieve the API client key from within the container, then restart the container (OR manually delete the client.key file)\e[0m"
        echo -e "\e[33m\t\t Example:\n\t\t\t\e[33mdocker exec safenet-auth-api cat '$DOCKER_API_CLIENT_KEY_PATH'\n\t\t\t\e[33mdocker exec safenet-auth-api rm '$DOCKER_API_CLIENT_KEY_PATH'\e[0m"

        echo "export API_KEY_HASH='$api_key_hash'" > "$DOCKER_API_SERVER_KEY_PATH"
        chmod 600 "$DOCKER_API_SERVER_KEY_PATH"
        source "$DOCKER_API_SERVER_KEY_PATH"
    fi

else
    echo -e "\033[34m[start.sh] Loading API_KEY_HASH defined from environment variable.\e[0m"
fi

# Phase 2: Set primary URL in config.ini based on environment variable
sed -i "/PrimaryServer=/c\PrimaryServer=$SAFENET_SERVER_HOST" $JCRYPTO_INI_PATH

# Phase 3: Set log level in config.ini based on environment variable
if [ "$API_LOG_LEVEL" = "DEBUG" ]; then
    sed -i "/LogLevel=/c\LogLevel=5" $JCRYPTO_INI_PATH
else
    sed -i "/LogLevel=/c\LogLevel=3" $JCRYPTO_INI_PATH
fi

# Phase 4: Launch tomcat web service
exec "$@"

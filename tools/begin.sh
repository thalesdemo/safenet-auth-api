#!/usr/bin/env bash

# Phase 1: Create or register the api key
DOCKER_API_CLIENT_KEY_PATH="/app/secret/client.key"
DOCKER_API_SERVER_KEY_PATH="/app/secret/server.key"
TOOL_LOCATION="/app/tools/keygen.jar"

printf '%.0s=' {1..100}
echo -e "\n"

function generate_api_key() {
  json=$(java -jar $TOOL_LOCATION)
  api_key=$(echo "$json" | jq -r '.apiKey')
  api_key_hash=$(echo "$json" | jq -r '.apiKeyHash')
  echo $api_key >"$DOCKER_API_CLIENT_KEY_PATH"
  chmod 600 "$DOCKER_API_CLIENT_KEY_PATH"
  echo
  echo -e "\e[1;32m\tHello and welcome to the SafeNet RESTful Authentication Gateway! \e[0m"
  echo -e "\n\t\e[32mThis is your first time launching the app, so you'll need to retrieve an API client key. \e[0m"
  echo -e "\e[32m\n\tHere's how: \e[0m"
  echo -e "\e[33m\n\t     Step 1: \e[0m\e[32mOpen a new terminal window\e[0m"
  echo -e "\e[33m\n\t     Step 2: \e[0m\e[32mIn the new terminal window, run the following commands: \e[0m"
  echo -e "\e[33m\n\t         docker exec safenet-auth-api cat '$DOCKER_API_CLIENT_KEY_PATH' \e[0m"
  echo -e "\e[33m\n\t         docker exec safenet-auth-api rm '$DOCKER_API_CLIENT_KEY_PATH' \e[0m"
  echo -e "\n\e[33m\tWARNING: > Do not kill the 'docker-compose up' session in the original terminal window, as the \e[0m"
  echo -e "\e[33m\t           API server will stop running.\e[0m"
  echo -e "\e[33m\t         > In all cases, the container will automatically delete the client key when it restarts.\e[0m"
  echo "export API_KEY_HASH='$api_key_hash'" >"$DOCKER_API_SERVER_KEY_PATH"
  chmod 600 "$DOCKER_API_SERVER_KEY_PATH"
  source "$DOCKER_API_SERVER_KEY_PATH"
}

if [ ! -n "$API_KEY_HASH" ]; then
  if [ -f "$DOCKER_API_SERVER_KEY_PATH" ]; then
    source "$DOCKER_API_SERVER_KEY_PATH"
    echo -e "\033[34m[KEY] API_KEY_HASH exists in configuration.\e[0m"
    if [ -f "$DOCKER_API_CLIENT_KEY_PATH" ]; then
      rm -f "$DOCKER_API_CLIENT_KEY_PATH"
      echo -e "\033[34m[KEY] Removed file secret $DOCKER_API_CLIENT_KEY_PATH.\e[0m"
    fi
  else
    echo -e "\e[33m[KEY] API_KEY_HASH does not exist in environment or in configuration. Generating keypair...\e[0m"
    generate_api_key
  fi
else
  echo -e "\033[34m[KEY] Loading API_KEY_HASH defined from environment variable.\e[0m"
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

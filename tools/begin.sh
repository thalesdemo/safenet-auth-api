#!/usr/bin/env bash

# Phase 1: Create or register the api key
DOCKER_API_CLIENT_KEY_PATH="/app/secret/client.key"
DOCKER_API_SERVER_KEY_PATH="/app/secret/server.key"
TOOL_LOCATION="/app/tools/keygen.jar"

printf '%.0s=' {1..80}
echo -e "\n"

function generate_api_key() {
  json=$(java -jar $TOOL_LOCATION)
  api_key=$(echo "$json" | jq -r '.apiKey')
  api_key_hash=$(echo "$json" | jq -r '.apiKeyHash')
  echo $api_key >"$DOCKER_API_CLIENT_KEY_PATH"
  chmod 600 "$DOCKER_API_CLIENT_KEY_PATH"
  echo
  echo -e "\n\e[1;32m\t          Welcome to the SafeNet RESTful Authentication Gateway \e[0m"
  echo -e "\n\n\t\e[32mThis is your first time launching the app, so you'll need to retrieve an\e[0m"
  echo -e "\t\e[32mAPI client key to be used in the \`X-API-Key\` header for authorization. \n\e[0m"
  echo -e "\e[32m\n\tHere's how: \e[0m"
  echo -e "\e[33m\n\t     Step 1: \e[0m\e[32mOpen a new terminal window\e[0m"
  echo -e "\e[33m\n\t     Step 2: \e[0m\e[32mIn the new terminal window, run the following commands: \e[0m"
  echo -e "\n\e[0m\t        docker exec safenet-auth-api cat '$DOCKER_API_CLIENT_KEY_PATH' \e[0m"
  echo -e "\n\e[0m\t        docker exec safenet-auth-api rm '$DOCKER_API_CLIENT_KEY_PATH' \e[0m"
  echo -e "\n\n\e[33m\tWARNING: \e[0m\n"
  echo -e "\e[33m\t    > Do not kill the 'docker-compose up' session in the original \e[0m"
  echo -e "\e[33m\t      terminal window, as the API server will stop running.\e[0m"
  echo -e "\n\e[33m\t    > The container will delete any clear-text client key file found\e[0m"
  echo -e "\e[33m\t      upon restart, since it only requires the hashed value of this\e[0m"
  echo -e "\e[33m\t      key. The hash is stored, by default, in server.key. \e[32mIt could \e[0m"
  echo -e "\e[32m\t      otherwise be defined in the \`API_KEY_HASH\` environment variable.\e[0m"
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
    echo -e "\e[33m[KEY] API_KEY_HASH does not exist in environment or in configuration.\e[0m"
    echo -e "\e[33m      > Generating keypair ...\e[0m\n"
    printf '%.0s=' {1..80}
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

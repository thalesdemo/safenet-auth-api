#!/usr/bin/env bash

DOCKER_API_CLIENT_KEY_PATH="/app/secret/client.key"
DOCKER_API_SERVER_KEY_PATH="/app/secret/server.key"
TOOL_LOCATION="/app/tools/keygen.jar"

function print_welcome_message() {
    local message=$(cat << "EOF" 


\e[1;32m             Welcome to the SafeNet RESTful Authentication Gateway


\e[0m\e[32m  This is your first time launching the app, so you'll need to retrieve an
\e[0m\e[32m  API client key to use in the `X-API-Key` header for authorization. 


\e[0m\e[32m  Here's how:

\e[1;33m    Step 1: \e[0m\e[32mOpen a new terminal window

\e[1;33m    Step 2: \e[0m\e[32mIn the new terminal window, run the following commands: 
\e[0m
        docker exec safenet-auth-api cat $DOCKER_API_CLIENT_KEY_PATH

        docker exec safenet-auth-api rm $DOCKER_API_CLIENT_KEY_PATH


\e[33m  WARNING: 

\e[33m   > Do not kill the 'docker-compose up' session in the original terminal
\e[33m     window, as the API server will stop running.

\e[33m   > The container will delete any clear-text client key file found upon
\e[33m     restart, since it only requires the hashed value of this key. The hash
\e[33m     is stored, by default, in server.key. \e[32mIt could otherwise be defined
\e[32m     in the `API_KEY_HASH` environment variable.\e[0m
EOF
)
    echo -e "$message" | sed -e "s|\$DOCKER_API_CLIENT_KEY_PATH|$DOCKER_API_CLIENT_KEY_PATH|g"

}

function generate_api_key() {
  print_welcome_message
  json=$(java -jar $TOOL_LOCATION)
  api_key=$(echo "$json" | jq -r '.apiKey')
  api_key_hash=$(echo "$json" | jq -r '.apiKeyHash')
  echo $api_key >"$DOCKER_API_CLIENT_KEY_PATH"
  chmod 600 "$DOCKER_API_CLIENT_KEY_PATH"
  echo "export API_KEY_HASH='$api_key_hash'" >"$DOCKER_API_SERVER_KEY_PATH"
  chmod 600 "$DOCKER_API_SERVER_KEY_PATH"
  source "$DOCKER_API_SERVER_KEY_PATH"

}

# Script starts here
printf '%.0s=' {1..80}
echo -e "\n"

# Phase 1: Create or register the api key
if [ ! -n "$API_KEY_HASH" ]; then
  if [ -f "$DOCKER_API_SERVER_KEY_PATH" ]; then
    source "$DOCKER_API_SERVER_KEY_PATH"
    echo -e "\033[34m [KEY] API_KEY_HASH exists in configuration.\e[0m"
    if [ -f "$DOCKER_API_CLIENT_KEY_PATH" ]; then
      rm -f "$DOCKER_API_CLIENT_KEY_PATH"
      echo -e "\033[34m       > Removed file secret $DOCKER_API_CLIENT_KEY_PATH.\e[0m"
    fi
  else
    echo -e "\e[33m [KEY] API_KEY_HASH does not exist in environment or in configuration.\e[0m"
    echo -e "\e[33m       > Generating keypair ...\e[0m\n"
    printf '%.0s=' {1..80}
    generate_api_key
  fi
else
  echo -e "\033[34m [KEY] Loading API_KEY_HASH defined from environment variable.\e[0m"
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

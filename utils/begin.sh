#!/bin/sh

# TODO: Rework the first-time initialization script given the new application architecture
#       and dependence on a volume-mounted configuration file (application.yaml)
#
# NOTE: 
#     - Up to version 0.0.5 of the SafeNet RESTful Authentication Gateway, the API key
#       was generated and stored in a secret file. This is no longer necessary, as the
#       API key is now stored in the application.yaml file. The script below will be
#       reworked to reflect the new architecture.
#
#     - First-time initialization will be handled externally to the container, and the
#       API key will be stored in the application.yaml file.

################## START OF DEPRECATED CODE ##################
# DOCKER_API_CLIENT_KEY_PATH="/app/secret/client.key"
# DOCKER_API_SERVER_KEY_PATH="/app/secret/server.key"
# TOOL_LOCATION="/app/utils/keygen.jar"

# function print_welcome_message() {
#     local message=$(cat << "EOF" 


# \e[1;32m             Welcome to the SafeNet RESTful Authentication Gateway


# \e[0m\e[32m  This is your first time launching the app, so you'll need to retrieve an
# \e[0m\e[32m  API client key to use in the `X-API-Key` header for authorization. 


# \e[0m\e[32m  Here's how:

# \e[1;33m    Step 1: \e[0m\e[32mOpen a new terminal window

# \e[1;33m    Step 2: \e[0m\e[32mIn the new terminal window, run the following commands: 
# \e[0m
#         docker exec safenet-auth-api cat $DOCKER_API_CLIENT_KEY_PATH

#         docker exec safenet-auth-api rm $DOCKER_API_CLIENT_KEY_PATH


# \e[33m  WARNING: 

# \e[33m   > Do not kill the 'docker-compose up' session in the original terminal
# \e[33m     window, as the API server will stop running.

# \e[33m   > The container will delete any clear-text client key file found upon
# \e[33m     restart, since it only requires the hashed value of this key. The hash
# \e[33m     is stored, by default, in server.key. \e[32mIt could otherwise be defined
# \e[32m     in the `API_KEY_HASH` environment variable.\e[0m
# EOF
# )
#     echo -e "$message" | sed -e "s|\$DOCKER_API_CLIENT_KEY_PATH|$DOCKER_API_CLIENT_KEY_PATH|g"

# }

# function generate_api_key() {
#   print_welcome_message
#   json=$(java -jar $TOOL_LOCATION)
#   api_key=$(echo "$json" | jq -r '.apiKey')
#   api_key_hash=$(echo "$json" | jq -r '.apiKeyHash')
#   echo $api_key >"$DOCKER_API_CLIENT_KEY_PATH"
#   chmod 600 "$DOCKER_API_CLIENT_KEY_PATH"
#   echo "export API_KEY_HASH='$api_key_hash'" >"$DOCKER_API_SERVER_KEY_PATH"
#   chmod 600 "$DOCKER_API_SERVER_KEY_PATH"
#   source "$DOCKER_API_SERVER_KEY_PATH"

# }

# Script starts here
# printf '%.0s=' {1..80}
# echo -e "\n"

# Phase 1: Create or register the api key
# if [ ! -n "$API_KEY_HASH" ]; then
#   if [ -f "$DOCKER_API_SERVER_KEY_PATH" ]; then
#     source "$DOCKER_API_SERVER_KEY_PATH"
#     echo -e "\033[34m [KEY] API_KEY_HASH exists in configuration.\e[0m"
#     if [ -f "$DOCKER_API_CLIENT_KEY_PATH" ]; then
#       rm -f "$DOCKER_API_CLIENT_KEY_PATH"
#       echo -e "\033[34m       > Removed file secret $DOCKER_API_CLIENT_KEY_PATH.\e[0m"
#     fi
#   else
#     echo -e "\e[33m [KEY] API_KEY_HASH does not exist in environment or in configuration.\e[0m"
#     echo -e "\e[33m       > Generating keypair ...\e[0m\n"
#     printf '%.0s=' {1..80}
#     generate_api_key
#   fi
# else
#   echo -e "\033[34m [KEY] Loading API_KEY_HASH defined from environment variable.\e[0m"
# fi
################## END OF DEPRECATED CODE ##################

# Phase 1: Set primary URL in config.ini based on environment variable
# Ensure SAFENET_PRIMARY_AUTH_URL is not empty or null and follows URL format
if [[ -z "$SAFENET_PRIMARY_AUTH_URL" ]]; then
    echo "Error: Environment variable 'SAFENET_PRIMARY_AUTH_URL' is empty or null (App failed to start)"
    exit 1
fi

if ! [[ "$SAFENET_PRIMARY_AUTH_URL" =~ ^(http|https):// ]]; then
    echo "Error: Environment variable 'SAFENET_PRIMARY_AUTH_URL' is not in the correct URL format (App failed to start)"
    exit 1
fi

# TokenValidator Parameters
protocol="${SAFENET_PRIMARY_AUTH_URL%%://*}"
full_path="${SAFENET_PRIMARY_AUTH_URL#*://}"

# Check if a port is specified in the base_url
if [[ "$full_path" =~ :[0-9]+ ]]; then
    # Extract port
    safenet_port="${full_path%%/*}"    # Extract everything before the first "/"
    safenet_port="${safenet_port##*:}" # Extract everything after the last ":"

    # Extract FQDN without port
    fqdn="${full_path%%:*}"
else
    fqdn="${full_path%%/*}"
    if [[ "$protocol" == "https" ]]; then
        safenet_port=443
    else
        safenet_port=80
    fi
fi

# Extract path
path="${full_path#$fqdn}"
path="${path#:$safenet_port}"

# Set the values in the config.ini file
sed -i "/PrimaryServer=/c\PrimaryServer=$fqdn" $JCRYPTO_INI_PATH
sed -i "/PrimaryServerPort=/c\PrimaryServerPort=$safenet_port" $JCRYPTO_INI_PATH
sed -i "/PrimaryProtocol=/c\PrimaryProtocol=$protocol" $JCRYPTO_INI_PATH
sed -i "/PrimaryWebServiceRelativePath=/c\PrimaryWebServiceRelativePath=$path" $JCRYPTO_INI_PATH

# Debug: Output the modified config.ini file for verification
cat $JCRYPTO_INI_PATH

# Phase 2: Set log level in config.ini based on environment variable
if [ "$API_LOG_LEVEL" = "DEBUG" ]; then
  sed -i "/LogLevel=/c\LogLevel=5" $JCRYPTO_INI_PATH
else
  sed -i "/LogLevel=/c\LogLevel=3" $JCRYPTO_INI_PATH
fi

# Phase 3: Launch tomcat web service
exec "$@"


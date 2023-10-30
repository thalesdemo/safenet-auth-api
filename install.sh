#!/bin/bash

# Exit immediately if a command exits with a non-zero status
set -e

# Associative array to store user input values
declare -A user_inputs

# Function to ensure a directory exists
ensure_dir_exists() {
    local dir_name=$1
    if [[ ! -d "$dir_name" ]]; then
        mkdir -p "$dir_name"
    fi
}

# Function to check if Java 11 is installed
check_java_version() {
    if ! command -v java &>/dev/null; then
        echo "Java is not installed."
        prompt_install_java
        return
    fi

    local java_version
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    if [[ "$java_version" == 11.* ]]; then
        echo "Java 11 is installed."
        return
    fi

    if alternatives --list | grep -q "java-11"; then
        echo "Java 11 is installed but not set as the default."
        echo "You can set it as default using 'alternatives --config java'."
        return
    fi

    echo "Java 11 is not installed."
    prompt_install_java
}

# Function to prompt the user to install Java 11 using yum
prompt_install_java() {
    read -p "Do you want to install Java 11 using yum? [y/N]: " install
    if [[ "$install" =~ ^[Yy]$ ]]; then
        sudo yum install java-11-openjdk
    else
        echo "Please ensure Java 11 is installed before proceeding."
        exit 1
    fi
}

# Function to download files from GitHub
download_to_dir() {
    local file_path=$1
    local destination=$2

    if [[ -e "$destination" ]]; then
        read -p "$destination already exists. Do you want to overwrite? [y/N]: " overwrite
        if [[ ! "$overwrite" =~ ^[Yy]$ ]]; then
            echo "Skipping download of $destination"
            return
        fi
    fi

    curl -o "$destination" "$file_path"
}

# Function to prompt the user for input
prompt_user() {
    local prompt_message=$1
    local default_value=$2

    # Check if a previous value exists
    if [[ -n $default_value ]] && [[ -n "${user_inputs[$default_value]}" ]]; then
        echo "${user_inputs[$default_value]}"
        return
    fi

    read -p "$prompt_message (Default: $default_value): " input_value

    # If input is empty, use the default value
    if [[ -z "$input_value" ]]; then
        input_value=$default_value
    fi

    if [[ -z "$input_value" ]]; then
        echo "\"\""
        return
    fi

    # Store the user input value if it's not empty
    if [[ -n $input_value && -n $default_value ]]; then
        user_inputs["$default_value"]=$input_value
    fi

    echo "$input_value"
}

# Function to replace markers in files
replace_marker() {
    local file=$1
    local marker=$2
    local replacement_value=$3

    sed -i "s|$marker|$replacement_value|g" "$file"
}

# Start the main script
echo "Checking Java installation..."
check_java_version

# Get installation directory
read -p "Enter the installation directory (Default is the current directory): " install_dir
[[ -z "$install_dir" ]] && install_dir=$(pwd)

# Ensure the config directory exists
ensure_dir_exists "$install_dir/config"

# Ensure the log folder directory exists
ensure_dir_exists "$install_dir/logs"

# Download necessary files
download_to_dir "https://raw.githubusercontent.com/thalesdemo/safenet-auth-api/alternate/jar/safenet-auth-api-0.0.6-alt.jar" "$install_dir/safenet-auth-api-0.0.6-alt.jar"
download_to_dir "https://raw.githubusercontent.com/thalesdemo/safenet-auth-api/alternate/config/application.yaml" "$install_dir/config/application.yaml"
download_to_dir "https://raw.githubusercontent.com/thalesdemo/safenet-auth-api/alternate/config/linux.ini" "$install_dir/config/linux.ini"
download_to_dir "https://raw.githubusercontent.com/thalesdemo/safenet-auth-api/alternate/tools/keygen-1.0.jar" "$install_dir/keygen-1.0.jar"
download_to_dir "https://raw.githubusercontent.com/thalesdemo/safenet-auth-api/alternate/tools/EncryptionUtility.java" "$install_dir/EncryptionUtility.java"

# Prompt user for configurations
base_url=$(prompt_user "Enter the base URL for SafeNet" "https://cloud.us.safenetid.com")
virtual_server_name=$(prompt_user "Enter the virtual server name" "<safenet-virtual-server-name>")
logging_level=$(prompt_user "Enter the logging level" "INFO")
# Extract the protocol, FQDN, port, and path from the base_url
protocol="${base_url%%://*}"
full_path="${base_url#*://}"

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

# Replace markers in application.yaml
replace_marker "$install_dir/config/application.yaml" "<safenet-base-url>" "$base_url"
replace_marker "$install_dir/config/application.yaml" "<safenet-virtual-server-name>" "$virtual_server_name"
replace_marker "$install_dir/config/application.yaml" "<application-logging-level>" "$logging_level"

# Replace markers in linux.ini using the base_url
replace_marker "$install_dir/config/linux.ini" "<safenet-fqdn>" "$fqdn"
replace_marker "$install_dir/config/linux.ini" "<safenet-protocol>" "$protocol"
replace_marker "$install_dir/config/linux.ini" "<safenet-port>" "$safenet_port"
replace_marker "$install_dir/config/linux.ini" "<safenet-relative-path>" "$path"

# Prompt user for the path to Agent.bsidkey file
while true; do
    default_bsidkey_path="$(pwd)/Agent.bsidkey"
    safenet_bsidkey_path=$(prompt_user "Enter the path to the Agent.bsidkey file" "$default_bsidkey_path")

    # Convert relative path to absolute path
    if [[ "$safenet_bsidkey_path" == ./* ]]; then
        safenet_bsidkey_path="$(pwd)${safenet_bsidkey_path:1}"
    fi

    # Check if the given file path exists
    if [[ -f "$safenet_bsidkey_path" ]]; then
        echo "File exists: $safenet_bsidkey_path"
        break
    else
        echo "Error: File does not exist. Please provide a valid path."
    fi
done

# Replace the marker in linux.ini with the provided file path
replace_marker "$install_dir/config/linux.ini" "<safenet-bsidkey-file-path>" "$safenet_bsidkey_path"

# Set agent logs path
replace_marker "$install_dir/config/linux.ini" "<install-dir>" "$install_dir"

# Set install dir for configs in application.yaml
replace_marker "$install_dir/config/application.yaml" "<install-dir>" "$install_dir"

### SSL/PORT

# Prompt user for configurations
application_port=$(prompt_user "Enter the application port number" "8080")
enable_ssl=$(prompt_user "Do you want to enable SSL? (true/false)" "false")

if [[ "$enable_ssl" == "true" ]]; then
    ssl_format=$(prompt_user "Do you have a PKCS12 file or PEM files? (pkcs12/pem)" "pkcs12")
    if [[ "$ssl_format" == "pkcs12" ]]; then
        pkcs12_path=$(prompt_user "Enter the path to the PKCS12 file" "$install_dir/keystore.p12")
        echo "OK for path"
        keystore_password=$(prompt_user "Enter the keystore password" "")
        key_alias=$(prompt_user "Enter the key alias" "tomcat")
    else
        fullchain_path=$(prompt_user "Enter the path to the fullchain.pem file" "")
        privkey_path=$(prompt_user "Enter the path to the privkey.pem file" "")
        # Generate PKCS12
        openssl pkcs12 -export -in "$fullchain_path" -inkey "$privkey_path" -out "$install_dir/keystore.p12" -name tomcat
        pkcs12_path="$install_dir/keystore.p12"
        keystore_password=$(prompt_user "Enter a password for the new PKCS12 keystore" "")
        key_alias="tomcat"
    fi
else
    pkcs12_path="<path-to-pkcs12-file>"
    keystore_password="<encrypted-key-store-password>"
    key_alias="<key-alias>"
fi

# Replace markers in application.yaml
replace_marker "$install_dir/config/application.yaml" "<application-port-number>" "$application_port"
replace_marker "$install_dir/config/application.yaml" "<application-enable-ssl>" "$enable_ssl"
replace_marker "$install_dir/config/application.yaml" "<path-to-pkcs12-file>" "$pkcs12_path"
replace_marker "$install_dir/config/application.yaml" "<key-alias>" "$key_alias"

#### BSIDKEY

# Run the Java command and capture its output
keygen_output=$(java -jar $install_dir/keygen-1.0.jar)

# Extract apiKey and apiKeyHash from the output
apiKey=$(echo "$keygen_output" | grep -oP '"apiKey":"\K[^"]+')
apiKeyHash=$(echo "$keygen_output" | grep -oP '"apiKeyHash":"\K[^"]+')

# Replace the marker in linux.ini with the apiKeyHash
replace_marker "$install_dir/config/application.yaml" "<safenet-api-key-hash>" "$apiKeyHash"

# Encryption part

# Encryption utility function
encrypt() {
    local key=$1
    local plaintext=$2
    echo $(java "$install_dir/EncryptionUtility.java" encrypt "$key" "$plaintext")
}

# Function to mask password input with asterisks
read_password() {
    echo -n "Enter the operator password (will be hidden): "
    #stty -echo
    read operator_password
    #stty echo
    echo
}

# Generate the encryption key (you'd typically store and reuse this key, but for simplicity, we're generating it here)
encryption_key=$(java "$install_dir/EncryptionUtility.java" generate)

# Prompt user for email using prompt_user
operator_email=$(prompt_user "Enter the operator email" "operator@example.com")

# Prompt user for password using read_password
operator_password=$(prompt_user "Enter the operator password" "password")

# Encrypt the email and password
encrypted_email=$(encrypt "$encryption_key" "$operator_email")
encrypted_password=$(encrypt "$encryption_key" "$operator_password")

# Encrypt the keystore password
encrypted_keystore_password=$(encrypt "$encryption_key" "$keystore_password")

# Replace encrypted markers in application.yaml
replace_marker "$install_dir/config/application.yaml" "<safenet_encrypted_operator_email>" "$encrypted_email"
replace_marker "$install_dir/config/application.yaml" "<safenet_encrypted_operator_password>" "$encrypted_password"
replace_marker "$install_dir/config/application.yaml" "<encrypted-key-store-password>" "$encrypted_keystore_password"

# Export the encryption key for the current session and instruct the user to do so for future sessions
echo
echo "1. For the gateway to function, ensure you set the environment variable for the encryption key:"
echo "export ENCRYPTION_SECRET_KEY=$encryption_key"
echo
echo Or run for example java with:
echo sudo -u root -E ENCRYPTION_SECRET_KEY=$encryption_key java -jar safenet-auth-api-0.0.6-alt.jar
echo

# Print apiKey to the screen
echo "2. Generated apiKey (X-API-Key Header): $apiKey"

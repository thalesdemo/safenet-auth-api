## Table of Contents
-   [Introduction](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#introduction)
-   [Requirements](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#requirements)
-   [Instructions](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#instructions)
    -   [Download Configuration File](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#step-1-download-the-configuration-file)
    -   [Generate API Key](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#step-2-generate-api-key)
    -   [Modify Configuration File](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#step-3-modify-the-ini-configuration-file)
    -   [Install Dependencies (Windows Only)](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#step-4-install-dependencies-windows-only)
    -   [Run JAR File](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar#step-5-run-the-jar-file)

## Introduction

This document provides instructions for using the SafeNet RESTful Authentication Gateway to authenticate users with a SafeNet Trusted Access (STA) or SafeNet Authentication Service (SAS-PCE) account. The SafeNet RESTful Authentication Gateway is a Spring project that provides a RESTful API for client applications to interact with the SafeNet authentication service and perform user authentication. The instructions in this document focus on using the JAR file directly, rather than the Docker image.

## Requirements

Before you can use the SafeNet Java Authentication API, you will need the following:

-   Java runtime environment (JRE) version 11 or higher installed on your system.
-   A SafeNet Trusted Access (STA) or SafeNet Authentication Service (SAS-PCE) account to download your tenant key `Agent.bsidkey`.

Note: If you are a Windows user, you may need to download and install additional dependencies for the SafeNet Java Authentication API.

## Instructions

To use the SafeNet Authentication API directly from the JAR file, follow these steps:

### Step 1: Download the Configuration File

Download the configuration file for your operating system:
-   Linux/Mac OS: [linux.ini](https://github.com/thalesdemo/safenet-auth-api/raw/main/config/linux.ini)
-   Windows: [windows.ini](https://github.com/thalesdemo/safenet-auth-api/raw/main/config/windows.ini)


### Step 2: Generate an API Key
Generate an API key for your client application:
    
-   Option 1: Run our [keygen-1.0.jar](https://github.com/thalesdemo/safenet-auth-api/blob/main/tools/keygen-1.0.jar) tool using the command line:
    
**Output example:**
```
[user@linux tools]$ java -jar keygen-1.0.jar 
{
    "apiKey":"0sMj_ylf-TVtkbn3E-kEPEXJ2e-2VJSBDiS-lBEq1fCQ-OtbPytek",
    "apiKeyHash":"$2a$10$eOSUL4ULDPPd/qXFxMmnOeFlRLgua5XWJQ8INmlnKk7A0JNemDKoi"
}
```
      
 -   Option 2: Write your own Java code using `BCryptPasswordEncoder`:
   ``` 
     import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

     public class ApiKeyGenerator {     
    	 public static void main(String[] args) {         
     		 String clientKey = "MySecureApiKey2023!";         
    	 	 BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();        
    		 String serverKey = bcrypt.encode(clientKey);         
    		 System.out.println("Client Key: " + clientKey);         
    		 System.out.println("Server Key: " + serverKey);         
    	 } 
     }
```
>    **Note:** Replace `MySecureApiKey2023!` with your desired client API key value. Running this Java code will output the server key (i.e., `X_API_KEY_HASH`) that you can use with the SafeNet Authentication API server JAR or Docker image.

 
### Step 3: Modify the INI Configuration File

Modify the INI configuration file you downloaded in step 1 to update the following fields:
    
-   `EncryptionKeyFile`: The SafeNet Agent.bsidkey file retrieved from your virtual server. See the screenshot below for an example. 

![Screenshot of EncryptionKeyFile field](https://github.com/thalesdemo/safenet-auth-api/raw/main/image/jar/screenshot1.png)
        
        
-   `PrimaryServer`: The hostname of the SafeNet TokenValidator server for your Cloud service zone (or your own hostname for the SAS-PCE edition). See the screenshot below for an example. 

![Screenshot of PrimaryServer field](https://github.com/thalesdemo/safenet-auth-api/raw/main/image/jar/screenshot2.png)
        
        
-   `LogFile`: The file path for the SafeNet Java API logs. See the screenshot below for an example. 

![Screenshot of LogFile field](https://github.com/thalesdemo/safenet-auth-api/raw/main/image/jar/screenshot3.png)
        
        
### Step 4: Install Dependencies (Windows Only)

If you are a Windows user, download the SafeNet Java Authentication API 1.3.0 installation package from the support portal, then install it to get the necessary Windows dependencies (such as CryptoCOM.dll).
    
### Step 5: Run the JAR File

Run the JAR file with the following command:
    
    java -jar -DAPI_LOG_LEVEL=INFO -DAPI_SERVER_PORT=8888 -DJCRYPTO_INI_PATH=./linux.ini -DAPI_KEY_HASH='$2a$10$eOSUL4ULDPPd/qXFxMmnOeFlRLgua5XWJQ8INmlnKk7A0JNemDKoi' safenet-auth-api-0.0.2.jar


This command sets several environment variables that are used by the Java application:
-   `API_LOG_LEVEL`: The logging level for the Java application (set to INFO in this example).
-   `API_SERVER_PORT`: The port number for the Java application server (set to 8888 in this example).
-   `JCRYPTO_INI_PATH`: The file path for the modified INI configuration file (set to `./linux.ini` in this example).
-   `API_KEY_HASH`: The hashed API key value for the SafeNet authentication service (set to `$2a$10$eOSUL4ULDPPd/qXFxMmnOeFlRLgua5XWJQ8INmlnKk7A0JNemDKoi` in this example).
-   `safenet-auth-api-0.0.2.jar`: The filename of the JAR file to run.


## Conclusion

In this guide, we have provided instructions for installing and running the SafeNet Authentication API using the JAR file. By following these steps, you can easily set up and deploy a RESTful authentication gateway that enables secure user authentication and authorization for applications that interact with the SafeNet authentication service.

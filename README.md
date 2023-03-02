<h1>SafeNet RESTful Authentication Gateway</h1>

🚀 Get Started with Our SafeNet RESTful Authentication Gateway on [Docker Hub](https://hub.docker.com/r/thalesdemo/safenet-auth-api)!

💻 Install the SafeNet Authentication API using our JAR file! Check out the [jar folder](https://github.com/thalesdemo/safenet-auth-api/tree/main/jar) in our repository for step-by-step instructions.

<h2>Important Notice</h2>

Thank you for your interest in our demo project! It is important to note that the project is currently under active development. <b>As a result, we kindly request that you use the project at your own discretion and assume any potential risks associated with its use</b>.

<h2>Summary</h2>

This microservice is a REST-based solution that allows for secure and efficient authentication against SafeNet authentication servers. It provides simple and easy-to-use endpoints that handle user authentication and credential validation. With advanced security features such as GRID or OTP authentication,  the SafeNet RESTful Authentication Gateway ensures secure and auditable authentication transactions, making it an ideal solution for any application or service that requires secure user authentication.

<h2>Deployment Guidelines</h2>

To execute the Docker image successfully, it is essential to create a dotenv file in your working directory named `.env` and input the necessary information below. No modifications are necessary to the `docker-compose.yml` file, and it should be stored in the same directory as the dotenv file.

In order to operate this image, it is imperative to have your `Agent.bsidkey` SafeNet encryption key readily available.

Please keep in mind that the authentication agent underlying this image depends on the `SafeNet TokenValidator endpoints`. To ensure uninterrupted functionality, it is important to whitelist your public-facing IP address(es) in the `Auth Nodes` section of the SafeNet management console.

<h2>Configuration Files</h2>

<b>.env</b>
```text
SAFENET_SERVER_HOST=cloud.us.safenetid.com
HOST_AGENT_KEY_PATH=/host/path/to/your/tenant/Agent.bsidkey
API_SERVER_PORT=8888
API_LOG_LEVEL=INFO
```


<b>docker-compose.yml</b>
```yaml
version: '3'

services:
  safenet-auth-api:
    image: thalesdemo/safenet-auth-api
    container_name: safenet-auth-api
    environment:
      HOST_AGENT_KEY_PATH: ${HOST_AGENT_KEY_PATH}
      SAFENET_SERVER_HOST: ${SAFENET_SERVER_HOST}
      API_SERVER_PORT: ${API_SERVER_PORT}
      API_LOG_LEVEL: ${API_LOG_LEVEL}
      API_KEY_HASH: ${API_KEY_HASH-}
      JCRYPTO_INI_PATH: /app/config/config.ini
    volumes:
      - type: bind
        source: ${HOST_AGENT_KEY_PATH}
        target: /app/secret/agent.key
        read_only: true
    ports:
      - ${API_SERVER_PORT}:${API_SERVER_PORT}

```

<h2>Starting the Docker Container</h2>

When you are ready, go to your working directory and run the Docker by typing:
```
docker-compose up
```
Then, follow the instructions that appear on the screen to get your unique client header key. You must supply this key in the `X-API-Key` header for every HTTP request.

<h2>API Reference</h2>

To view the API documentation and all available endpoints, open your web browser and go to `http://localhost:8888`.

![example.gif](https://github.com/thalesdemo/safenet-auth-api/blob/main/example.gif)

<h2>Contact Us</h2>
If you have any feedback to share or would like to request new features, please feel free to reach out to us at <a href="mailto:hello@onewelco.me">hello@onewelco.me</a>. We welcome your input!

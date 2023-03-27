package com.thalesdemo.safenet.auth.api;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {"jcrypto.iniPath=C:\\safenet_javaapi\\JCryptoWrapper.ini"})
@TestPropertySource(properties = {"API_SERVER_PORT=9999"})
@TestPropertySource(properties = {"API_KEY_HASH=1234"})
@TestPropertySource(properties = {"API_LOG_LEVEL=DEBUG"})
@TestPropertySource(properties = {"HOST_AGENT_KEY_PATH=C:\\agent.key"})

@SpringBootTest
class ApplicationTests {

    @BeforeAll
    public static void setup() {
        System.setProperty("JCRYPTO_INI_PATH", "C:/safenet_javaapi/JCryptoWrapper.ini");
    }
    
	@Test
	void contextLoads() {
	}

}

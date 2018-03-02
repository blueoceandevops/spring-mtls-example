package com.basaki.server.config;

import com.basaki.server.ServerApplication;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.config.SSLConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static io.restassured.RestAssured.given;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

/**
 * {@code SwaggerConfigurationIntegrationTests} represents functional tests for
 * {@code SwaggerConfiguration}.
 * <p/>
 *
 * @author Indra Basak
 * @since 02/11/18
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ServerApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SwaggerConfigurationFunctionalTests {

    @Value("${local.server.port}")
    private Integer port;

    @Value("${client.ssl.trust-store-type}")
    private String trustStoreType;

    @Value("${client.ssl.trust-store}")
    private Resource trustStore;

    @Value("${client.ssl.trust-store-password}")
    private String trustStorePassword;

    @Value("${client.ssl.key-store-type}")
    private String keyStoreType;

    @Value("${client.ssl.key-store}")
    private Resource keyStore;

    @Value("${client.ssl.key-store-password}")
    private String keyStorePassword;

    private RestAssuredConfig clientConfig;

    @Autowired
    ApplicationContext context;

    @Before
    public void startUp() throws IOException {
        RestAssured.useRelaxedHTTPSValidation();

        clientConfig =
                RestAssuredConfig.config().sslConfig(SSLConfig.sslConfig()
                        .allowAllHostnames()
                        .keystoreType(keyStoreType)
                        .keyStore(keyStore.getURL().getFile(),
                                keyStorePassword)
                        .trustStoreType(trustStoreType)
                        .trustStore(trustStore.getURL().getFile(),
                                trustStorePassword));
    }

    @Test
    public void testApi() {
        Response response = given()
                .config(clientConfig)
                .contentType(ContentType.JSON)
                .baseUri("https://localhost")
                .port(port)
                .contentType(ContentType.JSON)
                .get("/v2/api-docs?group=book");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody().prettyPrint());
    }
}

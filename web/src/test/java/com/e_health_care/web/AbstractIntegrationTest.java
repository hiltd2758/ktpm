package com.e_health_care.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    protected String baseUrl() {
        return "http://localhost:" + port;
    }

    protected HttpHeaders patientCookieHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "jwt-patient-token=" + token);
        return headers;
    }

    protected HttpHeaders doctorCookieHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "jwt-doctor-token=" + token);
        return headers;
    }

    protected HttpHeaders adminCookieHeader(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie", "jwt-admin-token=" + token);
        return headers;
    }

    protected HttpHeaders jsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders jsonWithCookie(String cookieName, String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Cookie", cookieName + "=" + token);
        return headers;
    }
}
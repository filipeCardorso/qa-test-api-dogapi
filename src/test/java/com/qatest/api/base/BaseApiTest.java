package com.qatest.api.base;

import com.qatest.api.specs.RequestSpecs;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;

import static io.restassured.RestAssured.given;

public abstract class BaseApiTest {

    protected RequestSpecification request;

    @BeforeEach
    void setUp() {
        request = given().spec(RequestSpecs.defaultSpec());
    }
}

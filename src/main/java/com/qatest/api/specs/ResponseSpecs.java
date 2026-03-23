package com.qatest.api.specs;

import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.ResponseSpecification;

import static org.hamcrest.Matchers.equalTo;

public final class ResponseSpecs {

    private ResponseSpecs() {}

    public static ResponseSpecification successResponse() {
        return new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .expectBody("status", equalTo("success"))
                .build();
    }

    public static ResponseSpecification errorResponse() {
        return new ResponseSpecBuilder()
                .expectStatusCode(404)
                .expectContentType(ContentType.JSON)
                .expectBody("status", equalTo("error"))
                .build();
    }
}

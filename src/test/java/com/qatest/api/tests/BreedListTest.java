package com.qatest.api.tests;

import com.qatest.api.base.BaseApiTest;
import com.qatest.api.models.BreedListResponse;
import com.qatest.api.specs.ResponseSpecs;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Breed List")
@DisplayName("GET /breeds/list/all")
class BreedListTest extends BaseApiTest {

    private static final String ENDPOINT = "/breeds/list/all";

    @Test
    @DisplayName("Should return status 200")
    @Description("Verify that the breed list endpoint returns HTTP 200")
    @Severity(SeverityLevel.BLOCKER)
    void shouldReturnStatus200() {
        // Arrange — request configured in BaseApiTest

        // Act & Assert
        request
            .when()
                .get(ENDPOINT)
            .then()
                .spec(ResponseSpecs.successResponse());
    }

    @Test
    @DisplayName("Should return non-empty list of breeds")
    @Description("Verify that the breed list is not empty")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnNonEmptyBreedList() {
        // Arrange — request configured in BaseApiTest

        // Act
        BreedListResponse response = request
            .when()
                .get(ENDPOINT)
            .then()
                .spec(ResponseSpecs.successResponse())
                .extract().as(BreedListResponse.class);

        // Assert
        assertThat(response.getMessage()).isNotEmpty();
    }

    @ParameterizedTest(name = "Should contain breed: {0}")
    @ValueSource(strings = {"bulldog", "labrador", "poodle", "beagle"})
    @Description("Verify that the list contains well-known breeds")
    @Severity(SeverityLevel.NORMAL)
    void shouldContainKnownBreed(String breed) {
        // Arrange — breed provided by @ValueSource

        // Act
        BreedListResponse response = request
            .when()
                .get(ENDPOINT)
            .then()
                .spec(ResponseSpecs.successResponse())
                .extract().as(BreedListResponse.class);

        // Assert
        assertThat(response.getMessage().keySet()).contains(breed);
    }

    @Test
    @DisplayName("Should return status field as 'success'")
    @Description("Verify JSON contains status = success")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnStatusSuccess() {
        // Arrange — request configured in BaseApiTest

        // Act
        BreedListResponse response = request
            .when()
                .get(ENDPOINT)
            .then()
                .statusCode(200)
                .extract().as(BreedListResponse.class);

        // Assert
        assertThat(response.getStatus()).isEqualTo("success");
    }
}

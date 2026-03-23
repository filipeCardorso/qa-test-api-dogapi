package com.qatest.api.tests;

import com.qatest.api.base.BaseApiTest;
import com.qatest.api.models.RandomImageResponse;
import com.qatest.api.specs.ResponseSpecs;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Random Image")
@DisplayName("GET /breeds/image/random")
class RandomImageTest extends BaseApiTest {

    private static final String ENDPOINT = "/breeds/image/random";

    @Test
    @DisplayName("Should return status 200")
    @Description("Verify that the random image endpoint returns HTTP 200")
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
    @DisplayName("Should return valid image URL")
    @Description("Verify that the returned URL points to an image file")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnValidImageUrl() {
        // Arrange — request configured in BaseApiTest

        // Act
        RandomImageResponse response = request
            .when()
                .get(ENDPOINT)
            .then()
                .spec(ResponseSpecs.successResponse())
                .extract().as(RandomImageResponse.class);

        // Assert
        assertThat(response.getMessage())
                .startsWith("https://")
                .containsPattern("\\.(jpg|jpeg|png|gif)");
    }

    @Test
    @DisplayName("Should return status field as 'success'")
    @Description("Verify JSON contains status = success")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnStatusSuccess() {
        // Arrange — request configured in BaseApiTest

        // Act
        RandomImageResponse response = request
            .when()
                .get(ENDPOINT)
            .then()
                .statusCode(200)
                .extract().as(RandomImageResponse.class);

        // Assert
        assertThat(response.getStatus()).isEqualTo("success");
    }
}

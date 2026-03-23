package com.qatest.api.tests;

import com.qatest.api.base.BaseApiTest;
import com.qatest.api.models.BreedImagesResponse;
import com.qatest.api.specs.ResponseSpecs;
import com.qatest.api.utils.DataHelper;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Breed Images")
@DisplayName("GET /breed/{breed}/images")
class BreedImagesTest extends BaseApiTest {

    @Test
    @DisplayName("Should return images for valid breed")
    @Description("Verify that images are returned for labrador breed")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnImagesForValidBreed() {
        // Arrange
        String breed = DataHelper.validBreed();

        // Act
        BreedImagesResponse response = request
            .when()
                .get("/breed/{breed}/images", breed)
            .then()
                .spec(ResponseSpecs.successResponse())
                .extract().as(BreedImagesResponse.class);

        // Assert
        assertThat(response.getMessage()).isNotEmpty();
    }

    @Test
    @DisplayName("Should return valid image URLs")
    @Description("Verify that returned URLs are valid image URLs ending in .jpg or .png")
    @Severity(SeverityLevel.NORMAL)
    void shouldReturnValidImageUrls() {
        // Arrange
        String breed = DataHelper.validBreed();

        // Act
        BreedImagesResponse response = request
            .when()
                .get("/breed/{breed}/images", breed)
            .then()
                .spec(ResponseSpecs.successResponse())
                .extract().as(BreedImagesResponse.class);

        // Assert
        assertThat(response.getMessage()).allSatisfy(url -> {
            assertThat(url).startsWith("https://");
            assertThat(url.toLowerCase()).containsPattern("\\.(jpg|jpeg|png|gif)");
        });
    }

    @Test
    @DisplayName("Should return error for invalid breed")
    @Description("Verify that a 404 error is returned for non-existent breed")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnErrorForInvalidBreed() {
        // Arrange
        String breed = DataHelper.invalidBreed();

        // Act & Assert
        request
            .when()
                .get("/breed/{breed}/images", breed)
            .then()
                .spec(ResponseSpecs.errorResponse());
    }
}

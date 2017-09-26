/*
 * Copyright 2017, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package com.yahoo.elide.tests;

import com.jayway.restassured.RestAssured;
import com.yahoo.elide.core.HttpStatus;
import com.yahoo.elide.initialization.AbstractApiResourceInitializer;
import com.yahoo.elide.utils.JsonParser;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

public class AnyPolymorphismIT extends AbstractApiResourceInitializer {
    private static final String JSONAPI_CONTENT_TYPE = "application/vnd.api+json";
    private final JsonParser jsonParser = new JsonParser();

    private final int horsepower = 102;
    private final String one = "1";

    private final String propertyPath = "/property";

    private final String tractorAsPropertyFile = "/AnyPolymorphismIT/AddTractorProperty.json";
    private final String smartphoneAsPropertyFile = "/AnyPolymorphismIT/AddSmartphoneProperty.json";


    @BeforeClass
    public void setUp() {
        //Create a tractor
        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body("{\"data\": {\"type\": \"tractor\", \"attributes\": {\"horsepower\": 102 }}}")
                .post("/tractor")
                .then()
                .statusCode(HttpStatus.SC_CREATED);

        //Create a smartphone
        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body("{\"data\": {\"type\": \"smartphone\", \"attributes\": {\"type\": \"android\" }}}")
                .post("/smartphone")
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    public void testAny() {
        final String relationshipType = "data.relationships.myStuff.data.type";
        final String relationshipId = "data.relationships.myStuff.data.id";
        final String tractorType = "tractor";
        final String smartphoneType = "smartphone";
        final String includedType = "included[0].type";
        final String includedId = "included[0].id";
        final String includedSize = "included.size()";

        String id1 = RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body(jsonParser.getJson(tractorAsPropertyFile))
                .post(propertyPath)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("data.id");

        String id2 = RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body(jsonParser.getJson(smartphoneAsPropertyFile))
                .post(propertyPath)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("data.id");

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id1)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(relationshipType, equalTo(tractorType),
                        relationshipId, equalTo(one));

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id2)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(relationshipType, equalTo(smartphoneType),
                        relationshipId, equalTo(one));

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id1 + "?include=myStuff")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(includedType, equalTo(tractorType),
                        includedId, equalTo(one),
                        "included[0].attributes.horsepower", equalTo(horsepower),
                        includedSize, equalTo(1));


        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/"  + id2 + "?include=myStuff")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body(includedType, equalTo(smartphoneType),
                        includedId, equalTo(one),
                        "included[0].attributes.type", equalTo("android"),
                        includedSize, equalTo(1));

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", equalTo(2));
    }

    @Test
    public void testAnyupdate() {
        String id = RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body(jsonParser.getJson(tractorAsPropertyFile))
                .post(propertyPath)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .body("data.relationships.myStuff.data.type", equalTo("tractor"))
                .extract()
                .path("data.id");

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body("{\"data\": {\"id\": \"" + id + "\", \"type\": \"property\", \"relationships\": {\"myStuff\": {\"data\": {\"type\": \"smartphone\", \"id\": \"1\"}}}}}")
                .patch(propertyPath + "/" + id)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("data.relationships.myStuff.data.type", equalTo("smartphone"));

        //delete relation
        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body("{\"data\": {\"id\": \"" + id + "\", \"type\": \"property\", \"relationships\": {\"myStuff\": {\"data\": null}}}}")
                .patch(propertyPath + "/" + id)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("data.relationships.myStuff.data", equalTo(null));
    }

    @Test
    public void testAnySubpaths() {
        String id = RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .body(jsonParser.getJson(tractorAsPropertyFile))
                .post(propertyPath)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .extract()
                .path("data.id");

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "?page[totals]")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("meta.page.totalRecords", greaterThan(0));

        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id + "/myStuff")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("data.attributes.horsepower", equalTo(horsepower),
                        "data.id", equalTo(one));

        //single entity so no page appropriate stuff
        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id + "/myStuff?page[totals]")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("meta", equalTo(null));

        //Filtering is not supported for these types.
        RestAssured
                .given()
                .contentType(JSONAPI_CONTENT_TYPE)
                .accept(JSONAPI_CONTENT_TYPE)
                .get(propertyPath + "/" + id + "?filter[tractor]=horsepower==103")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}

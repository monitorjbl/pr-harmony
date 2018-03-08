package it.com.monitorjbl.plugins.rest;

import com.google.common.collect.ImmutableList;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.jayway.restassured.RestAssured;
import com.monitorjbl.plugins.config.Config;
import it.com.monitorjbl.plugins.BaseIntegrationTest;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminPassword;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminUser;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1Repository1;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThat;

public class UserResourceTest extends BaseIntegrationTest {
  @Test
  public void testGet() {
    testContext.project(TEST_PROJECT, "Harmony")
        .repository(TEST_PROJECT, TEST_REPO);
    setUpUsersAndGroups(testContext);
    putRepositoryConfig(TEST_CONFIG, TEST_PROJECT, TEST_REPO);

    InputStream response = RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("blockMergeIfPrNeedsWork", equalTo(true))
        .body("defaultReviewers", hasSize(3))
        .body("requiredReviewers", hasSize(2))
        .body("requiredReviews", equalTo(1))
        .log().ifValidationFails()
        .when()
        .get(getRepositoryUrl("users", TEST_PROJECT, TEST_REPO))
        .asInputStream();

    //containsInAnyOrder is used because the way user lists are created doesn't guarantee ordering
    ReadContext ctx = JsonPath.parse(response);
    assertThat(ctx.read("defaultReviewers[*].name"), containsInAnyOrder("bob", "janed", "jdoe"));
    assertThat(ctx.read("requiredReviewers[*].name"), containsInAnyOrder("bob", "janed"));
  }

  @Test
  public void testGet_duplicatesRemoved() {
    List<String> allUsers = ImmutableList.of("bob", "janed", "jdoe");
    Config config = TEST_CONFIG.copyBuilder()
        //Include all of the users and all the groups for default and required. This means:
        //- "bob" gets referenced once (by name)
        //- "janed" gets referenced 4 times (by name, and once for each group)
        //- "jdoe" gets referenced 3 times (by name, by "all-group" and by "default-group")
        .defaultReviewers(allUsers)
        .defaultReviewerGroups(ImmutableList.of("all-group", "default-group", "required-group"))
        .requiredReviewers(allUsers)
        .requiredReviewerGroups(ImmutableList.of("all-group", "default-group", "required-group"))
        .build();

    testContext.project(TEST_PROJECT, "Harmony")
        .repository(TEST_PROJECT, TEST_REPO);
    setUpUsersAndGroups(testContext);
    putRepositoryConfig(config, TEST_PROJECT, TEST_REPO);

    InputStream response = RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("blockMergeIfPrNeedsWork", equalTo(true))
        .body("defaultReviewers", hasSize(3))
        .body("requiredReviewers", hasSize(3))
        .body("requiredReviews", equalTo(1))
        .log().ifValidationFails()
        .when()
        .get(getRepositoryUrl("users", TEST_PROJECT, TEST_REPO))
        .asInputStream();

    //containsInAnyOrder is used because the way user lists are created doesn't guarantee ordering
    ReadContext ctx = JsonPath.parse(response);
    assertThat(ctx.read("defaultReviewers[*].name"), containsInAnyOrder(allUsers.toArray()));
    assertThat(ctx.read("requiredReviewers[*].name"), containsInAnyOrder(allUsers.toArray()));
  }

  @Test
  public void testGet_noConfig() {
    RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("blockMergeIfPrNeedsWork", isEmptyString())
        .body("defaultReviewers", empty())
        .body("requiredReviewers", empty())
        .body("requiredReviews", isEmptyString())
        .log().ifValidationFails()
        .when()
        .get(getRepositoryUrl("users", getProject1(), getProject1Repository1()));
  }
}

package it.com.monitorjbl.plugins;

import com.atlassian.bitbucket.test.BaseFuncTest;
import com.atlassian.bitbucket.test.TestContext;
import com.jayway.restassured.RestAssured;
import com.jayway.restassured.http.ContentType;
import com.monitorjbl.plugins.config.Config;

import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminPassword;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminUser;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getRestURL;
import static java.util.Collections.singletonList;

public class BaseIntegrationTest extends BaseFuncTest {
  protected static final Config TEST_CONFIG = Config.builder()
      .automergePRs(singletonList("master"))
      .automergePRsFrom(singletonList("release/.*"))
      .blockedCommits(singletonList("master"))
      .blockMergeIfPrNeedsWork(true)
      .blockedPRs(singletonList("release/.*"))
      .defaultReviewerGroups(singletonList("default-group"))
      .defaultReviewers(singletonList("bob"))
      .excludedGroups(singletonList("excluded-group"))
      .excludedUsers(singletonList("janed"))
      .requiredReviews(1)
      .requiredReviewerGroups(singletonList("required-group"))
      .requiredReviewers(singletonList("bob"))
      .build();
  protected static final String TEST_PROJECT = "HRMNY";
  protected static final String TEST_REPO = "pr-harmony";

  protected static String getProjectUrl(String resource, String projectKey) {
    return getRestURL("pr-harmony", "1.0") + "/" + resource + "/" + projectKey;
  }

  protected static String getRepositoryUrl(String resource, String projectKey, String repositorySlug) {
    return getProjectUrl(resource, projectKey) + "/" + repositorySlug;
  }

  protected static void putProjectConfig(Config config, String projectKey) {
    putConfig(config, getProjectUrl("config", projectKey));
  }

  protected static void putRepositoryConfig(Config config, String projectKey, String repositorySlug) {
    putConfig(config, getRepositoryUrl("config", projectKey, repositorySlug));
  }

  protected static void setUpUsersAndGroups(TestContext testContext) {
    testContext
        .user("bob")
        .user("janed")
        .user("jdoe")
        .group("all-group", "bob", "janed", "jdoe")
        .group("default-group", "jdoe", "janed")
        .group("excluded-group", "bob")
        .group("required-group", "janed");
  }

  private static void putConfig(Config config, String url) {
    RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .body(config)
        .contentType(ContentType.JSON)
        .expect()
        .log().ifValidationFails()
        .statusCode(200)
        .when()
        .put(url);
  }
}

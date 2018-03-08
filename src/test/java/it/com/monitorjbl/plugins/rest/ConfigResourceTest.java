package it.com.monitorjbl.plugins.rest;

import com.jayway.restassured.RestAssured;
import it.com.monitorjbl.plugins.BaseIntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;

import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminPassword;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminUser;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1Repository1;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getRestURL;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

public class ConfigResourceTest extends BaseIntegrationTest {
  @BeforeClass
  public static void setUpUsersAndGroups() {
    //Setup groups/users to use in other tests.
    setUpUsersAndGroups(getClassTestContext());
  }

  @Test
  public void testGetProjectConfig_noConfig() {
    RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("automergePRs", empty())
        .body("automergePRsFrom", empty())
        .body("blockedCommits", empty())
        .body("blockedPRs", empty())
        .body("defaultReviewerGroups", empty())
        .body("defaultReviewers", empty())
        .body("excludedGroups", empty())
        .body("excludedUsers", empty())
        .body("requiredReviewerGroups", empty())
        .body("requiredReviewers", empty())
        .log().ifValidationFails()
        .when()
        .get(getRestURL("pr-harmony", "1.0") + "/config/" + getProject1());
  }

  @Test
  public void testGetRepoConfig_noConfig() {
    RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("automergePRs", empty())
        .body("automergePRsFrom", empty())
        .body("blockedCommits", empty())
        .body("blockedPRs", empty())
        .body("defaultReviewerGroups", empty())
        .body("defaultReviewers", empty())
        .body("excludedGroups", empty())
        .body("excludedUsers", empty())
        .body("requiredReviewerGroups", empty())
        .body("requiredReviewers", empty())
        .log().ifValidationFails()
        .when()
        .get(getRestURL("pr-harmony", "1.0") + "/config/" + getProject1() + "/" + getProject1Repository1());
  }

  @Test
  public void testProjectConfig_roundtrip() {
    //Create a project specifically for this test, to ensure we don't apply configuration to any
    //project that might be reused later and interfere with subsequent tests
    testContext.project(TEST_PROJECT, "Harmony");
    putProjectConfig(TEST_CONFIG, TEST_PROJECT);

    RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("automergePRs", equalTo(TEST_CONFIG.getAutomergePRs()))
        .body("automergePRsFrom", equalTo(TEST_CONFIG.getAutomergePRsFrom()))
        .body("blockedCommits", equalTo(TEST_CONFIG.getBlockedCommits()))
        .body("blockedPRs", equalTo(TEST_CONFIG.getBlockedPRs()))
        .body("blockMergeIfPrNeedsWork", equalTo(TEST_CONFIG.getBlockMergeIfPrNeedsWork()))
        .body("defaultReviewerGroups", equalTo(TEST_CONFIG.getDefaultReviewerGroups()))
        .body("defaultReviewers", equalTo(TEST_CONFIG.getDefaultReviewers()))
        .body("excludedGroups", equalTo(TEST_CONFIG.getExcludedGroups()))
        .body("excludedUsers", equalTo(TEST_CONFIG.getExcludedUsers()))
        .body("requiredReviewerGroups", equalTo(TEST_CONFIG.getRequiredReviewerGroups()))
        .body("requiredReviewers", equalTo(TEST_CONFIG.getRequiredReviewers()))
        .body("requiredReviews", equalTo(TEST_CONFIG.getRequiredReviews()))
        .log().ifValidationFails()
        .expect()
        .when()
        .get(getRestURL("pr-harmony", "1.0") + "/config/" + TEST_PROJECT);
  }

  @Test
  public void testRepoConfig_roundtrip() {
    //Create a repository specifically for this test, to ensure we don't apply configuration to any
    //repository that might be reused later and interfere with subsequent tests
    testContext.project(TEST_PROJECT, "Harmony")
        .repository(TEST_PROJECT, TEST_REPO);
    putRepositoryConfig(TEST_CONFIG, TEST_PROJECT, TEST_REPO);

    RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("automergePRs", equalTo(TEST_CONFIG.getAutomergePRs()))
        .body("automergePRsFrom", equalTo(TEST_CONFIG.getAutomergePRsFrom()))
        .body("blockedCommits", equalTo(TEST_CONFIG.getBlockedCommits()))
        .body("blockedPRs", equalTo(TEST_CONFIG.getBlockedPRs()))
        .body("blockMergeIfPrNeedsWork", equalTo(TEST_CONFIG.getBlockMergeIfPrNeedsWork()))
        .body("defaultReviewerGroups", equalTo(TEST_CONFIG.getDefaultReviewerGroups()))
        .body("defaultReviewers", equalTo(TEST_CONFIG.getDefaultReviewers()))
        .body("excludedGroups", equalTo(TEST_CONFIG.getExcludedGroups()))
        .body("excludedUsers", equalTo(TEST_CONFIG.getExcludedUsers()))
        .body("requiredReviewerGroups", equalTo(TEST_CONFIG.getRequiredReviewerGroups()))
        .body("requiredReviewers", equalTo(TEST_CONFIG.getRequiredReviewers()))
        .body("requiredReviews", equalTo(TEST_CONFIG.getRequiredReviews()))
        .log().ifValidationFails()
        .expect()
        .when()
        .get(getRestURL("pr-harmony", "1.0") + "/config/" + TEST_PROJECT + "/" + TEST_REPO);
  }
}

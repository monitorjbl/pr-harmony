package it.com.monitorjbl.plugins.ui;

import com.atlassian.bitbucket.test.BaseFuncTest;
import com.jayway.restassured.RestAssured;
import org.junit.Test;

import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminPassword;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminUser;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1Repository1;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getRestURL;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.isEmptyString;

//At the moment this test doesn't cover any significant. The biggest benefit it offers is that, by calling the
//UserResource, it verifies that the plugin has been installed and started successfully
public class UserResourceTest extends BaseFuncTest {

  @Test
  public void testGetWithoutConfig() {
    RestAssured.given()
        .auth().preemptive().basic(getAdminUser(), getAdminPassword())
        .expect()
        .body("requiredReviews", isEmptyString())
        .body("blockMergeIfPrNeedsWork", isEmptyString())
        .body("requiredReviewers", empty())
        .body("defaultReviewers", empty())
        .when()
        .get(getRestURL("pr-harmony", "1.0") + "/users/" + getProject1() + "/" + getProject1Repository1());
  }
}

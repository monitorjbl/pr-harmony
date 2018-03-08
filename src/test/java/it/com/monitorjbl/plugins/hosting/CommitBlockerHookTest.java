package it.com.monitorjbl.plugins.hosting;

import com.atlassian.bitbucket.permission.Permission;
import com.atlassian.bitbucket.test.ProcessFailedException;
import com.atlassian.bitbucket.test.RepositoryTestHelper;
import com.monitorjbl.plugins.config.Config;
import it.com.monitorjbl.plugins.BaseIntegrationTest;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Collections;
import java.util.Locale;

import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminPassword;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getAdminUser;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getBaseURL;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1;
import static com.atlassian.bitbucket.test.DefaultFuncTestData.getProject1Repository1;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class CommitBlockerHookTest extends BaseIntegrationTest {
  private static final Config FORK_CONFIG = Config.builder()
      .blockedCommits(Collections.singletonList("master"))
      .excludedGroups(Collections.singletonList("exclusions"))
      .excludedUsers(Collections.singletonList("excluded-by-name"))
      .build();
  private static final String FORK_PROJECT = ("~" + getAdminUser()).toUpperCase(Locale.ROOT);
  private static final String FORK_REPO = getProject1Repository1();

  @BeforeClass
  public static void setupRepository() {
    getClassTestContext()
        //Create some users to facilitate the various tests
        .user("blocked-user")
        .user("excluded-by-group")
        .user("excluded-by-name")
        //Create some groups for the new users
        .group("exclusions", "excluded-by-group")
        .group("test-users", "blocked-user", "excluded-by-group", "excluded-by-name")
        //Fork the default repository to our personal project so we don't make any changes
        //to the default repository directly, which may throw off other tests
        .fork(getProject1(), getProject1Repository1(), getAdminUser(), getAdminPassword())
        //Grant all of the test users permission to push to the test repository
        .repositoryPermissionForGroup(FORK_PROJECT, FORK_REPO, "test-users", Permission.REPO_WRITE);
    putRepositoryConfig(FORK_CONFIG, FORK_PROJECT, FORK_REPO);
  }

  @Test
  public void testPush_blocked() throws Exception {
    try {
      RepositoryTestHelper.pushCommit(getBaseURL(), "blocked-user", "blocked-user", FORK_PROJECT, FORK_REPO, "master",
          "Johnny B. Blocked", "jbblocked@test.com", "harmony", "harmonious.txt", "Test", "Should be blocked");

      fail("The blocked user should not be allowed to push");
    } catch (ProcessFailedException expected) {
      assertThat(expected.getStdErr(), allOf(
          containsString("Push Rejected"),
          containsString("Direct pushes are not allowed for [master]")
      ));
    }
  }

  @Test
  public void testPush_excludedByGroup() throws Exception {
    RepositoryTestHelper.pushCommit(getBaseURL(), "excluded-by-group", "excluded-by-group", FORK_PROJECT, FORK_REPO,
        "master", "John Doe", "jdoe@test.com", "harmony", "harmonious.txt", "Excluded by group", "Should be accepted");
  }

  @Test
  public void testPush_excludedByName() throws Exception {
    RepositoryTestHelper.pushCommit(getBaseURL(), "excluded-by-name", "excluded-by-name", FORK_PROJECT, FORK_REPO,
        "master", "Jane Doe", "janed@test.com", "harmony", "harmonious.txt", "Excluded by name", "Should be accepted");
  }

  @Test
  public void testPush_unrestrictedBranch() throws Exception {
    //This user has no exclusions, so they can only push if the branch isn't restricted
    RepositoryTestHelper.pushCommit(getBaseURL(), "blocked-user", "blocked-user", FORK_PROJECT, FORK_REPO,
        "basic_branching", "Jane Doe", "janed@test.com", "harmony", "harmonious.txt", "Test", "Unrestricted");
  }
}

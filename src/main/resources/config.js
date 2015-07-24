(function ($) {
  var baseUrl, projectKey, repoSlug;

  function saveConfig() {
    var requiredReviews = parseInt($('#requiredReviews').val());
    var requiredReviewers = $.grep($('#requiredReviewers').val().split(','), function (v) {return v != ''});
    var requiredReviewerGroups = $.grep($('#requiredReviewerGroups').val().split(','), function (v) {return v != ''});

    if (requiredReviews > requiredReviewers.length && requiredReviewerGroups.length == 0) {
      alert(
          "You've specified a number of required reviews greater than the number of required reviewers. " +
          "This will make PRs impossible to merge, please add some required reviewers.");
      return;
    }

    $.ajax({
      url: baseUrl + "/rest/pr-harmony/1.0/config/" + projectKey + "/" + repoSlug,
      type: "PUT",
      contentType: "application/json",
      data: JSON.stringify({
        requiredReviews: requiredReviews,
        requiredReviewers: requiredReviewers,
        requiredReviewerGroups: requiredReviewerGroups,
        defaultReviewers: $('#defaultReviewers').val().split(','),
        defaultReviewerGroups: $('#defaultReviewerGroups').val().split(','),
        excludedUsers: $('#excludedUsers').val().split(','),
        excludedGroups: $('#excludedGroups').val().split(','),
        blockedCommits: $('#blockedCommits').val().split(','),
        blockedPRs: $('#blockedPRs').val().split(','),
        automergePRs: $('#automergePRs').val().split(','),
        automergePRsFrom: $('#automergePRsFrom').val().split(',')
      }),
      success: function (config) {
        location.reload();
      }
    });
  }

  function getConfig() {
    $.ajax({
      url: baseUrl + "/rest/pr-harmony/1.0/config/" + projectKey + "/" + repoSlug,
      dataType: "json",
      success: function (config) {
        $('#requiredReviews').val(config.requiredReviews);
        $('#requiredReviewers').val(config.requiredReviewers);
        $('#requiredReviewerGroups').val(config.requiredReviewerGroups);
        $('#defaultReviewers').val(config.defaultReviewers);
        $('#defaultReviewerGroups').val(config.defaultReviewerGroups);
        $('#excludedUsers').val(config.excludedUsers);
        $('#excludedGroups').val(config.excludedGroups);
        $('#blockedCommits').val(config.blockedCommits);
        $('#blockedPRs').val(config.blockedPRs);
        $('#automergePRs').val(config.automergePRs);
        $('#automergePRsFrom').val(config.automergePRsFrom);
      }
    });
  }

  $(document).ready(function () {
    baseUrl = $("#baseUrl").val();
    projectKey = $("#projectKey").val();
    repoSlug = $("#repoSlug").val();
    $('#saveButton').click(function () {
      saveConfig();
    });
    getConfig();
  });
})(AJS.$);


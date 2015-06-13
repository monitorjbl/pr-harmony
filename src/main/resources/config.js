(function ($) {
    var baseUrl, projectKey, repoSlug;

console.log("asdfasfasdfasdf");
    function saveConfig() {
        $.ajax({
            url: baseUrl + "/rest/pr-harmony/1.0/config/" + projectKey + "/" + repoSlug,
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify({
                requiredReviews: $('#requiredReviews').val(),
                requiredReviewers: $('#requiredReviewers').val().split(','),
                requiredReviewerGroups: $('#requiredReviewerGroups').val().split(','),
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


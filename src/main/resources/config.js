(function ($) {
    var baseUrl, projectKey, repoSlug;


    function saveConfig() {
        $.ajax({
            url: baseUrl + "/rest/pr-harmony/1.0/config/" + projectKey + "/" + repoSlug,
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify({
                requiredReviews: $('#requiredReviews').val(),
                requiredReviewers: $('#requiredReviewers').val().split(','),
                defaultReviewers: $('#defaultReviewers').val().split(','),
                excludedUsers: $('#excludedUsers').val().split(','),
                blockedCommits: $('#blockedCommits').val().split(','),
                blockedPRs: $('#blockedPRs').val().split(','),
                automergePRs: $('#automergePRs').val().split(',')
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
                $('#defaultReviewers').val(config.defaultReviewers);
                $('#excludedUsers').val(config.excludedUsers);
                $('#blockedCommits').val(config.blockedCommits);
                $('#blockedPRs').val(config.blockedPRs);
                $('#automergePRs').val(config.automergePRs);
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


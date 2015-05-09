(function ($) {
    var baseUrl;

    function saveConfig() {
        $.ajax({
            url: baseUrl + "/rest/pr-harmony/1.0/config/PROJECT_1/rep_1",
            type: "PUT",
            contentType: "application/json",
            data: JSON.stringify({
                requiredReviews: $('#requiredReviews').val(),
                requiredReviewers: $('#requiredReviewers').val().split(','),
                defaultReviewers: $('#defaultReviewers').val().split(','),
                excludedUsers: $('#excludedUsers').val().split(','),
                blockedCommits: $('#blockedCommits').val().split(','),
                blockedPRs: $('#blockedPRs').val().split(',')
            }),
            success: function (config) {
                location.reload();
            }
        });
    }

    function getConfig() {
        $.ajax({
            url: baseUrl + "/rest/pr-harmony/1.0/config/PROJECT_1/rep_1",
            dataType: "json",
            success: function (config) {
                $('#requiredReviews').val(config.requiredReviews);
                $('#requiredReviewers').val(config.requiredReviewers);
                $('#defaultReviewers').val(config.defaultReviewers);
                $('#excludedUsers').val(config.excludedUsers);
                $('#blockedCommits').val(config.blockedCommits);
                $('#blockedPRs').val(config.blockedPRs);
            }
        });
    }

    $(document).ready(function(){
        baseUrl = $("#baseUrl").html();
        $('#saveButton').click(function(){
           saveConfig();
        });
        getConfig();
    });
})(AJS.$);


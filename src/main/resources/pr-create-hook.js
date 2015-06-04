define('suggested-reviewers', [
        'jquery',
        'util/events',
        'stash/api/util/state',
        'exports'
    ], function ($, events, state, exports) {

        var root = AJS.contextPath(),
            defaultReviewers,
            currentUser,
            projectKey,
            repoSlug;

        (function () {
            currentUser = state.getCurrentUser();
            var action = $('form[action]').attr('action').replace(root + '/projects/', '');
            projectKey = action.substring(0, action.indexOf("/"));
            repoSlug = action.substring(action.indexOf("/")).replace('/repos/', '');
            repoSlug = repoSlug.substring(0, repoSlug.indexOf("/"));
        })();

        var addUser = function (user) {
            if (user.slug != currentUser.slug) {
                var select2 = $('.field-group.pull-request-reviewers #reviewers').data('select2');
                var reviewers = select2.data();
                reviewers.push({
                    id: user.slug,
                    text: user.name,
                    item: {
                        displayName: user.name,
                        avatarUrl: root + '/users/' + user.slug + '/avatar.png?s=48'
                    }
                });
                select2.data(reviewers);
            }
        };

        var addAllUsers = function () {
            $.each(defaultReviewers, function () {
                addUser(this);
            });
        };

        var fetchUsers = function (callback) {
            if (!defaultReviewers) {
                $.ajax({
                    url: root + '/rest/pr-harmony/1.0/users/' + projectKey + '/' + repoSlug,
                    dataType: "json",
                    success: function (reviewers) {
                        defaultReviewers = reviewers;
                        console.log(reviewers);
                        if (callback) {
                            callback();
                        }
                    }
                });
            }
        };

        exports.init = function () {
            console.log("init");
            var fetch = function (data) {
                fetchUsers();
            };
            events.on("stash.model.page-state.changed.sourceBranch", fetch);
            events.on("stash.model.page-state.changed.targetBranch", fetch);
            events.on("stash.feature.compare.form.state", fetch);

            fetchUsers(addAllUsers);


            var check = function () {
                console.log('reviewers: ' + $('#reviewers').val());
                setTimeout(check, 2000);
            };
            setTimeout(check, 2000);
        };
    }
)
;

AJS.$(document).ready(function ($) {
    return function () {
        require("suggested-reviewers").init();
    };
}(AJS.$));
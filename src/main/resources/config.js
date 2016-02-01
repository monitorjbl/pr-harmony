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
      url: baseUrl + "/rest/pr-harmony/1.0/config/" + projectKey + "/" + (repoSlug || ''),
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
      url: baseUrl + "/rest/pr-harmony/1.0/config/" + projectKey + "/" + (repoSlug || ''),
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

        //initialize selections
        userSelection("#defaultReviewers");
        userSelection("#requiredReviewers");
        userSelection("#excludedUsers");
        groupSelection("#defaultReviewerGroups");
        groupSelection("#requiredReviewerGroups");
        groupSelection("#excludedGroups");
      }
    });
  }

  function userSelection(ele) {
    $(ele).auiSelect2({
      placeholder: "Search for a user",
      minimumInputLength: 2,
      multiple: true,
      initSelection: function (element, callback) {
        callback($.map($(element).val().split(','), function (v, i) {
          return {id: v.trim(), name: v.trim()};
        }));
      },
      ajax: {
        url: function (search) {
          return baseUrl + "/rest/api/latest/users?avatarSize=32&permission.1=LICENSED_USER&start=0&filter=" + search;
        },
        dataType: 'json',
        quietMillis: 250,
        results: function (data) {
          //format results to Select2 format by adding a unique id field
          $.each(data.values, function () {
            this.id = this.name;
          });
          return {results: data.values}
        },
        cache: true
      },
      formatResult: function (user) {
        return '<div>' + user.displayName + ' (' + user.emailAddress + ')</div>';
      },
      formatSelection: function (user) {
        return user.name;
      }
    });
  }

  function groupSelection(ele) {
    $(ele).auiSelect2({
      placeholder: "Search for a group",
      minimumInputLength: 2,
      multiple: true,
      initSelection: function (element, callback) {
        callback($.map($(element).val().split(','), function (v, i) {
          return {id: v.trim(), name: v.trim()};
        }));
      },
      ajax: {
        url: function (search) {
          return baseUrl + "/projects/" + projectKey + "/permissions/groups?start=0&avatarSize=32&filter=" + search;
        },
        dataType: 'json',
        quietMillis: 250,
        results: function (data) {
          //format results to Select2 format by adding a unique id field
          return {
            results: $.map(data.values, function (val) {
              return {
                id: val.group.name,
                name: val.group.name
              };
            })
          }
        },
        cache: true
      },
      formatResult: function (group) {
        return '<div>' + group.name + '</div>';
      },
      formatSelection: function (group) {
        return group.name;
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


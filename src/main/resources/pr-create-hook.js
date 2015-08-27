define('suggested-reviewers', [
  'jquery',
  'util/events',
  'stash/api/util/state',
  'aui/flag',
  'exports'
], function ($, events, state, flag, exports) {

  var root = AJS.contextPath(),
      selection = $('.field-group.pull-request-reviewers #reviewers'),
      config,
      warning,
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
      var select2 = selection.data('select2');
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
    $.each(config.defaultReviewers, function () {
      addUser(this);
    });
  };

  var current = function () {
    return $('#reviewers').val().split('|!|');
  };

  var currentRequired = function () {
    return $.grep(config.requiredReviewers, function (r) {
      return r.slug == currentUser.name || $.grep(current(), function (v) { return r.slug == v }).length > 0;
    });
  };

  var isSubmitEnabled = function () {
    return currentRequired().length < config.requiredReviews;
  };

  var handleChange = function () {
    if (isSubmitEnabled()) {
      showFlag();
      $('#submit-form').prop('disabled', true);
    } else {
      closeFlag();
      $('#submit-form').prop('disabled', false);
    }
  };

  var showFlag = function () {
    var body = '<p>Your PR must have ' + config.requiredReviews + ' required reviewers on it. Please add ' +
        (config.requiredReviews - currentRequired().length) + ' of the following:</p><ul>';
    var curr = current();
    $.each(config.requiredReviewers, function (i, v) {
      if (v.slug != currentUser.name && $.inArray(v.slug, curr) < 0) {
        body += '<li>' + v.name + ' (' + v.slug + ')</li>';
      }
    });
    body += '</ul>';

    if (warning) {
      $(warning).find('span.body').html(body);
    } else if (selection.is(":visible")) {
      warning = flag({
        type: 'warning',
        title: 'Missing required reviewers',
        persistent: false,
        body: '<span class="body">' + body + '</span>'
      });
    }
  };

  var closeFlag = function () {
    if (warning) {
      warning.close();
      warning = undefined;
    }
  };

  var fetchUsers = function (callback) {
    if (!config) {
      $.ajax({
        url: root + '/rest/pr-harmony/1.0/users/' + projectKey + '/' + repoSlug,
        dataType: "json",
        success: function (data) {
          config = data;
          if (typeof callback == 'function') {
            callback();
          }
        }
      });
    } else if (typeof callback == 'function') {
      callback();
    }
  };

  exports.init = function () {
    var load = function () {
      fetchUsers(function () {
        addAllUsers();
        handleChange();
      });
    };
    events.on("stash.model.page-state.changed.sourceBranch", fetchUsers);
    events.on("stash.model.page-state.changed.targetBranch", fetchUsers);
    events.on("stash.feature.compare.form.state", load);
    selection.on("change", handleChange);
    load();
  };
});

AJS.$(document).ready(function ($) {
  return function () {
    require("suggested-reviewers").init();
  };
}(AJS.$));
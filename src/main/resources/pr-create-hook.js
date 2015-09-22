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
      currentUser;

  (function () {
    currentUser = state.getCurrentUser();
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

  var getTarget = function () {
    return $('#targetRepo > span').data().repository;
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
    AJS.log('Showing warning flag');
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
      AJS.log('Hiding warning flag');
      warning.close();
      warning = undefined;
    }
  };

  var fetchUsers = function (callback) {
    var target = getTarget();
    AJS.log('Fetching required users');
    $.ajax({
      url: root + '/rest/pr-harmony/1.0/users/' + target.project.key + '/' + target.name,
      dataType: "json",
      success: function (data) {
        AJS.log('Loaded required users');
        config = data;
        if (typeof callback == 'function') {
          callback();
        }
      }
    });
  };

  exports.init = function () {
    var load = function () {
      fetchUsers(function () {
        addAllUsers();
        handleChange();
      });
    };
    events.on("stash.model.page-state.changed.targetBranch", load);
    events.on("stash.feature.compare.form.state", load);
    $('#show-create-pr-button').click(load);
    selection.on("change", handleChange);
    load();
  };
});

AJS.$(document).ready(function ($) {
  return function () {
    require("suggested-reviewers").init();
  };
}(AJS.$));
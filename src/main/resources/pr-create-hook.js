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

  function addUser(user) {
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
  }

  function getTarget() {
    return $('#targetRepo > span').data().repository;
  }

  function addAllUsers() {
    $.each(config.defaultReviewers, function () {
      addUser(this);
    });
  }

  function currentReviewers() {
    return $('#reviewers').val().split('|!|');
  }

  function currentRequiredReviewers() {
    var current = currentReviewers();
    return $.grep(config.requiredReviewers, function (r) {
      return reviewerIsSelected(current, r.slug) ||
          (r.slug == currentUser.slug && submitterIsRequiredReviewer() && exactlyEnoughRequiredReviewers());
    });
  }

  function reviewerIsSelected(current, username) {
    return $.grep(current, function (v) { return username == v }).length > 0
  }

  function submitterIsRequiredReviewer() {
    return $.grep(config.requiredReviewers, function (v) {return currentUser.slug == v.slug}).length > 0;
  }

  function exactlyEnoughRequiredReviewers() {
    return config.requiredReviewers.length == config.requiredReviews;
  }

  function isSubmitEnabled() {
    return currentRequiredReviewers().length < config.requiredReviews;
  }

  function handleChange() {
    if (isSubmitEnabled()) {
      showFlag();
      $('#submit-form').prop('disabled', true);
    } else {
      closeFlag();
      $('#submit-form').prop('disabled', false);
    }
  }

  function showFlag() {
    AJS.log('Showing warning flag');
    var body = '<p>Your PR must have ' + config.requiredReviews + ' required reviewers on it. Please add ' +
        (config.requiredReviews - currentRequiredReviewers().length) + ' of the following:</p><ul>';
    var curr = currentReviewers();
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
  }

  function closeFlag() {
    if (warning) {
      AJS.log('Hiding warning flag');
      warning.close();
      warning = undefined;
    }
  }

  function fetchConfig(callback) {
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
      fetchConfig(function () {
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
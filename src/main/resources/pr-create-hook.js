log('pr-create-hook.js');

define('pr-harmony-create', [
  'jquery',
  'bitbucket/util/events',
  'bitbucket/util/state',
  'aui/flag',
  'exports'
], function ($, events, state, flag, exports) {

  log('PR create hook initializing');

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
      log('Adding user "' + user.slug + '" to reviewer list');
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
    } else {
      log('Not adding active user "' + user.slug + '" to reviewer list');
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
      log('Disabling submit button');
      showFlag();
      $('#submit-form').prop('disabled', true);
    } else {
      log('Enabling submit button');
      closeFlag();
      $('#submit-form').prop('disabled', false);
    }
  }

  function showFlag() {
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
      log('Warning tag visible already, replacing HTML body');
      $(warning).find('span.body').html(body);
    } else if (selection.is(":visible")) {
      log('Showing warning flag');
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
      log('Hiding warning flag');
      warning.close();
      warning = undefined;
    }
  }

  function fetchConfig(callback) {
    var target = getTarget();
    log('Fetching PR Harmony configuration for '+target.project.key+'/'+target.slug);
    $.ajax({
      url: root + '/rest/pr-harmony/1.0/users/' + target.project.key + '/' + target.slug,
      dataType: "json",
      success: function (data) {
        log('Loaded config', data);
        config = data;
        if (typeof callback == 'function') {
          callback();
        }
      }
    });
  }

  exports.init = function () {
    var load = function () {
      fetchConfig(function () {
        addAllUsers();
        handleChange();
      });
    };
    events.on("bitbucket.model.page-state.changed.targetBranch", load);
    events.on("bitbucket.feature.compare.form.state", load);
    $('#show-create-pr-button').click(load);
    selection.on("change", handleChange);
    load();
  };
});

AJS.$(document).ready(function ($) {
  return function () {
    log('Reguiring PR create hook');
    require("pr-harmony-create").init();
  };
}(AJS.$));

function log() {
  var args = [].slice.apply(arguments);
  args.unshift('[PR Harmony]:');
  AJS.log.apply(this, args);
}
# PR Harmony

![YinYang](/src/main/resources/images/pluginIcon.png?raw=true)

Provides additional pull request workflows for your team. Most teams have processes around how they merge PRs, but there aren't mechanisms in Stash to ensure that no one accidentally merges something they shouldn't. This plugin attempts to address some of these shortcomings.

# Branch Protection

There are branch permissions in Stash, but they interfere with merge permissions. If you want to lock down a branch to prevent direct commits, you won't be able to merge unless you grant write access to said branch (which defeats the purpose of locking it down in the first place).

This plugin gives you some extra options to make your life a little easier:

* **Block Direct Commits**: Prevent any `git push` to branches.
* **Excluded Users**: Allows particular users the ability to push to blocked branches. Handy if you have integration jobs.
* **Block Pull Requests**: Prevents any pull request from being merged to branches. Handy if you have branches that should only be committed to by integration jobs.

# Pull Request Options

These options allow more Gerrit-style voting workflows within Stash.

* **Default reviewers**: Reviewers that will be added to every PR that is opened.
* **Required reviewers**: Reviewers that must *approve* every PR that is opened.
* **# of reviews**: Number of required reviewers that must approve a PR for it to be merged.
* **Automerge Pull Requests**: Automatically merge pull requests when all required approvals are submitted.

# Screenshots

![Config](/src/main/resources/images/config_screen.png?raw=true)

## History

This plugin is a clean reimplementation of a couple of plugins I did for the Stash instance we use at work. I wanted to clean some pieces of it up after learning more about the Stash plugin API, and I thought that some users might find it useful as well.

## Attributions

[Yin Yang](https://www.iconfinder.com/icons/379351/yang_yin_icon#size=128) icon by [Webalys](https://www.iconfinder.com/webalys)

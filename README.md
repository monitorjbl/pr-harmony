[![Run Status](https://api.shippable.com/projects/55cfbb00edd7f2c052a980ac/badge?branch=master)](https://app.shippable.com/projects/55cfbb00edd7f2c052a980ac)

# PR Harmony

![YinYang](/src/main/resources/images/pluginIcon.png?raw=true)

Provides additional pull request workflows for your team. Available on the [Atlassian Marketplace](https://marketplace.atlassian.com/plugins/com.monitorjbl.plugins.pr-harmony) now. 

Most teams have processes around how they merge PRs, but there aren't mechanisms in Stash to ensure that no one accidentally merges something they shouldn't. This plugin attempts to address some of these shortcomings.

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

# Building

Requires Java 7+ and Maven 3.2+

```
git clone https://github.com/monitorjbl/pr-harmony.git
cd pr-harmony
mvn clean package -s settings.xml
```

# Debugging

The easiest way to debug this plugin is to use [Atlassian's SDK](https://developer.atlassian.com/docs/getting-started) to run it in a local instance of Bitbucket/Stash. Once you have it set up, simply go into the project directory and run the following (replace `bitbucket` with `stash` if needed):

```
atlas-run --product bitbucket --http-port 7990 --jvmargs "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

This will take some time to run at first, as it has to download the Bitbucket runtime. Once the app comes up, go to http://localhost:7990/bitbucket in your browser. The plugin will already be installed, all you have to do is configure it for the test project. The server will also be started with Java remote debugging enabled. In your IDE, you can connect to the remote debugging port on localhost at port 5005.

The SDK allows you to make live changes by detecting changes to class files and resources in the plugin. However, if for some reason it doesn't redeploy, you can use the `atlas-cli` command to quickly redeploy the plugin instead of restarting the server. Just run `atlas-cli` from the project directory and when the command prompt comes up type `pi`:

## History

This plugin is a clean reimplementation of a couple of plugins I did for the Stash instance we use at work. I wanted to clean some pieces of it up after learning more about the Stash plugin API, and I thought that some users might find it useful as well. I've also expanded it a bit to do some things we didn't have a direct need for at work but that I think makes life easier on developers/admins alike.

## Attributions

[Yin Yang](https://www.iconfinder.com/icons/379351/yang_yin_icon#size=128) icon by [Webalys](https://www.iconfinder.com/webalys)

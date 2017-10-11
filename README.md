[![Build Status](https://travis-ci.org/karlmdavis/ansible-role-jenkins2.svg?branch=master)](https://travis-ci.org/karlmdavis/ansible-role-jenkins2)

Ansible Role for Jenkins 2+
===========================

This [Ansible](https://www.ansible.com/) role can be used to install and manage [Jenkins 2](https://jenkins.io/2.0/).

Requirements
------------

This role requires Ansible 2.4 or later, with either Ansible pipelining available or `setfacl` available on the system being managed (per [Becoming an Unprivileged User](http://docs.ansible.com/ansible/latest/become.html#becoming-an-unprivileged-user)).

Role Variables
--------------

This role supports the following variables, listed here with their default values from [defaults/main.yml](defaults/main.yml):

* `jenkins_release_line`: `'long_term_support'`
    * When set to `long_term_support`, the role will install the LTS releases of Jenkins.
    * When set to `weekly`, the role will install the weekly releases of Jenkins.
* `jenkins_port`: `8080`
    * The port that Jenkins will run on, for HTTP requests.
    * On most systems, this value will need to be over 1024, as Jenkins is not run as `root`.
* `jenkins_context_path`: `''`
    * The context path that Jenkins will be hosted at, e.g. `/foo` in `http://localhost:8080/foo`. Leave as `''` to host at root path.
* `jenkins_url_external`: `''`
    * The external URL that users will use to access Jenkins. Gets set in the Jenkins config and used in emails, webhooks, etc.
    * If this is left empty/None, the configuration will not be set and Jenkins will try to auto-discover this (which won't work correctly if it's proxied).
* `jenkins_admin_users`: `['hudson.security.HudsonPrivateSecurityRealm:admin']`
    * Override this variable to support an alternative authorization system (i.e.  security realm). Note that this doesn't install/configure that realm, it's just needed to ensure that the Jenkins CLI can still be used once you've activated the realm.
    * For example, if you're using the [GitHub OAuth plugin](https://wiki.jenkins-ci.org/display/JENKINS/Github+OAuth+Plugin)'s security realm, you would add an extra entry such as "`org.jenkinsci.plugins.GithubSecurityRealm:your_github_user_id`" as the first element in this list (and leave the `admin` entry, too).
* `jenkins_plugins_extra`: `[]`
    * Override this variable to install additional Jenkins plugins.
    * These would be in addition to the plugins recommended by Jenkins 2's new setup wizard, which are installed automatically by this role (see `jenkins_plugins_recommended` in [defaults/main.yml](defaults/main.yml)).
* `jenkins_java_args_extra`: `''`
    * Additional options that will be added to `JAVA_ARGS` for the Jenkins process, such as the JVM memory settings, e.g. `-Xmx4g`.

Dependencies
------------

This role does not have direct dependencies on other Ansible roles. However, it does require that a Java JRE be available on the system path.

Example Playbook
----------------

This role can be installed, as follows:

    $ ansible-galaxy install karlmdavis.jenkins2

This role can be applied, as follows:

    - hosts: some_box
      tasks:
        - import_role:
            name: karlmdavis.ansible-jenkins2
          vars:
            jenkins_plugins_extra:
              - github-oauth

License
-------

This project is in the worldwide [public domain](LICENSE.md). As stated in [CONTRIBUTING](CONTRIBUTING.md):

> This project is in the public domain within the United States, and copyright and related rights in the work worldwide are waived through the [CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
>
> All contributions to this project will be released under the CC0 dedication. By submitting a pull request, you are agreeing to comply with this waiver of copyright interest.

Author Information
------------------

This plugin was authored by Karl M. Davis (https://justdavis.com/karl/).

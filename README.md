Role Name
=========

This [Ansible](https://www.ansible.com/) role can be used to install and manage [Jenkins 2](https://jenkins.io/2.0/).

![Travis CI Build Status](https://travis-ci.org/karlmdavis/ansible-jenkins2.svg)

Requirements
------------

This role requires Ansible 2.0 or later.

Role Variables
--------------

Available variables are listed below, along with default values (see [defaults/main.yml](defaults/main.yml)):

    # Jenkins doesn't (and shouldn't) run as root, so this must be over 1024.
    jenkins_port: 8080

Override this variable to set the port that Jenkins will run on.

    # The context path that Jenkins will be hosted at, e.g. '/foo' in 
    # 'http://localhost:8080/foo'. Leave as '' to host at root path.
    jenkins_context_path: ''

Override this variable if Jenkins needs to be hosted at a context path/prefix other than the root (default).

    jenkins_admin_users:
      - 'hudson.security.HudsonPrivateSecurityRealm:admin'

Override this variable to support an alternative authorization system (i.e.  security realm). Note that this doesn't install/configure that realm, it's just needed to ensure that the Jenkins CLI can still be used once you've activated the realm. For example, if you're using the [GitHub OAuth plugin](https://wiki.jenkins-ci.org/display/JENKINS/Github+OAuth+Plugin)'s security realm, you would add an extra entry such as "`org.jenkinsci.plugins.GithubSecurityRealm:your_github_user_id`" as the first element in this list (and leave the `admin` entry, too).

    # The additional plugins that users of this role would like to be installed 
    # (must be overridden).
    jenkins_plugins_extra: []

Override this variable to install additional Jenkins plugins. These would be in addition to the plugins recommended by Jenkins 2's new setup wizard, which are installed automatically by this role (see `jenkins_plugins_recommended` in [defaults/main.yml](defaults/main.yml)).

Dependencies
------------

This role does not have direct dependencies on other Ansible roles. However, it does require that a Java JRE be available on the system path.

Example Playbook
----------------

This role can be installed, as follows:

    $ ansible-galaxy install karlmdavis.jenkins2

This role can be applied, as follows:

    - hosts: servers
      vars:
        jenkins_plugins_extra:
          - github-oauth
      roles:
         - ansible-jenkins2

License
-------

This project is in the worldwide [public domain](LICENSE.md). As stated in [CONTRIBUTING](CONTRIBUTING.md):

> This project is in the public domain within the United States, and copyright and related rights in the work worldwide are waived through the [CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
>
> All contributions to this project will be released under the CC0 dedication. By submitting a pull request, you are agreeing to comply with this waiver of copyright interest.

Author Information
------------------

This plugin was authored by Karl M. Davis (https://justdavis.com/karl/).

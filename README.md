[![Build Status](https://travis-ci.org/karlmdavis/ansible-role-jenkins2.svg?branch=master)](https://travis-ci.org/karlmdavis/ansible-role-jenkins2)

Ansible Role for Jenkins 2+
===========================

This [Ansible](https://www.ansible.com/) role can be used to install and manage [Jenkins 2](https://jenkins.io/2.0/).

Requirements
------------

This role requires Ansible 2.4 or later, with either Ansible pipelining available or `setfacl` available on the system being managed (per [Becoming an Unprivileged User](http://docs.ansible.com/ansible/latest/become.html#becoming-an-unprivileged-user)).

The role currently supports Ubuntu 14.04 (Trusty) and Ubuntu 16.04 (Xenial), though contributtions for additional platform support are welcome!

Role Variables
--------------

This role supports the following variables, listed here with their default values from [defaults/main.yml](defaults/main.yml):

* `jenkins_release_line`: `'long_term_support'`
    * When set to `long_term_support`, the role will install the LTS releases of Jenkins.
    * When set to `weekly`, the role will install the weekly releases of Jenkins.
* `jenkins_home`: `/var/lib/jenkins`
    * The directory that (most of) Jenkins data will be stored.
    * Due to limitations of the Jenkins installer, the `jenkins` service account will still use the default as its home directory. This should really only come into play for SSH keys.
* `jenkins_port`: `8080`
    * The port that Jenkins will run on, for HTTP requests.
    * On most systems, this value will need to be over 1024, as Jenkins is not run as `root`.
* `jenkins_context_path`: `''`
    * The context path that Jenkins will be hosted at, e.g. `/foo` in `http://localhost:8080/foo`. Leave as `''` to host at root path.
* `jenkins_url_external`: `''`
    * The external URL that users will use to access Jenkins. Gets set in the Jenkins config and used in emails, webhooks, etc.
    * If this is left empty/None, the configuration will not be set and Jenkins will try to auto-discover this (which won't work correctly if it's proxied).
    * If you set this, chances are that you'll also need to set jenkins_java_args_extra` to also include `-Dorg.jenkinsci.main.modules.sshd.SSHD.hostName=localhost` in order for the CLI (and this role) to work.
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

```yaml
- hosts: some_box
  tasks:
    - import_role:
        name: karlmdavis.ansible-jenkins2
      vars:
        jenkins_plugins_extra:
          - github-oauth
```

## Using the Jenkins CLI

After installing Jenkins, the Jenkins CLI that it installs can also be used to further customize Jenkins.

For example, here's how to install Jenkins and then configure Jenkins to use its `HudsonPrivateSecurityRealm`, for local Jenkins accounts:

```yaml
- hosts: some_box
  tasks:

    - import_role:
        name: karlmdavis.ansible-jenkins2
      vars:
        jenkins_admin_users:
          # Won't be required on first run, but will ensure that tasks using the
          # CLI function on subsequent playbook executions.
          - hudson.security.HudsonPrivateSecurityRealm:test

    # Ensure that Jenkins has restarted, if it needs to.
    - meta: flush_handlers

    # Configure security to use Jenkins-local accounts.
    - name: Configure Security
      shell:
        # We use a here document to pass in a templated Groovy script.
        cmd: |
          cat <<EOF |
          // These are the basic imports that Jenkin's interactive script console
          // automatically includes.
          import jenkins.*;
          import jenkins.model.*;
          import hudson.*;
          import hudson.model.*;

          // Configure the security realm, which handles authentication.
          def securityRealm = new hudson.security.HudsonPrivateSecurityRealm(false)
          if(!securityRealm.equals(Jenkins.instance.getSecurityRealm())) {
            Jenkins.instance.setSecurityRealm(securityRealm)

            // Create a user to login with. Ensure that user is bound to the
            // system-local `jenkins` user's SSH key, to ensure that this
            // account can be used with Jenkins' CLI.
            def testUser = securityRealm.createAccount("test", "supersecret")
            testUser.addProperty(new hudson.tasks.Mailer.UserProperty("foo@example.com"));
            testUser.addProperty(new org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl("{{ jenkins_user_ssh_public_key }}"))
            testUser.save()

            Jenkins.instance.save()
            println "Changed authentication."
          }

          // Configure the authorization strategy, which specifies who can do
          // what.
          def authorizationStrategy = new hudson.security.FullControlOnceLoggedInAuthorizationStrategy()
          if(!authorizationStrategy.equals(Jenkins.instance.getAuthorizationStrategy())) {
            authorizationStrategy.setAllowAnonymousRead(false)
            Jenkins.instance.setAuthorizationStrategy(authorizationStrategy)
            Jenkins.instance.save()
            println "Changed authorization."
          }
          EOF
          # Note the CLI command here uses an "=" sign as the argument for the
          # script to be run, which the Jenkins' CLI interprets as a directive
          # to read the script from STDIN.
          {{ jenkins_cli_command }} groovy =
      become: true
      become_user: jenkins
      register: shell_jenkins_security
      changed_when: "(shell_jenkins_security | success) and 'Changed' not in shell_jenkins_security.stdout"

    # This variable has to be updated if later tasks in the same playbook
    # execution will use the Jenkins CLI.
    - name: Update Jenkins CLI Command
      set_fact:
        jenkins_cli_command: "{{ jenkins_cli_command }} -ssh -user test"
      when: "' -ssh -user test' not in jenkins_cli_command"
```

License
-------

This project is in the worldwide [public domain](LICENSE.md). As stated in [CONTRIBUTING](CONTRIBUTING.md):

> This project is in the public domain within the United States, and copyright and related rights in the work worldwide are waived through the [CC0 1.0 Universal public domain dedication](https://creativecommons.org/publicdomain/zero/1.0/).
>
> All contributions to this project will be released under the CC0 dedication. By submitting a pull request, you are agreeing to comply with this waiver of copyright interest.

Author Information
------------------

This plugin was authored by Karl M. Davis (https://justdavis.com/karl/).

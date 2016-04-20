// These are the basic imports that Jenkin's interactive script console 
// automatically includes.
import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;

// Search the existing users to find 'admin'.
def admin = Jenkins.instance.securityRealm.allUsers.findAll { u -> "admin".equals(u.id) }[0]

// Jenkins users store their SSH keys as a single instance of this property. 
// Multiple keys can be provided here, separated by '\n'. Hat tip to Chef for 
// figuring out how to do this: 
// https://github.com/chef-cookbooks/jenkins/blob/master/libraries/user.rb
def jenkinsUserKey = "{{ jenkins_user_ssh_public_key }}"
def keys = new org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl(jenkinsUserKey)
admin.addProperty(keys)

admin.save()

// Scratch: Trying to figure out how to assign an SSH key for the Jenkins "admin" user.

//println(Jenkins.instance.securityRealm.properties)
//println(Jenkins.instance.securityRealm.allUsers[0].id)

//def admin = Jenkins.instance.securityRealm.allUsers.findAll { u -> "admin".equals(u.id) }[0]
//println("admin.properties: " + admin.properties + "\n")
//def adminSshProperty = admin.getProperty(org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl.class)
//println("adminSshProperty.properties: " + adminSshProperty.properties + "\n")

//org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl

// These are the basic imports that Jenkin's interactive script console 
// automatically includes.
import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;

// Design Note: This script started off simple, but has ended up being a bit of
// a monstrosity, due to a number of constraints and requirements:
// 1. Support Multiple Security Realms: When first installed, Jenkins will be 
//    using the `hudson.security.HudsonPrivateSecurityRealm`, with the default 
//    `admin` account. Many users will wish to switch to a different realm,
//    though. In fact, it's quite possible that the same Ansible playbook run
//    will need to support the default role+user at the start of its execution,
//    and a custom one towards the end of its execution. Imagine a user 
//    installing Jenkins, and using Ansible in that first run to install a 
//    security plugin, configure that plugin, then restart Jenkins, and use 
//    the CLI -- all in the same run.
//    Supporting this means introspecting the active security realm, and 
//    choosing the user whose SSH key will be set based on that.
// 2. Odd User Lookups Behavior: Not sure why, but calling the following line 
//    of code throws odd exceptions if run in an init script, after the GitHub 
//    OAuth security realm has been activated:
//
//        Jenkins.instance.securityRealm.loadUserByUsername("<foo>")
//
//    That code works fine later in the script console, but not in an init
//    script. No clue why.
//    Fortunately, `User.get("foo")` is a reasonable substitute.
// 3. Broken SSH Key Auth Behavior: Don't assign the same public SSH key to two
//    separate users, even if one of them is in a different and inactive 
//    security realm. If the same SSH key is assigned to the default realm's 
//    `admin` user and a user from the GitHub OAuth realm, Jenkins will still 
//    try (and fail) to login the CLI as `admin`, even if the OAuth realm is the
//    active one.
//    The workaround for this is the last section of the script wherein the SSH
//    key is removed from all other users.

// Debugging Note: Output from this script won't appear in webapp log UI, but 
// instead in the system log file, e.g. `/var/log/jenkins/jenkins.log`. 

// This is the list of (possible) admin users that need to allow SSH logins by 
// the server's `jenkins` system user account. It is passed into this script 
// via Ansible templating. Each entry in the list will be a string of the form
// "realm:userId".
def jenkinsAdminUsers = [{{ jenkins_admin_users | quote_items | join(',') }}]
println("Configuring SSH keys for users '" + jenkinsAdminUsers + "'. Current security realm: '" + Jenkins.instance.securityRealm + "'")

// This is the SSH key for the box's `jenkins` system user account. It is 
// passed into this script via Ansible templating.
def jenkinsUserKey = "{{ jenkins_user_ssh_public_key }}"

// What's the current security realm?
def securityRealmClassName = Jenkins.instance.securityRealm.getClass().getName()

// Find the (first) admin ID with the correct realm.
def adminId = null
for(adminData in jenkinsAdminUsers) {
	println("Checking admin SSH data: " + adminData)
	
	def adminEntryRealm = adminData.tokenize(':')[0]
	def adminEntryId = adminData.tokenize(':')[1]
	
	if(adminEntryRealm.equals(securityRealmClassName)) {
		adminId = adminEntryId
		break;
	}
}

// Verify that a matching admin was found.
if(adminId != null) {
	println("Admin match found: " + adminId)
} else {
	println("Admin match not found")
	return;
}

// Can't get the user from the security realm, as some security realms
// (e.g. GitHub OAuth) don't support that operation until *some* user
// has logged in. Instead, we create a basic hudson.model.User record
// for the admin accounts, and trust that Jenkins will merge these with
// the user data from the security realm (which it seems to do).
def admin = User.get(adminId)

// Jenkins users store their SSH keys as a single instance of this property. 
// Multiple keys can be provided here, separated by '\n'. Hat tip to Chef for 
// figuring out how to do this: 
// https://github.com/chef-cookbooks/jenkins/blob/master/libraries/user.rb
def keysProperty = admin.getProperty(org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl)
if(keysProperty == null) {
	keysProperty = new org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl("")
}

// Append the SSH key, if it's not already present.
if(!keysProperty.authorizedKeys.contains(jenkinsUserKey)) {
	keysProperty.authorizedKeys = jenkinsUserKey + "\n" + keysProperty.authorizedKeys
	
	admin.addProperty(keysProperty)
	admin.save()
	println("Added SSH key for user ID: " + adminId)
} else {
	println("SSH key was already configured for user ID: " + adminId)
}

// Remove that SSH key from any other users that have it.
for(user in User.getAll()) {
	// Don't remove it from the active admin.
	if(user.id.equals(adminId)) {
		continue;
	}
	
	def userKeysProperty = user.getProperty(org.jenkinsci.main.modules.cli.auth.ssh.UserPropertyImpl)
	if(userKeysProperty == null) {
		continue;
	}
	
	if(userKeysProperty.authorizedKeys.contains(jenkinsUserKey)) {
		userKeysProperty.authorizedKeys = userKeysProperty.authorizedKeys.replace(jenkinsUserKey, "")
		userKeysProperty.authorizedKeys = userKeysProperty.authorizedKeys.replace("\n\n", "\n")
		userKeysProperty.authorizedKeys = userKeysProperty.authorizedKeys.trim()
		
		user.addProperty(userKeysProperty)
		user.save()
		println("Removed admin SSH key from user: " + user.id)
	}
}
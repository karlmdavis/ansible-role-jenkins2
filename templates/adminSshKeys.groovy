// These are the basic imports that Jenkin's interactive script console 
// automatically includes.
import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;

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
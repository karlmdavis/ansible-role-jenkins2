// 
// This script applies the basic Jenkins configuration settings that this
// Ansible role supports.
// 
// Debugging Note: Output from this script won't appear in webapp log UI, but 
// instead in the system log file, e.g. `/var/log/jenkins/jenkins.log`. 
//

// These are the basic imports that Jenkin's interactive script console 
// automatically includes.
import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;


// Set the Jenkins external URL, if defined.
// (Hat tip: http://stackoverflow.com/questions/30355079/jenkins-setting-root-url-via-groovy-api.)
def externalUrl = "{{ '' if jenkins_url_external == None else (jenkins_url_external | default('') | trim) }}"
def locationConfig = JenkinsLocationConfiguration.get()
println("Configuring Jenkins External URL (current URL: '${locationConfig.url}')...")
if(!externalUrl.equals(locationConfig.url)) {
	locationConfig.url = externalUrl
	locationConfig.save()
	println("External URL updated: '${locationConfig.url}'.")
} else {
	println("External URL not updated.")
}


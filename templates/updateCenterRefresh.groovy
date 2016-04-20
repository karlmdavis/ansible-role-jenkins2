// These are the basic imports that Jenkin's interactive script console 
// automatically includes.
import jenkins.*;
import jenkins.model.*;
import hudson.*;
import hudson.model.*;

// Out of the box, Jenkins has no Update Center metadata, and so the CLI's
// 'install-plugins' command will fail. This script pulls that data.
def updateCenter = Jenkins.instance.updateCenter
def result = updateCenter.updateAllSites()

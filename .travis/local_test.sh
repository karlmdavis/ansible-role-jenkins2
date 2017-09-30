#!/bin/bash

set -e

# Edit these constants to change the test case this script runs.
ansiblePipSpec="ansible"
platform="ubuntu_16_04"
sshPublicKey="$(eval echo ~)/.ssh/id_rsa.pub"

# Determine the directory that this script is in.
scriptDirectory="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "${scriptDirectory}/.."

# Create and activate the Python virtualenv needed by Ansible.
if [[ ! -d "${scriptDirectory}/../venv" ]]; then
  virtualenv -p /usr/bin/python2.7 venvi
fi
source venv/bin/activate
pip install "${ansiblePipSpec}"
pip install -r .travis/requirements.txt

# Prep the Ansible roles that the test will use.
if [[ ! -d .travis/roles ]]; then mkdir .travis/roles; fi
if [[ ! -x .travis/roles/karlmdavis.jenkins2 ]]; then ln -s `pwd` .travis/roles/karlmdavis.jenkins2; fi
ansible-galaxy install --roles-path=.travis/roles --role-file=.travis/install_roles.yml

# Prep the Docker container that will be used.
sudo PLATFORM="${platform}" ./.travis/docker_launch.sh "ansible_test_jenkins2"
cat "${sshPublicKey}" | sudo docker exec --interactive ansible_test_jenkins2.${platform} /bin/bash -c "mkdir /home/ansible_test/.ssh && cat >> /home/ansible_test/.ssh/authorized_keys"

# Ensure that Ansible treats this folder as the project's base.
cd .travis/

# Basic role syntax check
ansible-playbook basic_test.yml --inventory-file=inventory --syntax-check

# Run the Ansible test case.
ansible-playbook basic_test.yml --inventory-file=inventory

# Remove the Docker instance used in the tests.
sudo docker rm -f ansible_test_jenkins2.${platform}

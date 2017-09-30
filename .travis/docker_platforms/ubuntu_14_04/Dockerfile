FROM ubuntu-upstart:14.04
ENV container docker

# Create the SSH user.
RUN adduser ansible_test && adduser ansible_test sudo
RUN echo 'ansible_test ALL=(ALL) NOPASSWD: ALL' > /etc/sudoers.d/ansible_test
RUN echo 'ansible_test:secret' | chpasswd

# Ensure that Python 2.7 is installed, for Ansible.
RUN apt-get update && apt-get install -y python2.7 python

EXPOSE 22
CMD ["/sbin/init"]

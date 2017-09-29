FROM ubuntu:16.04
ENV container docker

# Getting systemd to run correctly inside Docker is very tricky. Need to 
# ensure that it doesn't start things it shouldn't, without stripping out so
# much as to make it useless.
#
# References:
#
# * <https://hub.docker.com/_/centos/>: Good start, but badly broken.
# * <https://github.com/solita/docker-systemd>: For Ubuntu, but works!
RUN find /etc/systemd/system \
         /lib/systemd/system \
         -path '*.wants/*' \
         -not -name '*journald*' \
         -not -name '*systemd-tmpfiles*' \
         -not -name '*systemd-user-sessions*' \
         -exec rm \{} \;
RUN systemctl set-default multi-user.target
STOPSIGNAL SIGRTMIN+3

# Install and start SSH.
#
# References:
#
# * <https://stackoverflow.com/questions/18173889/cannot-access-centos-sshd-on-docker>
RUN apt-get update && apt-get install -y openssh-server sudo
RUN systemctl enable ssh

# Create the SSH user.
RUN adduser ansible_test && adduser ansible_test sudo
RUN echo ansible_test ALL=NOPASSWD:ALL > /etc/sudoers.d/ansible_test
RUN echo 'ansible_test:secret' | chpasswd

# Ensure that Python 2.7 is installed, for Ansible.
RUN apt-get update && apt-get install -y python2.7 python

VOLUME [ "/sys/fs/cgroup" ]
EXPOSE 22
CMD ["/sbin/init"]


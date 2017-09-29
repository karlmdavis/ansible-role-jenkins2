#!/bin/bash

# Bail on the first error.
set -e

# Echo all commands before running them.
set -v

# Grab the container prefix to use from args.
containerPrefix="${1}"

# Build and start the container, running systemd and ssh.
docker build \
	--tag "${containerPrefix}/${PLATFORM}" \
	"./.travis/docker_platforms/${PLATFORM}"
docker run \
	--cap-add=SYS_ADMIN \
	--detach \
	-p 127.0.0.1:13022:22 \
	--volume=/sys/fs/cgroup:/sys/fs/cgroup:ro \
	--tmpfs /run \
	--tmpfs /run/lock \
	--name "${containerPrefix}.${PLATFORM}" \
	"${containerPrefix}/${PLATFORM}"

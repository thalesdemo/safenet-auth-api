#!/bin/bash
POM_VERSION=$(sh mvnw -q help:evaluate -Dexpression=project.version -DforceStdout=true)
ARCHITECTURE=$(uname -m)
DOCKER_HUB=thalesdemo/safenet-auth-api
echo "Repository:   $DOCKER_HUB"
echo "POM Version:  $POM_VERSION"
echo "Architecture: $ARCHITECTURE"
docker build --no-cache \
    --build-arg APP_VERSION=$POM_VERSION \
    --rm=true -t $DOCKER_HUB:$POM_VERSION-$ARCHITECTURE .

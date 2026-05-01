#!/bin/sh

DIR=$(cd "$(dirname "$0")" && pwd)

cd $DIR

export XZIT_HOME=$DIR

ProgramJar=${XZIT_HOME}/xzit-server.jar
LoggingConfig=${XZIT_HOME}/logback-prod.xml
ConfigLocation=classpath:/application.yml,file://${XZIT_HOME}/application-prod.yml

JAX_JAVA_OPTS="-Xmx4096m -Xms512m"

exec java -jar $JAX_JAVA_OPTS -Dspring.config.location=${ConfigLocation} -Dspring.profiles.active=qa -Dlogging.config=${LoggingConfig} ${ProgramJar} "$@" <&- &

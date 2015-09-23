#!/bin/bash

if [ ! -f h2.jar ]; then
	echo "[ERROR] File h2.jar is missing."
  exit
fi

if [ ! -f kwikshop-db.mv.db ]; then
	echo "[ERROR] File kwikshop-db.mv.db is missing."
  exit
fi

liquibase --driver=org.h2.Driver \
     --classpath=h2.jar \
     --changeLogFile=update.sql \
     --url="jdbc:h2:./kwikshop-db" \
     --username=sa \
     migrate

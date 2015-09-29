#!/bin/bash

if [ ! -f liquibase/liquibase ]; then
	echo "[INFO] Liquibase is missing, downloading..."
	mkdir liquibase && cd liquibase
	wget https://github.com/liquibase/liquibase/releases/download/liquibase-parent-3.4.1/liquibase-3.4.1-bin.zip
	unzip liquibase-3.4.1-bin.zip
	cd ..
fi

if [ ! -f h2.jar ]; then
	echo "[ERROR] File h2.jar is missing."
	exit
fi

if [ ! -f kwikshop-db.mv.db ]; then
	echo "[ERROR] File kwikshop-db.mv.db is missing."
	exit
fi

liquibase/liquibase --driver=org.h2.Driver \
     --classpath=h2.jar \
     --changeLogFile=update.sql \
     --url="jdbc:h2:./kwikshop-db" \
     --username=sa \
     migrate

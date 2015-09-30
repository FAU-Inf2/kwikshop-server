#!/bin/bash

CURDATE=$(date +%d.%m.%y-%H)

if [ ! -f h2.jar ]; then
  echo "[INFO] File h2.jar is missing, downloading..."
  wget -q http://www.h2database.com/h2-2015-09-13.zip
  unzip h2-2015-09-13.zip
  cp h2/bin/h2*.jar h2.jar
  rm -rf h2/
fi

if [ ! -f ~/kwikshop-data/kwikshop-db.mv.db ]; then
  echo "[ERROR] File ~/kwikshop-data/kwikshop-db.mv.db is missing."
  exit
fi

cp ~/kwikshop-data/kwikshop-db.mv.db ./

echo "Backing up kwikshop-db.mv.db..."

java -cp h2.jar org.h2.tools.Script -url jdbc:h2:./kwikshop-db \
     -user sa -script dump.sql -options drop

tar cfz "$CURDATE.tgz" dump.sql

rm dump.sql

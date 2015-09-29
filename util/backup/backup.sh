#!/bin/bash

CURDATE=$(date +%d.%m.%y-%H)

if [ ! -f h2.jar ]; then
  echo "[ERROR] File h2.jar is missing."
  exit
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

#!/bin/bash

if [ ! -f h2.jar ]; then
  echo "[ERROR] File h2.jar is missing."
  exit
fi

if [ ! -f dump.sql ]; then
  echo "[ERROR] File dump.sql is missing."
  exit
fi

if [ ! -f kwikshop-db.mv.db ]; then
  echo "[ERROR] File kwikshop-db.mv.db is missing."
  exit
fi

echo "Restoring database dump from dump.sql..."

java -cp h2.jar org.h2.tools.RunScript -url jdbc:h2:./kwikshop-db \
     -user sa -script dump.sql

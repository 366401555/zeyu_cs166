#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
psql -h localhost -p $PGPORT $USER"_DB" < create_tables.sql
psql -h localhost -p $PGPORT $USER"_DB" < create_indexes.sql
psql -h localhost -p $PGPORT $USER"_DB" < load_data.sql
echo "Creating sequence..."
cat <(echo 'CREATE SEQUENCE msgId_seq  START WITH 27812;')|psql -h localhost -p $PGPORT $USER"_DB"
psql -h localhost -p $PGPORT $USER"_DB" < triggers.sql
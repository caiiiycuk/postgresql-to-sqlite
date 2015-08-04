# postgresql-to-sqlite (pg2sqlite) [![Build Status](https://travis-ci.org/caiiiycuk/postgresql-to-sqlite.svg)](https://travis-ci.org/caiiiycuk/postgresql-to-sqlite)

Easy to use solution to create sqlite database from postgresql dump.

* default [`pg_dump`](http://www.postgresql.org/docs/9.4/static/app-pgdump.html) script format
* as fast as possible
* silently ignore unsupported postgresql features
* gzip support

## How to use

```sh
#  Making dump
pg_dump -h host -U user -f database.dump database

#  Making sqlite database
pg2sqlite -d database.dump -o sqlite.db
```

## Command line arguments

`pg2sqlite -d <file> -o <file> [-f <true|false>]`

* `-d <file>` - file that contains dump of postgresql database (made by pg_dump, accepts .gz)
* `-o <file>` - file name of newly created sqlite3 database
* `-f <true|false>` - default: false, force database re-creation if database file alredy exists

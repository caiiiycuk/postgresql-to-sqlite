# postgresql-to-sqlite (pg2sqlite) [![Build Status](https://travis-ci.org/caiiiycuk/postgresql-to-sqlite.svg)](https://travis-ci.org/caiiiycuk/postgresql-to-sqlite)

Easy to use solution to create sqlite database from postgresql dump.

* default [`pg_dump`](http://www.postgresql.org/docs/9.4/static/app-pgdump.html) script format
* as fast as possible
* silently ignore unsupported postgresql features
* gzip support

## Installing

In [release section](https://github.com/caiiiycuk/postgresql-to-sqlite/releases/) you can download pre-built version of pg2sqlite.jar

## How to build
```sh
git clone https://github.com/caiiiycuk/postgresql-to-sqlite.git
cd postgresql-to-sqlite
sbt one-jar
cp target/scala-2.11/postgresql-to-sqlite_2.11-0.0.1-SNAPSHOT-one-jar.jar pg2sqlite.jar
```

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

## Tips

pg2sqlite does not support database schemas. If your dump file includes schema definition It will print errors like this:
```
Create Table - Exception:
unknown database <schema>
[SQL] 'CREATE TABLE <schema>.table (...;'
```
You can easily fix dump file with `sed`:
```
# sed 's/<schema name>\.//' -i  output.dump
$ sed 's/public\.//' -i  output.dump
$ pg2sqlite -d output.dump -o sqlite.db
```
Where `public` is a schema name.


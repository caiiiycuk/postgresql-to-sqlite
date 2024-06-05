# postgresql-to-sqlite (pg2sqlite)
[![Build](https://github.com/caiiiycuk/postgresql-to-sqlite/actions/workflows/build.yml/badge.svg)](https://github.com/caiiiycuk/postgresql-to-sqlite/actions/workflows/build.yml)

Easy to use solution to create sqlite database from postgresql dump.

* default [`pg_dump`](http://www.postgresql.org/docs/9.4/static/app-pgdump.html) script format
* as fast as possible
* silently ignore unsupported postgresql features
* gzip support

## Installing

In [release section](https://github.com/caiiiycuk/postgresql-to-sqlite/releases/) you can download pre-built version of pg2sqlite.jar

## How to use

1. Install jre (java) on your PC

2. Create dump from postgresql database
```sh
pg_dump -h host -U user -f database.dump database
```

3. Make sqlite database from it
```
java -jar pg2sqlite-1.0.3.jar -d database.dump -o sqlite.db
```

## Command line arguments

`pg2sqlite -d <file> -o <file> [-f <true|false>]`

* `**-d** <file>` - file that contains dump of postgresql database (made by pg_dump, accepts .gz)
* `**-o** <file>` - file name of newly created sqlite3 database
* `**-f** <true|false>` - default: false, force database re-creation if database file alredy exists
* `**-t** <integer|text|real>` - default: integer, change sqlite3 date class (read below)

## Timestamps

SQLite does not have a storage class set aside for storing dates and/or times. Instead, the built-in [Date And Time Functions](https://www.sqlite.org/lang_datefunc.html) of SQLite are capable of storing dates and times as TEXT, REAL, or INTEGER values:

* TEXT as ISO8601 strings ("YYYY-MM-DD HH:MM:SS.SSS").
* REAL as Julian day numbers, the number of days since noon in Greenwich on November 24, 4714 B.C. according to the proleptic Gregorian calendar.
* INTEGER as Unix Time, the number of seconds since 1970-01-01 00:00:00 UTC.

By default pg2sqlite uses **INTEGER** to store dates, but you can change this with **-t** argument (`-t text` or `-t real`), use it like this:

```sh
java -jar pg2sqlite-1.0.3.jar -d database.dump -o sqlite.db -t text
```

## Tips

pg2sqlite does not support database schemas. If your dump file includes schema definition It will print errors like this:
```
Create Table - Exception:
unknown database <schema>
[SQL] 'CREATE TABLE <schema>.table (...;'
```
You can easily fix dump file with `sed`:
```sh
# sed 's/<schema name>\.//' -i  database.dump
sed 's/public\.//' -i  database.dump
pg2sqlite -d output.dump -o sqlite.db
```
Where `public` is a schema name.

## How to build
```sh
git clone https://github.com/caiiiycuk/postgresql-to-sqlite.git
cd postgresql-to-sqlite
sbt one-jar
cp target/scala-2.11/postgresql-to-sqlite_2.11-0.0.1-SNAPSHOT-one-jar.jar pg2sqlite.jar
```

## Docker

Clone the repository and run 
```
docker build -t postgresql-to-sqlite:latest .
```
inside the postgresql-to-sqlite folder.

Use 
```
docker run -v /home/john/dbdata:/dbdata -e psource='/dbdata/pqdump.sql' -e starget='/dbdata/output.sqlite'  -it postgresql-to-sqlite:latest
```
where
- -v: is the volume where the pqdump file is located. (and later the output file)
- -e: `psource` is the pqdump filename and folder & `starget` the sqlite filename and folder

p.s. the schema removal has to be done outside the container

## Support

If you appreciate this project, please consider voting for it on Stack Overflow:

https://stackoverflow.com/questions/6148421/how-to-convert-a-postgres-database-to-sqlite/69293251#69293251

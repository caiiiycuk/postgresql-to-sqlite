package com.github.caiiiycuk.pg2sqlite.dsl

import org.scalatest.FlatSpec
import org.scalatest.Matchers
import com.github.caiiiycuk.pg2sqlite.iterator.Line
import com.github.caiiiycuk.pg2sqlite.{Connection, DumpInserter}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import java.io.File

class DumperTest extends FlatSpec with Matchers with BeforeAndAfter {

  val dbFile = new File("test.db")

  private final val DATE_DUMP =
    """
      |CREATE TABLE test (
      |    current timestamp without time zone NOT NULL
      |);
      |
      |COPY test (current) FROM stdin;
      |2024-05-06 15:14:12
      |\.
      |""".stripMargin

  private def makeConnection(dateClass: String = Connection.DEFAULT_DATE_CLASS) = {
    if (dbFile.exists()) {
      dbFile.delete()
    }

    Connection.sqlite(dbFile, dateClass)
  }

  after {
    new File("test.db").delete()
  }

  "dumper" should "generate db from test-case of issue#11" in {
    val connection = makeConnection()
    val inserter = new DumpInserter(connection)
    val dump =
      """
        |CREATE TYPE product_type AS ENUM (
        |    'Material',
        |    'Digital'
        |);
        |
        |CREATE TABLE product (
        |    client_id integer NOT NULL,
        |    order_product integer,
        |    upper_price integer NOT NULL,
        |    lower_price integer NOT NULL,
        |    type product_type NOT NULL,
        |    product_id integer NOT NULL--,
        |    CONSTRAINT product_check CHECK (((lower_price > upper_price) AND (upper_price <= 200))),
        |    CONSTRAINT product_order_product_check CHECK ((order_product > 0)),
        |    CONSTRAINT product_upper_price_check CHECK ((upper_price >= 0))
        |);
        |""".stripMargin
        .split("\n")
        .zipWithIndex
        .map {
          case (text, num) =>
            Line(num, text)
        }

    inserter.insert(dump.iterator)
    connection.close
  }

  "dumper" should "should respect date class (Default)" in {
    val connection = makeConnection()
    val inserter = new DumpInserter(connection)
    val dump = DATE_DUMP.split("\n")
      .zipWithIndex
      .map {
        case (text, num) =>
          Line(num, text)
      }

    inserter.insert(dump.iterator)
    connection.withStatement { statment =>
      val rs = statment.executeQuery("SELECT * FROM test")
      rs.next() should equal(true)
      rs.getLong(1) > 0 should equal(true)
      rs.close()
    }
    connection.close
  }

  "dumper" should "should respect date class (text)" in {
    val connection = makeConnection(Connection.TEXT_DATE_CLASS)
    val inserter = new DumpInserter(connection)
    val dump = DATE_DUMP.split("\n")
      .zipWithIndex
      .map {
        case (text, num) =>
          Line(num, text)
      }

    inserter.insert(dump.iterator)
    connection.withStatement { statment =>
      val rs = statment.executeQuery("SELECT * FROM test")
      rs.next() should equal(true)
      rs.getString(1) should equal("2024-05-06 15:14:12.000")
      rs.close()
    }
    connection.close
  }

  "dumper" should "should respect date class (real)" in {
    val connection = makeConnection(Connection.REAL_DATE_CLASS)
    val inserter = new DumpInserter(connection)
    val dump = DATE_DUMP.split("\n")
      .zipWithIndex
      .map {
        case (text, num) =>
          Line(num, text)
      }

    inserter.insert(dump.iterator)
    connection.withStatement { statment =>
      val rs = statment.executeQuery("SELECT * FROM test")
      rs.next() should equal(true)
      rs.getDouble(1) > 0 should equal(true)
      rs.close()
    }
    connection.close
  }

}

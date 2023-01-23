package com.github.caiiiycuk.pg2sqlite.dsl

import com.github.caiiiycuk.pg2sqlite.iterator.Line
import com.github.caiiiycuk.pg2sqlite.{Connection, DumpInserter}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

import java.io.File

class DumperTest extends FlatSpec with Matchers with BeforeAndAfter {

  val dbFile = new File("test.db")

  private def makeConnection() = {
    if (dbFile.exists()) {
      dbFile.delete()
    }

    Connection.sqlite(dbFile)
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
}

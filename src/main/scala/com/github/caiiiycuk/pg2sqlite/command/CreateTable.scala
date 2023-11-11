package com.github.caiiiycuk.pg2sqlite.command

import java.sql.SQLException
import scala.annotation.tailrec
import com.github.caiiiycuk.pg2sqlite.Connection
import com.github.caiiiycuk.pg2sqlite.iterator.Line
import com.github.caiiiycuk.pg2sqlite.schema.Schema
import com.github.caiiiycuk.pg2sqlite.Log
import com.github.caiiiycuk.pg2sqlite.dsl.DSL._

object CreateTable extends Command with Log {

  private final val TABLE_NAME_POSITON = 2
  private final val activator = """^(?i)create\s+table""".r

  override def matchHead(head: String): Boolean = {
    activator.findFirstIn(head).isDefined
  }

  override def apply(connection: Connection, iterator: Iterator[Line])(implicit schema: Schema) = {
    val rows = takeUntil(iterator, _.contains(";"))
    val rawSql = rows.mkString(" ")

    val (tableName, sql) = try {
      val table = rawSql.tokens(TABLE_NAME_POSITON)
      val columns = rawSql.takeBraces.head.columns

      columns.foreach { column =>
        schema.addColumn(table, column)
      }

      (table, s"CREATE TABLE [$table] (${columns.map(column => s"[${column.name}]").mkString(", ")});")
    } catch {
      case t: Throwable =>
        throw CommandException(s"CREATE TABLE - Unable to find TABLE NAME or COLUMNS in '$rawSql'",
          t, rawSql, rows)
    }

    if (schema.shouldExcludeTable(tableName)) {
      log.info(s"Skipping '$sql'")
    } else {
      try {
        connection.execute(sql)
      } catch {
        case e: SQLException =>
          throw CommandException("Create Table", e, sql, rows)
      }
    }
  }

}

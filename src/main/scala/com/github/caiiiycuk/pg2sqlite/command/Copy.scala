package com.github.caiiiycuk.pg2sqlite.command

import com.github.caiiiycuk.pg2sqlite.Connection
import com.github.caiiiycuk.pg2sqlite.Log
import com.github.caiiiycuk.pg2sqlite.values.LineToValues
import java.sql.SQLException
import com.github.caiiiycuk.pg2sqlite.iterator.Line
import com.github.caiiiycuk.pg2sqlite.schema.Schema
import com.github.caiiiycuk.pg2sqlite.values.ValueParseException
import com.github.caiiiycuk.pg2sqlite.dsl.DSL._

object Copy extends Command with Log {

  import LineToValues._

  private val TABLE_NAME_POSITION = 1
  private val activator = "^(?i)copy".r

  override def matchHead(head: String): Boolean = {
    activator.findFirstIn(head).isDefined
  }

  override def apply(connection: Connection, iterator: Iterator[Line])(implicit schema: Schema) = {
    val rows = takeUntil(iterator, _.contains(";"))
    val rawSql = rows.mkString(" ")

    val (tableName, sql, columnTypes) = try {
      val tableName = rawSql.tokens(TABLE_NAME_POSITION)
      val columns = rawSql.takeBraces.head.columns.map(_.name).toList

      val marks = ("?," * columns.size).dropRight(1)
      val sql = s"insert into $tableName(${columns.map(column => s"[$column]").mkString(",")}) values($marks)"

      val columnTypes = schema.columnsToTypeConstants(tableName, columns)

      (tableName, sql, columnTypes)
    } catch {
      case t: Throwable =>
        throw CommandException(s"COPY - Unable to find TABLE NAME or COLUMNS in '$rawSql'",
          t, rawSql, rows)
    }

    if (schema.shouldExcludeTable(tableName)) {
      log.info(s"Skipping '$sql'")
    } else {
      log.info(s"COPY table '$tableName'")
      connection.withPreparedStatement(sql) { statement =>
        iterator.takeWhile(!_.startsWith("\\.")).foreach { row =>
          val values = try {
            toValues(row.text)(columnTypes)
          } catch {
            case e: ValueParseException =>
              throw CommandException("COPY", e, sql, rows,
                List(s"[DATA #${row.num}] '$row'",
                  s"[COLUMN,TYPE] ${schema.columns(tableName).map(_.toString).mkString(" ")}"))
          }

          try {
            values.foreach(_.apply(statement))
            statement.executeUpdate()
          } catch {
            case e: SQLException =>
              val vals = values.map(_.toString).mkString(", ")
              throw CommandException("COPY", e, sql, rows,
                List(s"[DATA #${row.num}] '$row'", s"[VALUES] '$vals'"))
          }

        }
      }
    }
  }

}

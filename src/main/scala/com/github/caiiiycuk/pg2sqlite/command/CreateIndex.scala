package com.github.caiiiycuk.pg2sqlite.command

import java.sql.SQLException
import scala.annotation.tailrec
import com.github.caiiiycuk.pg2sqlite.Connection
import com.github.caiiiycuk.pg2sqlite.iterator.Line
import com.github.caiiiycuk.pg2sqlite.schema.Schema
import com.github.caiiiycuk.pg2sqlite.dsl.DSL._
import com.github.caiiiycuk.pg2sqlite.Log

object CreateIndex extends Command with Log {

  private val INDEX_NAME_POSITION = 2
  private val TABLE_NAME_POSITION = 0
  private val activator = """^(?i)create\s+index""".r

  override def matchHead(head: String): Boolean = {
    activator.findFirstIn(head).isDefined
  }

  override def apply(connection: Connection, iterator: Iterator[Line])(implicit schema: Schema) = {
    val rows = takeUntil(iterator, _.contains(";"))
    val rawSql = rows.mkString(" ").toLowerCase

    val (tableName, sql, columns) = try {
      val createIndexParts = rawSql.split("""\s+on\s+""")
      val indexName = createIndexParts(0).tokens(INDEX_NAME_POSITION)
      val tableName = createIndexParts(1).tokens(TABLE_NAME_POSITION)
      val columns = rawSql.takeBraces.head.columns.map(column => s"[${column.name}]").mkString(",")

      (tableName, s"CREATE INDEX $indexName ON $tableName ($columns)", columns)
    } catch {
      case t: Throwable =>
        throw CommandException(s"CREATE INDEX - Unable to find INDEX_NAME or TABLE NAME or COLUMNS in '$rawSql'",
          t, rawSql, rows)
    }

    if (schema.shouldExcludeTable(tableName) ||
      columns.isEmpty) {
      log.info(s"Skipping '$sql'")
    } else {
      try {
        connection.execute(sql)
      } catch {
        case e: SQLException =>
          throw CommandException("Create Index", e, sql, rows)
      }
    }
  }

}

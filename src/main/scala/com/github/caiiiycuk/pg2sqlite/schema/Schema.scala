package com.github.caiiiycuk.pg2sqlite.schema

import java.sql.Types

import scala.collection.mutable.Map

class Schema(excludeTables: Set[String] = Set("sqlite_stat")) {

  protected case class Table(columns: Map[String, Column] = Map.empty)

  val tables: Map[String, Table] = Map.empty

  def addColumn(tableName: String, column: Column) = {
    val loweredTableName = tableName.toLowerCase
    val table = tables.get(loweredTableName).getOrElse {
      val table = Table()
      tables += ((loweredTableName, table))
      table
    }
    table.columns += ((column.name.toLowerCase, column))
  }

  def columns(tableName: String) = {
    tables.get(tableName.toLowerCase).map(_.columns).getOrElse(Map.empty)
  }

  def columnsToTypeConstants(tableName: String, columns: List[String]): scala.collection.immutable.Map[Int, Int] = {
    tables.get(tableName.toLowerCase).map { table =>
      columns.zipWithIndex.flatMap {
        case (column, index) =>
          table.columns.get(column).flatMap { column =>
            column.typeConstant.map((index + 1, _))
          }
      }.toMap
    }.getOrElse(scala.collection.immutable.Map.empty)
  }

  def shouldExcludeTable(table: String) = {
    excludeTables.contains(table.toLowerCase)
  }

}

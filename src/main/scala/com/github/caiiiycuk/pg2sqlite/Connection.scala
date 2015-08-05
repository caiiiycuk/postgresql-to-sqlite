package com.github.caiiiycuk.pg2sqlite

import java.sql.DriverManager
import java.sql.Statement
import java.sql.PreparedStatement
import scala.collection.mutable.ListBuffer
import java.sql.ResultSet
import scala.annotation.tailrec
import java.io.File

trait ConnectionHolder {
  def makeConnection: java.sql.Connection
  def db: String
}

object Connection {
  final val FETCH_SIZE = 8192
  final val MAX_VARIABLE_NUMBER = 999

  def sqlite(dbFile: File) = {
    val connectionHolder = new ConnectionHolder {
      override def makeConnection: java.sql.Connection = {
        implicit val connection = DriverManager.getConnection(s"jdbc:sqlite:$dbFile")

        connection.setAutoCommit(true)
        sqlitePragmas()

        connection.setAutoCommit(false)
        connection
      }

      override def db = dbFile.toString
    }

    new Connection(connectionHolder)
  }

  private def sqlitePragmas()(implicit connection: java.sql.Connection) = {
    val statment = connection.createStatement()
    statment.executeUpdate("PRAGMA synchronous = OFF")
    statment.executeUpdate("PRAGMA journal_mode = OFF")
    statment.executeUpdate("PRAGMA threads = 64")
    statment.executeUpdate("PRAGMA max_page_count = 2147483646")
    statment.executeUpdate("PRAGMA cache_size = 65536")
    statment.executeUpdate("PRAGMA cache_spill = true")
    statment.close
  }
}

class Connection(connectionHolder: ConnectionHolder) {

  import Connection._

  final val MAX_VARIABLE_NUMBER = Connection.MAX_VARIABLE_NUMBER

  lazy val connection = connectionHolder.makeConnection

  lazy val db = connectionHolder.db

  def withStatement[T](block: (Statement) => T): T = {
    val statement = connection.createStatement()
    val t = block(statement)
    statement.close
    t
  }

  def withPreparedStatement[T](sql: String, keepAlive: Boolean = false)(block: (PreparedStatement) => T): T = {
    val statement = connection.prepareStatement(sql)
    statement.setFetchSize(FETCH_SIZE)

    val t = block(statement)
    if (!keepAlive) statement.close
    t
  }

  def close = {
    connection.commit
    connection.close
  }

  def execute(sql: String) = {
    withStatement { statement =>
      statement.executeUpdate(sql)
    }
  }

}

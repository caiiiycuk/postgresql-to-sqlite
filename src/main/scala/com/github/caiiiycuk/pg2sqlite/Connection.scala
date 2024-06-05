package com.github.caiiiycuk.pg2sqlite

import org.sqlite.SQLiteConfig

import java.sql.DriverManager
import java.sql.Statement
import java.sql.PreparedStatement
import scala.collection.mutable.ListBuffer
import java.sql.ResultSet
import scala.annotation.tailrec
import java.io.File
import java.util.Properties

trait ConnectionHolder {
  def makeConnection: java.sql.Connection

  def db: String
}

object Connection {
  final val DEFAULT_DATE_CLASS = "INTEGER"
  final val TEXT_DATE_CLASS = "TEXT"
  final val REAL_DATE_CLASS = "REAL"
  private final val DATE_CLASS_PRAGMA = "date_class"
  private final val FETCH_SIZE = 8192
  private final val MAX_VARIABLE_NUMBER = 999

  def sqlite(dbFile: File, dateClass: String = DEFAULT_DATE_CLASS): Connection = {
    val connectionHolder = new ConnectionHolder {
      override def makeConnection: java.sql.Connection = {
        val properties = new Properties()
        properties.setProperty(DATE_CLASS_PRAGMA, dateClass)
        implicit val connection = DriverManager.getConnection(s"jdbc:sqlite:$dbFile", properties)

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
    assert(SQLiteConfig.Pragma.DATE_CLASS.pragmaName.equals(DATE_CLASS_PRAGMA));
    val statement = connection.createStatement()
    statement.executeUpdate(s"PRAGMA ${SQLiteConfig.Pragma.SYNCHRONOUS.pragmaName} = OFF")
    statement.executeUpdate(s"PRAGMA ${SQLiteConfig.Pragma.JOURNAL_MODE.pragmaName} = OFF")
    statement.executeUpdate(s"PRAGMA ${SQLiteConfig.Pragma.LIMIT_WORKER_THREADS.pragmaName} = 64")
    statement.executeUpdate(s"PRAGMA ${SQLiteConfig.Pragma.MAX_PAGE_COUNT.pragmaName} = 2147483646")
    statement.executeUpdate(s"PRAGMA ${SQLiteConfig.Pragma.CACHE_SIZE.pragmaName} = 65536")
    statement.executeUpdate("PRAGMA cache_spill = true")
    statement.close
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

package com.github.caiiiycuk.pg2sqlite.values

import java.sql.PreparedStatement
import java.sql.Types
import java.util.Date

abstract class Value(index: Int) {
  def apply(statement: PreparedStatement)
}

case class NullValue(index: Int, sqlType: Option[Int]) extends Value(index) {
  def apply(statement: PreparedStatement) {
    statement.setNull(index, sqlType.getOrElse(Types.BIGINT))
  }
}

case class BooleanValue(index: Int, value: Boolean) extends Value(index) {
  def apply(statement: PreparedStatement) {
    statement.setBoolean(index, value)
  }
}

case class RealValue(index: Int, value: Double) extends Value(index) {
  def apply(statement: PreparedStatement) {
    statement.setDouble(index, value)
  }
}

case class IntegerValue(index: Int, value: Long) extends Value(index) {
  def apply(statement: PreparedStatement) {
    statement.setLong(index, value)
  }
}

case class StringValue(index: Int, value: String) extends Value(index) {
  def apply(statement: PreparedStatement) {
    statement.setString(index, value)
  }
}

case class BlobValue(index: Int, value: Array[Byte]) extends Value(index) {
  def apply(statement: PreparedStatement) {
    statement.setBytes(index, value)
  }
}

case class DateValue(index: Int, value: Date, dateType: Int) extends Value(index) {
  def apply(statement: PreparedStatement) {
    dateType match {
      case Types.DATE =>
        statement.setDate(index, new java.sql.Date(value.getTime))
      case Types.TIME =>
        statement.setTime(index, new java.sql.Time(value.getTime))
      case _ =>
        statement.setTimestamp(index, new java.sql.Timestamp(value.getTime))
    }
  }
}

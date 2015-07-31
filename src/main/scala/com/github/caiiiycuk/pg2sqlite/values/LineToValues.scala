package com.github.caiiiycuk.pg2sqlite.values

import java.sql.Types
import java.util.Formatter.DateTime
import java.text.SimpleDateFormat
import java.util.Date

object LineToValues {

  val DOUBLE = """^\d+\.\d+$""".r
  val INTEGER = """^\d+$""".r

  val SIMPLE_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  val SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd")
  val SIMPLE_TIME_FORMAT = new SimpleDateFormat("HH:mm:ss")

  val FORMATTER = Map(Types.DATE -> SIMPLE_DATE_FORMAT,
    Types.TIME -> SIMPLE_TIME_FORMAT,
    Types.TIMESTAMP -> SIMPLE_TIMESTAMP_FORMAT)

  val NO_HEX_DIGITS = """[^0-9A-Fa-f]""".r

  def toValues(line: String)(implicit indexToType: Map[Int, Int]): List[Value] = {
    val parts = line.split("\t").map(_.trim)
    parts.zipWithIndex.map {
      case (value, index) =>
        toValue(index + 1, value)
    }.toList
  }

  def toValue(index: Int, value: String)(implicit indexToType: Map[Int, Int]) = {
    if (value == """\N""") {
      NullValue(index, indexToType.get(index))
    } else {
      indexToType.get(index).map { sqlType =>
        toValueWithKnownType(index, value, sqlType)
      }.getOrElse {
        value match {
          case DOUBLE(_*) =>
            toDoubleWithStringFallback(index, value)
          case INTEGER(_*) =>
            toIntegerWithDoubleFallback(index, value)
          case _ =>
            StringValue(index, value)
        }
      }
    }
  }

  def toValueWithKnownType(index: Int, value: String, sqlType: Int) = {
    sqlType match {
      case Types.BIGINT =>
        toIntegerWithDoubleFallback(index, value)
      case Types.DOUBLE | Types.NUMERIC =>
        toDoubleWithStringFallback(index, value)
      case Types.VARCHAR =>
        StringValue(index, value)
      case Types.BOOLEAN =>
        BooleanValue(index, value.toLowerCase != "f")
      case Types.TIMESTAMP | Types.TIME | Types.DATE =>
        val date = toDate(value, sqlType).getOrElse {
          throw new ValueParseException(s"[COLUMN#${index}] Doesn`t know how to convert string '$value', to timestamp")
        }
        DateValue(index, date, sqlType)
      case Types.BLOB =>
        BlobValue(index, hex2bytes(value))
      case _ =>
        throw new ValueParseException(s"[COLUMN#${index}] Doesn`t know how to convert string '$value', to sql type '$sqlType'")
    }
  }

  private def toDate(value: String, sqlType: Int): Option[Date] = {
    val formatter = FORMATTER(sqlType)

    try {
      Some(formatter.parse(value.take(formatter.toPattern().length)))
    } catch {
      case t: Throwable =>
        None
    }
  }

  private def toIntegerWithDoubleFallback(index: Int, value: String) = {
    try {
      IntegerValue(index, value.toLong)
    } catch {
      case e: NumberFormatException =>
        toDoubleWithStringFallback(index, value)
    }
  }

  private def toDoubleWithStringFallback(index: Int, value: String) = {
    try {
      RealValue(index, value.toDouble)
    } catch {
      case e: NumberFormatException =>
        StringValue(index, value)
    }
  }

  private def hex2bytes(value: String): Array[Byte] = {
    if (value.length % 2 != 0 || NO_HEX_DIGITS.findFirstIn(value).isDefined) {
      value.getBytes
    } else {
      javax.xml.bind.DatatypeConverter.parseHexBinary(value)
    }
  }

}

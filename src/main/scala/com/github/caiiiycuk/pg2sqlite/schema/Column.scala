package com.github.caiiiycuk.pg2sqlite.schema

import java.sql.Types

object Column {
  val TYPE_DETECTORS = List(
    ("""boolean""".r -> Types.BOOLEAN),
    ("""int""".r -> Types.BIGINT),
    ("""float""".r -> Types.DOUBLE),
    ("""numeric""".r -> Types.NUMERIC),
    ("""bytea""".r -> Types.BLOB),
    ("""geometry""".r -> Types.BLOB),
    ("""timestamp""".r -> Types.TIMESTAMP),
    ("""time""".r -> Types.TIME),
    ("""date""".r -> Types.DATE),
    ("""char""".r -> Types.VARCHAR),
    ("""text""".r -> Types.VARCHAR))
}

case class Column(name: String, sqlType: Option[String]) {

  import Column._

  lazy val typeConstant = sqlType.map {
    sqlType =>
      val nativeType = TYPE_DETECTORS.find {
        case (regex, _) =>
          regex.findFirstIn(sqlType).isDefined
      }

      nativeType.map(_._2)
  }.flatten

}

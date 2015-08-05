package com.github.caiiiycuk.pg2sqlite.command

import com.github.caiiiycuk.pg2sqlite.iterator.Line

case class CommandException(command: String, cause: Throwable, context: List[String])
  extends Exception(s"""
$command - Exception:
\t${cause.getMessage}
\t${context.mkString("\n\t")},
""", cause)

object CommandException {
  def apply(command: String, cause: Throwable, sql: String, rows: List[Line], context: List[String] = Nil): CommandException = {
    val default = List(s"[SQL] '$sql'", s"[LINE #${rows.head.num}] ${rows.mkString(" ")}")
    CommandException(command, cause, default ++ context)
  }
}

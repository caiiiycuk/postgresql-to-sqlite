package com.github.caiiiycuk.pg2sqlite.command

import scala.util.matching.Regex
import scala.annotation.tailrec
import com.github.caiiiycuk.pg2sqlite.Connection
import com.github.caiiiycuk.pg2sqlite.iterator.Line
import com.github.caiiiycuk.pg2sqlite.schema.Schema

trait Command {

  def matchHead(head: Line): Boolean =
    matchHead(head.text)

  def matchHead(head: String): Boolean

  def apply(connection: Connection, iterator: Iterator[Line])(implicit schema: Schema)

  @tailrec
  final protected def takeUntil(iterator: Iterator[Line],
                                when: (String) => Boolean,
                                buffer: List[Line] = Nil): List[Line] = {
    if (!iterator.hasNext) {
      buffer.reverse
    } else {
      val line = iterator.next
      val newBuffer = line :: buffer

      if (when(line.text)) {
        newBuffer.reverse
      } else {
        takeUntil(iterator, when, newBuffer)
      }
    }
  }

}

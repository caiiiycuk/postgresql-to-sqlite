package com.github.caiiiycuk.pg2sqlite

import scala.annotation.tailrec
import com.github.caiiiycuk.pg2sqlite.command._
import com.github.caiiiycuk.pg2sqlite.iterator.Line
import com.github.caiiiycuk.pg2sqlite.schema.Schema

object DumpInserter {
  val COMMANDS = List(CreateTable, Copy, CreateIndex)
}

class DumpInserter(connection: Connection) {

  import DumpInserter._

  implicit val schema = new Schema()

  @tailrec
  final def insert(iterator: Iterator[Line]): Unit = {
    if (iterator.hasNext) {
      val head = iterator.next()
      val fullIterator = Iterator(head) ++ iterator

      COMMANDS.find(_.matchHead(head)).map { command =>
        command.apply(connection, fullIterator)
      }

      insert(iterator)
    }
  }

}

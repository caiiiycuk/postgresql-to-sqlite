package com.github.caiiiycuk.pg2sqlite

import com.github.caiiiycuk.pg2sqlite.command.CommandException
import com.github.caiiiycuk.pg2sqlite.iterator.LineIterator
import com.github.caiiiycuk.pg2sqlite.values.ValueParseException

import ch.qos.logback.classic.Level

object Boot extends App with Log {

  val root = org.slf4j.LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME).asInstanceOf[ch.qos.logback.classic.Logger]
  root.setLevel(Level.INFO)

  val config = Config.parse(args)
  import config._

  val size = pgdump.length()
  val connection = Connection.sqlite(sqlite)
  val iterator = LineIterator(pgdump)
  val loggedIterator = LoggedIterator(iterator, () => 100.0 * iterator.readed / size)
  val dumpInserter = new DumpInserter(connection)

  log.info(s"'$pgdump' (${toMb(size)} Mb) -> '$sqlite'")

  val success = try {
    dumpInserter.insert(loggedIterator)
    true
  } catch {
    case e: CommandException =>
      log.error(e.getMessage)
      false
    case e: ValueParseException =>
      log.error(e.getMessage)
      false
    case e: Throwable =>
      log.error(e.getMessage, e)
      false
  }

  iterator.close
  connection.close

  if (success) {
    log.info("Well done...")
  } else {
    log.error("Task failed...")
  }
}

package com.github.caiiiycuk.pg2sqlite

import org.slf4j.LoggerFactory

trait Log {

  protected lazy val log = LoggerFactory.getLogger(getClass)

  def toMb(length: Long) = {
    length / 1024 / 1024
  }

  def humanizeMsTime(time: Long) = {
    val ms = time % 1000
    val s = time / 1000 % 60
    val m = time / 1000 / 60

    s"${m}m ${s}s ${ms}ms"
  }

  def humanizeElapsedAndRemaning(startAt: Long, progress: Double): String = {
    val elapsed = System.currentTimeMillis - startAt
    val remaining = (elapsed / progress - elapsed).toInt

    s"elapsed: ${humanizeMsTime(elapsed)} / remaining: ${humanizeMsTime(remaining)}"
  }

}

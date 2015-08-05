package com.github.caiiiycuk.pg2sqlite

object LoggedIterator {
  final val DEFAULT_SENSIVITY = 10
}

case class LoggedIterator[T](iterator: Iterator[T],
                             progress: () => Double, sensivity: Int = LoggedIterator.DEFAULT_SENSIVITY)
    extends Iterator[T] with Log {

  val startAt = System.currentTimeMillis
  var currentProgress: Long = 0L

  override def hasNext = iterator.hasNext

  override def next(): T = {
    val value = iterator.next
    val newProgress = progress()
    val intProgress = (newProgress * sensivity).toLong

    if (intProgress > currentProgress) {
      val elapsedAndRemaining = humanizeElapsedAndRemaning(startAt, newProgress / 100)
      log.info(s"Progress ${intProgress.toDouble / sensivity}%, ${elapsedAndRemaining}...\t")
      currentProgress = intProgress
    }

    value
  }

}

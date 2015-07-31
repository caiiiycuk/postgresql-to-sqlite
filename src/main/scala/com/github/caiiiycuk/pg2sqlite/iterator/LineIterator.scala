package com.github.caiiiycuk.pg2sqlite.iterator

import java.io.FileReader
import java.io.BufferedReader
import java.io.Closeable
import java.io.File
import scala.collection.TraversableOnce.flattenTraversableOnce

trait LineIterator extends Iterator[Line] with Closeable {
  def readed: Long
}

class FileOptionStringIterator(file: File) extends Iterator[Option[String]] with Closeable {

  var readed = 0L

  private val reader = new FileReader(file) {
    override def read(buf: Array[Char], off: Int, len: Int) = {
      val count = super.read(buf, off, len)
      readed += count
      count
    }
  }

  private val bufferedReader = new BufferedReader(reader)

  private var current = Option(bufferedReader.readLine())

  override def hasNext: Boolean = {
    current.nonEmpty
  }

  override def next(): Option[String] = {
    val value = current
    current = Option(bufferedReader.readLine())
    value
  }

  override def close(): Unit = {
    bufferedReader.close
  }

}

object LineIterator {
  def apply(file: File) = {
    val iterator = new FileOptionStringIterator(file)
    val flatIterator = iterator.flatten.zipWithIndex.map {
      case (text, index) =>
        Line(index + 1, text)
    }

    new LineIterator {
      override def hasNext: Boolean = flatIterator.hasNext
      override def next(): Line = flatIterator.next()
      override def close = iterator.close
      override def readed: Long = iterator.readed
    }
  }
}

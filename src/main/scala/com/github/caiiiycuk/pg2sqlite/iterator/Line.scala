package com.github.caiiiycuk.pg2sqlite.iterator

case class Line(num: Int, text: String) {
  def startsWith(value: String) =
    text.startsWith(value)

  override def toString(): String = text
}

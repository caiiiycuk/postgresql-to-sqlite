package com.github.caiiiycuk.pg2sqlite.dsl

import scala.annotation.tailrec
import com.github.caiiiycuk.pg2sqlite.schema.Column

class DSL(line: String) {

  import DSL._

  def dropBraces: String =
    dropBraces(line.toIterator)

  def takeBraces: List[String] = {
    takeBraces(line.toIterator)
  }

  def commaSplitRespectBraces: List[String] = {
    commaSplitRespectBraces(line.toIterator)
  }

  def tokens: List[String] = {
    line.replaceAll("\"|'","").split("""\s|:|,|\(|\)""").map(_.trim).filterNot(_.isEmpty).toList
  }

  def columns: List[Column] = {
    val columns = commaSplitRespectBraces(line.toIterator).map(_.trim).filterNot(_.isEmpty)

    columns.map(_.replaceAll("\"|'", "")).flatMap { columnDefenition =>
      val partials = columnDefenition.split("""\s""")
        .map(_.trim.toLowerCase).filterNot(_.isEmpty).toList

      partials match {
        case head :: _ if head.startsWith("constraint") =>
          None
        case head :: _ if head.startsWith("to_tsvector") =>
          val name = columnDefenition.takeBraces.head.tokens.last
          Some(Column(name, None))
        case head :: _ if head.startsWith("lower") || head.startsWith("upper") =>
          val name = columnDefenition.takeBraces.head.tokens.head
          Some(Column(name, None))
        case head :: sqlType :: _ =>
          Some(Column(head, Some(sqlType)))
        case head :: Nil =>
          Some(Column(head, None))
        case _ =>
          None
      }
    }
  }

  @tailrec
  private def takeBraces(line: Iterator[Char], nesting: Int = 0,
                         acc: String = "", buff: List[String] = Nil): List[String] =
    if (line.hasNext) {
      val head = line.next

      val newAcc = if (nesting > 1 || (nesting > 0 && head != ')')) {
        acc + head
      } else {
        acc
      }

      if (head == '(') {
        takeBraces(line, nesting + 1, newAcc, buff)
      } else if (head == ')' && nesting == 1) {
        takeBraces(line, nesting - 1, "", newAcc :: buff)
      } else if (head == ')') {
        takeBraces(line, nesting - 1, newAcc, buff)
      } else {
        takeBraces(line, nesting, newAcc, buff)
      }
    } else if (acc.nonEmpty) {
      (acc :: buff).reverse
    } else {
      buff.reverse
    }

  @tailrec
  private def dropBraces(line: Iterator[Char], nesting: Int = 0, buff: String = ""): String =
    if (line.hasNext) {
      val head = line.next

      if (head == '(') {
        dropBraces(line, nesting + 1, buff)
      } else if (head == ')') {
        dropBraces(line, nesting - 1, buff)
      } else if (nesting == 0) {
        dropBraces(line, nesting, buff + head)
      } else {
        dropBraces(line, nesting, buff)
      }
    } else {
      buff
    }

  @tailrec
  private def commaSplitRespectBraces(line: Iterator[Char], nesting: Int = 0,
                                      acc: String = "", buff: List[String] = Nil): List[String] =
    if (line.hasNext) {
      val head = line.next

      if (head == '(') {
        commaSplitRespectBraces(line, nesting + 1, acc + head, buff)
      } else if (head == ')') {
        commaSplitRespectBraces(line, nesting - 1, acc + head, buff)
      } else if (head == ',' && nesting == 0) {
        commaSplitRespectBraces(line, nesting, "", acc :: buff)
      } else {
        commaSplitRespectBraces(line, nesting, acc + head, buff)
      }
    } else if (acc.nonEmpty) {
      (acc :: buff).reverse
    } else {
      buff.reverse
    }

}

object DSL {

  implicit def toDSLClass(line: String): DSL = {
    new DSL(line)
  }

}

package com.temerev.mi.analytics

import java.time.{Duration, Instant}

import com.miriamlaurel.fxcore.market.{Snapshot, Quote}
import com.miriamlaurel.fxcore._
import org.scalatest.FunSuite

import scala.io.Source

class WindowTest extends FunSuite {

  private val startTs = Instant.ofEpochMilli(1453199819110L)
  private val startQuote = Quote(EURUSD, Some(1), Some(10), startTs)
  private val quotes = List(
    startQuote,
    startQuote.copy(bid = Some(2), ask = Some(8), timestamp = startTs.plusSeconds(10)),
    startQuote.copy(bid = Some(2), ask = Some(8), timestamp = startTs.plusSeconds(20)),
    startQuote.copy(bid = Some(3), ask = Some(7), timestamp = startTs.plusSeconds(25)),
    startQuote.copy(bid = Some(1), ask = Some(6), timestamp = startTs.plusSeconds(40)),
    startQuote.copy(bid = Some(5), ask = Some(8), timestamp = startTs.plusSeconds(60)),
    startQuote.copy(bid = Some(5), ask = Some(5), timestamp = startTs.plusSeconds(70))
  )
  val empty = Window(period = Duration.ofMinutes(1), maxGap = Duration.ofMinutes(1))
  val emptyHour = Window(period = Duration.ofHours(1), maxGap = Duration.ofHours(1))
  val window = quotes.foldLeft(empty)((w: Window, q: Quote) => w.addQuote(q))

  test("Quotes time elimination") {
    assert(window.size == 5, "; window size is incorrect")
  }

  test("Min/max elimination") {
    assert(window.min == BigDecimal(1))
    assert(window.max == BigDecimal(8))
    val newWindow = window.addQuote(startQuote.copy(bid = Some(4), ask = Some(9), timestamp = startTs.plusSeconds(80)))
    assert(newWindow.min == BigDecimal(1))
    assert(newWindow.max == BigDecimal(9))
  }

  test("Many quotes elimination") {
    val quotes = Source.fromInputStream(classOf[WindowTest].getResourceAsStream("/sample.csv")).getLines().map(Snapshot.fromCsv(_).best)
    val bigWindow = quotes.foldLeft(emptyHour)((w: Window, q: Quote) => w.addQuote(q))
    assert(Duration.between(bigWindow.mainQueue.head.timestamp, bigWindow.mainQueue.last.timestamp).compareTo(Duration.ofMinutes(30)) > 0)
    assert(BigDecimal("1.12283") === bigWindow.min)
    assert(BigDecimal("1.12392") === bigWindow.max)
  }

}

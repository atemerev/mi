package com.temerev.mi.analytics

import java.time.{Instant, Duration}

import com.miriamlaurel.fxcore.market.Quote
import com.miriamlaurel.fxcore._

import scala.annotation.tailrec
import scala.collection.immutable.Queue

case class Window(period: Duration,
                  maxGap: Duration,
                  mainQueue: Queue[Quote] = Queue(),
                  minQueue: Queue[Quote] = Queue(),
                  maxQueue: Queue[Quote] = Queue(),
                  full: Boolean = false) {

  def addQuote(quote: Quote): Window = {
    val (newMainQueue, newMinQueue, newMaxQueue) = {
      if (mainQueue.nonEmpty && Duration.between(mainQueue.last.timestamp, quote.timestamp).compareTo(maxGap) > 0) {
        (Queue(), Queue(), Queue())
      } else (trimOld(mainQueue, quote.timestamp), trimMin(trimOld(minQueue, quote.timestamp), quote), trimMax(trimOld(maxQueue, quote.timestamp), quote))
    }
    val newFull = mainQueue.nonEmpty && newMainQueue.nonEmpty && mainQueue.head.timestamp != newMainQueue.head.timestamp
    copy(mainQueue = newMainQueue :+ quote, minQueue = newMinQueue :+ quote, maxQueue = newMaxQueue :+ quote, full = newFull)
  }

  lazy val size: Int = mainQueue.size

  lazy val min: BigDecimal = minQueue.head.bid.get

  lazy val max: BigDecimal = maxQueue.head.ask.get

  lazy val heightPips: BigDecimal = asPips(mainQueue.head, max - min)

  lazy val minuteIndex: Int = ((mainQueue.last.timestamp.toEpochMilli / 60000 - 5760) % (7 * 24 * 60)).toInt

  lazy val deltaBidPips = asPips(mainQueue.head.instrument, mainQueue.last.bid.get - mainQueue.head.bid.get)

  lazy val deltaAskPips = asPips(mainQueue.head.instrument, mainQueue.last.ask.get - mainQueue.head.ask.get)

  @tailrec
  private def trimOld(queue: Queue[Quote], now: Instant): Queue[Quote] = if (queue.isEmpty || Duration.between(queue.head.timestamp, now).compareTo(period) < 0)
    queue else trimOld(queue.tail, now)

  @tailrec
  private def trimMin(queue: Queue[Quote], quote: Quote): Queue[Quote] =
    if (queue.isEmpty || queue.last.bid.get <= quote.bid.get) queue
    else trimMin(queue.dropRight(1), quote)

  @tailrec
  private def trimMax(queue: Queue[Quote], quote: Quote): Queue[Quote] =
    if (queue.isEmpty || queue.last.ask.get >= quote.ask.get) queue else trimMax(queue.dropRight(1), quote)

  private def distance(queue: Queue[Quote]): Duration = Duration.between(queue.head.timestamp, queue.last.timestamp)
}
package com.temerev.mi.analytics

import java.time.Duration

import com.miriamlaurel.fxcore.market.Quote

import scala.annotation.tailrec
import scala.collection.immutable.Queue

case class Window(period: Duration,
                  maxGap: Duration,
                  mainQueue: Queue[Quote] = Queue(),
                  minQueue: Queue[Quote] = Queue(),
                  maxQueue: Queue[Quote] = Queue()) {

  def addQuote(quote: Quote): Window = {
    val (newMainQueue, newMinQueue, newMaxQueue) = {
      if (mainQueue.nonEmpty && distance(mainQueue).compareTo(maxGap) > 0) {
        (Queue(), Queue(), Queue())
      } else (trimOld(mainQueue :+ quote), trimMin(trimOld(minQueue :+ quote), quote), trimMax(trimOld(maxQueue :+ quote), quote))
    }
    copy(mainQueue = newMainQueue, minQueue = newMinQueue, maxQueue = newMaxQueue)
  }

  lazy val size: Int = mainQueue.size

  lazy val min: BigDecimal = minQueue.head.bid.get

  lazy val max: BigDecimal = maxQueue.head.ask.get

  @tailrec
  private def trimOld(queue: Queue[Quote]): Queue[Quote] = if (queue.isEmpty || distance(queue).compareTo(period) < 0)
    queue else trimOld(queue.tail)

  @tailrec
  private def trimMin(queue: Queue[Quote], quote: Quote): Queue[Quote] =
    if (queue.isEmpty || queue.last.bid.get <= quote.bid.get) queue else trimMin(queue.dropRight(1), quote)

  @tailrec
  private def trimMax(queue: Queue[Quote], quote: Quote): Queue[Quote] =
    if (queue.isEmpty || queue.last.ask.get >= quote.ask.get) queue else trimMax(queue.dropRight(1), quote)

  private def distance(queue: Queue[Quote]): Duration = Duration.between(queue.head.timestamp, queue.last.timestamp)
}
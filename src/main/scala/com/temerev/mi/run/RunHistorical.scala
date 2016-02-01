package com.temerev.mi.run

import java.io.File
import java.time.{Duration, LocalDate, Month}

import com.miriamlaurel.fxcore._
import com.temerev.mi.analytics.{FutureView, Window}
import com.temerev.mi.io.TickReader

import scala.collection.immutable.Queue
import scala.io.Source

object RunHistorical extends App {
  val reader = new TickReader(new File("/opt/data"))
  val startDate = LocalDate.of(2015, Month.JULY, 15)
  val endDate = LocalDate.of(2015, Month.AUGUST, 23)
  println("Loading ticks...")
  val ticks = reader.getTicks(EURUSD, startDate, endDate).map(_.best)
  val smap = readSeasonalityMap(new File(args(0)))
  var view = FutureView(500, Window(Duration.ofHours(1), Duration.ofMinutes(5)), Queue.empty)
  var candidates = Vector.empty[FutureView]
  var count = 0
  for (tick <- ticks) {
    view = view.addQuote(tick)
    count += 1
    if (view.window.mainQueue.nonEmpty &&
        view.window.heightPips < smap.getOrElse(view.window.minuteIndex, 0) &&
       (view.window.min == tick.bid.get || view.window.max == tick.ask.get)) candidates = candidates :+ view
  }
  println(candidates.size)
  println(count)

  def readSeasonalityMap(file: File): Map[Int, BigDecimal] = {
    val pairs = Source.fromFile(file).getLines().map(s => {
      val tokens = s.split(",")
      (tokens(0).toInt, BigDecimal(tokens(1)))
    })
    Map(pairs.toSeq: _*)
  }
}

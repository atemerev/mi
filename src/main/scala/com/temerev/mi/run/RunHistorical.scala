package com.temerev.mi.run

import java.io.{FileWriter, File}
import java.time.format.DateTimeFormatter
import java.time.{ZoneId, Duration, LocalDate, Month}

import com.miriamlaurel.fxcore._
import com.miriamlaurel.fxcore.market.Quote
import com.miriamlaurel.fxcore.portfolio.StrictPortfolio
import com.temerev.mi.analytics.{FutureView, Window}
import com.temerev.mi.io.TickReader
import com.temerev.mi.strategy.BreakoutStrategy

import scala.collection.immutable.Queue
import scala.io.Source

object RunHistorical extends App {
  val TZ = ZoneId.of("UTC")
  val reader = new TickReader(new File("/opt/data"))
  val startDate = LocalDate.of(2015, Month.OCTOBER, 1)
  val endDate = LocalDate.of(2015, Month.OCTOBER, 30)
  val format = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
  println("Loading ticks...")
  val startTs = System.currentTimeMillis()
  val ticks = reader.getTicks(EURUSD, startDate, endDate).map(_.best)
  val smap = readSeasonalityMap(new File(args(0)))
  val outDir = new File(args(1))
  var view = FutureView(500, Window(Duration.ofHours(1), Duration.ofMinutes(5)), Queue.empty)
  var candidates = Vector.empty[FutureView]
  var count = 0
  var ts = 0L
  val strategy = BreakoutStrategy(new StrictPortfolio(), view.window, smap)
  val result = ticks.foldLeft(strategy)((s: BreakoutStrategy, q: Quote) => s.apply(q))
  result.deals.foreach(println)
/*
  for (tick <- ticks) {
    view = view.addQuote(tick)
    count += 1
    if (view.window.mainQueue.nonEmpty &&
        view.window.heightPips < smap.getOrElse(view.window.minuteIndex, 0) &&
       (view.window.max == tick.ask.get) &&
        tick.timestamp.toEpochMilli - ts > 60000) {
      ts = tick.timestamp.toEpochMilli
      candidates = candidates :+ view
    }
  }
  val endTs = System.currentTimeMillis()
  val tps = (count / (endTs - startTs).toDouble) * 1000
  val fmtTps = "%.1f".format(tps)
  println(s"$count ticks processed ($fmtTps ticks per second), ${candidates.size} candidates found...")
  candidates.foreach(writeView(_, outDir))

  def writeView(view: FutureView, directory: File): Unit = {
    val writer = new FileWriter(new File(directory, format.format(view.future.head.timestamp.atZone(TZ)) + ".txt"))
    val writeQuote = (q: Quote) => {
      val string = q.ask.get.toString()
      writer.write(string + "\n")
    }
    view.window.mainQueue.foreach(writeQuote)
    view.future.foreach(writeQuote)
    writer.flush()
    writer.close()
  }
*/

  def readSeasonalityMap(file: File): Map[Int, BigDecimal] = {
    val pairs = Source.fromFile(file).getLines().map(s => {
      val tokens = s.split(",")
      (tokens(0).toInt, BigDecimal(tokens(1)))
    })
    Map(pairs.toSeq: _*)
  }
}

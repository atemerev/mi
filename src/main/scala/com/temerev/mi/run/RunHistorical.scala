package com.temerev.mi.run

import java.io.File
import java.time.{Duration, Month, LocalDate}

import com.miriamlaurel.fxcore._
import com.miriamlaurel.fxcore.market.Quote
import com.temerev.mi.analytics.{Window, FutureView}
import com.temerev.mi.io.TickReader

import scala.collection.immutable.Queue

object RunHistorical extends App {
  val reader = new TickReader(new File("/opt/data"))
  val startDate = LocalDate.of(2015, Month.JULY, 15)
  val endDate = LocalDate.of(2015, Month.OCTOBER, 23)
  println("Loading ticks...")
  val ticks = reader.getTicks(EURUSD, startDate, endDate).map(_.best)
  val initialView = FutureView(500, Window(Duration.ofHours(1), Duration.ofMinutes(5)), Queue.empty)
  val endView = ticks.foldLeft(initialView)((f: FutureView, q: Quote) => f.addQuote(q))
  println(endView.future.size)
}

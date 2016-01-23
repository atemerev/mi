package com.temerev.mi.io

import java.io.File
import java.time._
import java.time.temporal.ChronoUnit._

import com.miriamlaurel.fxcore._
import com.miriamlaurel.fxcore.instrument.Instrument
import com.miriamlaurel.fxcore.market.Snapshot

import scala.io.Source

class TickReader(val dataDir: File) {

  require(dataDir.isDirectory)

  def getTicks(instrument: Instrument, startDate: LocalDate, endDate: LocalDate): Iterator[Snapshot] = {
    val daysBetween = DAYS.between(startDate, endDate)
    val weekdays = (0 to daysBetween.toInt)
      .map(startDate.plusDays(_))
      .filter(date => date.getDayOfWeek != DayOfWeek.SATURDAY
        && date.getDayOfWeek != DayOfWeek.SUNDAY)
    val files = weekdays.map(wd => new File(new File(dataDir, wd.toString),
      instrument.toString.replaceFirst("/", "-") + ".txt"))
    val streams = files.filter(_.exists()).map(Source.fromFile(_).getLines())
    streams.foldLeft(Iterator[String]())(_ ++ _).map(Snapshot.fromCsv)
  }
}
package com.temerev.mi.strategy

import com.miriamlaurel.fxcore.accounting.Deal
import com.miriamlaurel.fxcore.market.Quote
import com.miriamlaurel.fxcore.portfolio.{CreateDeal, Position, StrictPortfolio}
import com.temerev.mi.analytics.Window

case class BreakoutStrategy(portfolio: StrictPortfolio, window: Window, deltaWindow: Window, seasonality: Map[Int, BigDecimal], deals: Vector[Deal] = Vector.empty) extends Strategy {

  override def apply(quote: Quote): BreakoutStrategy = {
    val newWindow = window.addQuote(quote)
    portfolio.position(quote.instrument) match {
      case None => if (!window.full) copy(window = newWindow)
      else {
        val amp = window.heightPips
        val threshold = seasonality.getOrElse(window.minuteIndex, BigDecimal(0))
        if (amp < threshold && window.max == quote.ask.get) {
          val position = Position(quote.instrument, quote.ask.get, BigDecimal(100000))
          val (newPortfolio, diff) = portfolio << position
          copy(window = newWindow, portfolio = newPortfolio)
        } else copy(window = newWindow)
      }
      case Some(existingPosition) =>
        val delta = window.deltaAskPips
        val pl = existingPosition.profitLossPips(quote).get
        if (delta < -0.1 && (pl > 5 || pl < -5)) {
          val closePosition = existingPosition.close(quote.bid.get)
          val (newPortfolio, diff) = portfolio << closePosition
          val deal = diff.actions.find(_.isInstanceOf[CreateDeal]).get.asInstanceOf[CreateDeal].deal
          copy(window = newWindow, portfolio = newPortfolio, deals = deals :+ deal)
        } else copy(window = newWindow)
    }
  }
}

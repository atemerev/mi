package com.temerev.mi.analytics

import com.miriamlaurel.fxcore.market.Quote

import scala.collection.immutable.Queue

case class FutureView(size: Int, window: Window, future: Queue[Quote]) {
  def addQuote(quote: Quote): FutureView = {
    val added = future.enqueue(quote)
    if (added.size > size) {
      val (quote, newFuture) = future.dequeue
      copy(window = window.addQuote(quote), future = newFuture)
    } else copy(future = added)
  }
}

package com.temerev.mi.strategy

import com.miriamlaurel.fxcore.market.Quote

trait Strategy {
  def apply(quote: Quote): Strategy
}

package stock

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PriceException(msg: String) extends RuntimeException(msg)
trait StockPrice extends PricePrecision {
  def priceConnector : PriceConnector

  /* 1 - 1 year historic prices given a ticker */
  def dailyPrices(ticker: String) : Future[Seq[BigDecimal]] = priceConnector.getPrices(ticker)

  /* 2- daily returns, where return = ( Price_Today – Price_Yesterday)/Price_Yesterday */
  def returns(ticker: String): Future[Seq[BigDecimal]] = {

    def calculateReturns(prices: Seq[BigDecimal]): Seq[BigDecimal] = {
      val (result, _) = prices.tail.foldLeft[(Seq[BigDecimal], BigDecimal)](Seq.empty, prices.head) {
        case ((accumulated, today), yesterday) =>
          val dailyReturn = (today - yesterday) / yesterday
          (accumulated :+ dailyReturn, yesterday)
      }
      result
    }

    dailyPrices(ticker).map { prices =>
      if (prices.size <= 1 )
        throw new PriceException(s"Received $prices, but need at least two prices for calculation")

      calculateReturns(prices)
    }

  }

  /* 1 year mean returns, assuming that's standard average function */
  def meanReturn(ticker:String): Future[BigDecimal] = returns(ticker).map( returns => returns.sum / returns.size)
}

package stock

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future

class StockPriceSpec extends WordSpec with Matchers with ScalaFutures with TableDrivenPropertyChecks with PricePrecision {

  trait  Setup {
    def googleTicker = "GOOG"
    def testPrices: Map[String, Seq[BigDecimal]] = Map(googleTicker -> Seq[Double](100.0, 50, 100, 200, 100).map(BigDecimal(_).setScale(scaleDigits)))
    object ConnectorStub  extends PriceConnector {
      override def getPrices(ticker: String) = Future.successful(testPrices(ticker))
    }

    val stockPrice = new StockPrice {
       def priceConnector: PriceConnector = ConnectorStub
    }
  }

  "StockPrice"  should {

    "return daily prices" in new Setup {
      stockPrice.dailyPrices(googleTicker).futureValue shouldBe testPrices(googleTicker)
    }

    "calculate daily returns" in new Setup {
      val prices = stockPrice.returns(googleTicker).futureValue
      prices shouldBe Seq(1.0, -0.5, -0.5, 1.0)
    }

    "calculate mean return" in new Setup {
      val expectedMeanReturn = Seq(1.0, -0.5, -0.5, 1.0).sum / 4
      stockPrice.meanReturn(googleTicker).futureValue shouldBe expectedMeanReturn
    }

    "throw error if there isn't enough price data" in new Setup {
      override def testPrices = Map(googleTicker -> Seq[Double](100.0).map(BigDecimal(_).setScale(scaleDigits)))
      val functions = Table("check exception",  stockPrice.returns(googleTicker), stockPrice.meanReturn(googleTicker))

      forAll(functions) { futureValue =>
        whenReady(futureValue.failed) { exception =>
          exception shouldBe a[PriceException]
        }
      }
    }

  }
}

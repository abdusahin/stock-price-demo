package stock

import java.time.LocalDate

import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.{HttpExt, HttpsConnectionContext}
import akka.stream.Materializer
import com.typesafe.config.ConfigFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Future
import scala.io.Source

class PriceConnectorSpec extends WordSpec with Matchers with ScalaFutures with IntegrationPatience with AkkaImplicits {

  trait Setup {
    val csvBody = Source.fromInputStream(getClass.getResourceAsStream("/table.csv")).mkString
    def httpResponse = HttpResponse(entity = csvBody)
    object HttpStub extends HttpExt(ConfigFactory.empty()) {
      override def singleRequest(request: HttpRequest, connectionContext: HttpsConnectionContext, settings: ConnectionPoolSettings, log: LoggingAdapter)(
        implicit fm: Materializer): Future[HttpResponse] = Future.successful(httpResponse)
    }

    val priceConnector = new PriceConnector {
      override def today = LocalDate.of(2017, 1, 20)
      override lazy val http  = HttpStub
    }
  }

  "PriceConnector" should {
    "return historical prices for GOOG" in new Setup {
      val expectedPrices = Seq(805.02002, 802.174988, 806.070007, 804.609985, 807.880005)
      whenReady(priceConnector.getPrices("GOOG").map(_.take(5))) { prices => prices shouldBe expectedPrices}
    }
  }


}

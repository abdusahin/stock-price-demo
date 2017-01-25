package stock

import java.time.LocalDate

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{HttpEntity, StatusCodes}
import akka.stream.ActorMaterializer

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.util.{Failure, Success, Try}

class PriceConnector extends PricesUrlDate with AkkaImplicits with CSVParser {
  lazy val http = Http()

  def getPrices(ticker: String): Future[Seq[BigDecimal]] = {
    val url = pricesURL(today, ticker)
    for {
      response <- http.singleRequest(Get(url))
      body <- response.entity.toStrict(timeout)
      result = if (response.status == StatusCodes.OK) body.data.utf8String else ""
    } yield parseCSVAndGetAdjustedCloseValue(result)
  }
}

trait CSVParser extends PricePrecision {

  /**
    * CSV Format
    * Date,Open,High,Low,Close,Volume,Adj Close
    * 2017-01-20,806.909973,806.909973,801.690002,805.02002,1645000,805.02002
    */
  def parseCSVAndGetAdjustedCloseValue(body: String): Seq[BigDecimal] = {
    Try {
      Source.fromString(body).getLines().drop(1).flatMap(_.split(",").takeRight(1).map(BigDecimal(_).setScale(scaleDigits))).toSeq
    } match {
      case Success(prices) => prices
      case Failure(_) => Seq.empty
    }
  }
}


trait PricePrecision {
  def scaleDigits = 6
}

trait PricesUrlDate {
  def today: LocalDate = LocalDate.now()
  def pricesURL(businessDate : java.time.LocalDate, ticker: String) : String = {
    val lastYear = businessDate.minusYears(1)
    f"http://real-chart.finance.yahoo.com/table.csv?s=$ticker&a=${lastYear.getMonthValue}&b=${lastYear.getDayOfMonth}&c=${lastYear.getYear}&d=${today.getMonthValue}&e=${today.getDayOfMonth}&f=${today.getYear}&g=d&ignore=.csv"
  }
}

trait AkkaImplicits {
  val timeout = 3 seconds
  implicit val system: ActorSystem = ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}

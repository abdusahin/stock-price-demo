package stock

import org.scalatest.{Matchers, WordSpec}

import scala.io.Source


class CSVParserSpec extends WordSpec with Matchers {

  "CSVParser" should {

    "parse csv values" in {
      object parser extends CSVParser
      val top5AdjustedClosePrices = Seq(805.02002, 802.174988, 806.070007, 804.609985, 807.880005)
      val body = Source.fromInputStream(getClass.getResourceAsStream("/table.csv")).mkString
      val prices = parser.parseCSVAndGetAdjustedCloseValue(body)
      prices.take(5) shouldBe top5AdjustedClosePrices
    }

    "return no price for empty body" in {
      object parser extends CSVParser
      val body = ""
      val prices = parser.parseCSVAndGetAdjustedCloseValue(body)
      prices shouldBe empty

    }

    "return no price for faulty csv string" in {
      object parser extends CSVParser
      val body = Source.fromInputStream(getClass.getResourceAsStream("/google.html")).mkString
      val prices = parser.parseCSVAndGetAdjustedCloseValue(body)
      prices shouldBe empty

    }
  }

}

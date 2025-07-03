/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossintermediarydashboard.models

import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.Json
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class CountrySpec extends BaseSpec with ScalaFutures {

  "Country" - {

    "serialize and deserialize correctly" in {
      val country = Country("DE", "Germany")

      val json = Json.toJson(country)
      json mustBe Json.obj(
        "code" -> "DE",
        "name" -> "Germany"
      )

      val deserialized = json.as[Country]
      deserialized mustEqual country
    }

    "serialize and deserialize with another Country" in {
      val country = Country("FR", "France")

      val json = Json.toJson(country)
      json mustBe Json.obj(
        "code" -> "FR",
        "name" -> "France"
      )

      val deserialized = json.as[Country]
      deserialized mustEqual country
    }

    "handle invalid JSON" in {
      val invalidJson = Json.obj(
        "code" -> "IT",
      )

      val result = invalidJson.validate[Country]
      result.isError mustBe true
    }

    "handle missing 'code' field" in {
      val invalidJson = Json.obj(
        "name" -> "Germany"
      )

      val result = invalidJson.validate[Country]
      result.isError mustBe true
    }

    "serialize a Country with a long name" in {
      val country = Country("TH", "Thailand")

      val json = Json.toJson(country)
      json mustBe Json.obj(
        "code" -> "TH",
        "name" -> "Thailand"
      )

      val deserialized = json.as[Country]
      deserialized mustEqual country
    }

    "deserialize a Country with unknown code" in {
      val unknownJson = Json.obj(
        "code" -> "ZZ",
        "name" -> "UnknownLand"
      )

      val deserialized = unknownJson.as[Country]
      deserialized mustEqual Country("ZZ", "UnknownLand")
    }
  }
}

package uk.gov.hmrc.iossintermediarydashboard.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class PartialReturnPeriodSpec extends BaseSpec {

  private val partialReturnPeriod: PartialReturnPeriod =
    arbitraryPartialReturnPeriod.arbitrary.sample.value

  "PartialReturnPeriod" - {

    "must deserialise/serialise from and to PartialReturnPeriod" in {

      val json = Json.obj(
        "firstDay" -> partialReturnPeriod.firstDay,
        "lastDay" -> partialReturnPeriod.lastDay,
        "year" -> partialReturnPeriod.year,
        "month" -> s"M${partialReturnPeriod.month.getValue}"
      )

      val expectedResult = PartialReturnPeriod(
        firstDay = partialReturnPeriod.firstDay,
        lastDay = partialReturnPeriod.lastDay,
        year = partialReturnPeriod.year,
        month = partialReturnPeriod.month
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[PartialReturnPeriod] `mustBe` JsSuccess[PartialReturnPeriod](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PartialReturnPeriod] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "firstDay" -> partialReturnPeriod.firstDay,
        "lastDay" -> partialReturnPeriod.lastDay,
        "year" -> "2025",
        "month" -> s"M${partialReturnPeriod.month.getValue}"
      )

      json.validate[PartialReturnPeriod] `mustBe` a[JsError]
    }
  }
}

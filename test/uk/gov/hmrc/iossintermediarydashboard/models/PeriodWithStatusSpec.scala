package uk.gov.hmrc.iossintermediarydashboard.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class PeriodWithStatusSpec extends BaseSpec {

  private val periodWithStatus: PeriodWithStatus = arbitraryPeriodWithStatus.arbitrary.sample.value

  "PeriodWithStatus" - {

    "must deserialise/serialise from and to PeriodWithStatus" in {

      val json = Json.obj(
        "iossNumber" -> periodWithStatus.iossNumber,
        "period" -> periodWithStatus.period,
        "status" -> periodWithStatus.status
      )

      val expectedResult = PeriodWithStatus(
        iossNumber = periodWithStatus.iossNumber,
        period = periodWithStatus.period,
        status = periodWithStatus.status
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[PeriodWithStatus] `mustBe` JsSuccess[PeriodWithStatus](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PeriodWithStatus] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "status" -> 123456
      )

      json.validate[PeriodWithStatus] `mustBe` a[JsError]
    }
  }
}

package uk.gov.hmrc.iossintermediarydashboard.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class CurrentReturnsSpec extends BaseSpec {

  private val currentReturns: CurrentReturns = arbitraryCurrentReturns.arbitrary.sample.value

  "CurrentReturns" - {

    "must deserialise/serialise from and to CurrentReturns" in {

      val json = Json.obj(
        "iossNumber" -> currentReturns.iossNumber,
        "incompleteReturns" -> currentReturns.incompleteReturns,
        "completedReturns" -> currentReturns.completedReturns
      )

      val expectedResult = CurrentReturns(
        iossNumber = currentReturns.iossNumber,
        incompleteReturns = currentReturns.incompleteReturns,
        completedReturns = currentReturns.completedReturns
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[CurrentReturns] `mustBe` JsSuccess[CurrentReturns](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[CurrentReturns] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "iossNumber" -> currentReturns.iossNumber,
        "incompleteReturns" -> 123456,
        "completedReturns" -> currentReturns.completedReturns
      )

      json.validate[CurrentReturns] `mustBe` a[JsError]
    }
  }
}

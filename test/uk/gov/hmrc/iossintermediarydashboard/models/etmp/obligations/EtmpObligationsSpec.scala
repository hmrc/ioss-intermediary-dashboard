package uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class EtmpObligationsSpec extends BaseSpec {

  private val etmpObligations: EtmpObligations = arbitraryEtmpObligations.arbitrary.sample.value

  "EtmpObligations" - {

    "must deserialise/serialise from and to EtmpObligations" in {

      val json = Json.obj(
        "obligations" -> etmpObligations.obligations
      )

      val expectedResult = EtmpObligations(
        obligations = etmpObligations.obligations
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[EtmpObligations] `mustBe` JsSuccess[EtmpObligations](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EtmpObligations] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "obligations" -> "123456"
      )

      json.validate[EtmpObligations] `mustBe` a[JsError]
    }
  }
}

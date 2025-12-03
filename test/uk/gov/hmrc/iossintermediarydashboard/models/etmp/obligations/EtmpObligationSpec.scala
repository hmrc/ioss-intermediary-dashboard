package uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class EtmpObligationSpec extends BaseSpec {

  private val etmpObligation: EtmpObligation = arbitraryEtmpObligation.arbitrary.sample.value

  "EtmpObligation" - {

    "must deserialise/serialise from and to EtmpObligation" in {

      val json = Json.obj(
        "identification" -> etmpObligation.identification,
        "obligationDetails" -> etmpObligation.obligationDetails
      )

      val expectedResult = EtmpObligation(
        identification = etmpObligation.identification,
        obligationDetails = etmpObligation.obligationDetails
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[EtmpObligation] `mustBe` JsSuccess[EtmpObligation](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EtmpObligation] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "obligationDetails" -> "123456"
      )

      json.validate[EtmpObligation] `mustBe` a[JsError]
    }
  }
}

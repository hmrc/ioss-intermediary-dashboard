package uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class EtmpObligationIdentificationSpec extends BaseSpec {

  private val etmpObligationIdentification: EtmpObligationIdentification =
    arbitraryEtmpObligationIdentification.arbitrary.sample.value

  "EtmpObligationIdentification" - {

    "must deserialise/serialise from and to EtmpObligationIdentification" in {

      val json = Json.obj(
        "referenceNumber" -> etmpObligationIdentification.referenceNumber
      )

      val expectedResult = EtmpObligationIdentification(
        referenceNumber = etmpObligationIdentification.referenceNumber
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[EtmpObligationIdentification] `mustBe` JsSuccess[EtmpObligationIdentification](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EtmpObligationIdentification] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "referenceNumber" -> 123456
      )

      json.validate[EtmpObligationIdentification] `mustBe` a[JsError]
    }
  }
}

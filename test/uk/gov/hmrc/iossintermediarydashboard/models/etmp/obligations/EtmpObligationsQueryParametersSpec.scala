package uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class EtmpObligationsQueryParametersSpec extends BaseSpec {

  private val etmpObligationsQueryParameters: EtmpObligationsQueryParameters =
    arbitraryEtmpObligationsQueryParameters.arbitrary.sample.value

  "EtmpObligationsQueryParameters" - {

    "must deserialise/serialise from and to EtmpObligationsQueryParameters" - {

      "when all optional values are present" in {

        val json = Json.obj(
          "fromDate" -> etmpObligationsQueryParameters.fromDate,
          "toDate" -> etmpObligationsQueryParameters.toDate,
          "status" -> etmpObligationsQueryParameters.status,
        )

        val expectedResult = EtmpObligationsQueryParameters(
          fromDate = etmpObligationsQueryParameters.fromDate,
          toDate = etmpObligationsQueryParameters.toDate,
          status = etmpObligationsQueryParameters.status
        )

        Json.toJson(expectedResult) `mustBe` json
        json.validate[EtmpObligationsQueryParameters] `mustBe` JsSuccess[EtmpObligationsQueryParameters](expectedResult)
      }

      "when all optional values are missing" in {

        val json = Json.obj(
          "fromDate" -> etmpObligationsQueryParameters.fromDate,
          "toDate" -> etmpObligationsQueryParameters.toDate
        )

        val expectedResult = EtmpObligationsQueryParameters(
          fromDate = etmpObligationsQueryParameters.fromDate,
          toDate = etmpObligationsQueryParameters.toDate,
          status = None
        )

        Json.toJson(expectedResult) `mustBe` json
        json.validate[EtmpObligationsQueryParameters] `mustBe` JsSuccess[EtmpObligationsQueryParameters](expectedResult)
      }
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EtmpObligationsQueryParameters] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "fromDate" -> 123456,
        "toDate" -> etmpObligationsQueryParameters.toDate,
        "status" -> None
      )

      json.validate[EtmpObligationsQueryParameters] `mustBe` a[JsError]
    }
  }
}

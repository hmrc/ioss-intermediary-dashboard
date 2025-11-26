package uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class EtmpObligationsFulfilmentStatusSpec extends BaseSpec with ScalaCheckPropertyChecks {

  "EtmpObligationsFulfilmentStatus" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(EtmpObligationsFulfilmentStatus.values)

      forAll(gen) {
        etmpObligationsFulfilmentStatus =>

          JsString(etmpObligationsFulfilmentStatus.toString)
            .validate[EtmpObligationsFulfilmentStatus].asOpt.value `mustBe` etmpObligationsFulfilmentStatus
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String].suchThat(!EtmpObligationsFulfilmentStatus.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValues =>

          JsString(invalidValues).validate[EtmpObligationsFulfilmentStatus] `mustBe` JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(EtmpObligationsFulfilmentStatus.values)

      forAll(gen) {
        etmpObligationsFulfilmentStatus =>

          Json.toJson(etmpObligationsFulfilmentStatus) `mustBe` JsString(etmpObligationsFulfilmentStatus.toString)
      }
    }
  }
}

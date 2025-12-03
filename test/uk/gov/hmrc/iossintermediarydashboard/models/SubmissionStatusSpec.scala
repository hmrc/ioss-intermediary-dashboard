package uk.gov.hmrc.iossintermediarydashboard.models

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class SubmissionStatusSpec extends BaseSpec with ScalaCheckPropertyChecks {

  "SubmissionStatus" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(SubmissionStatus.values)

      forAll(gen) {
        submissionStatus =>

          JsString(submissionStatus.toString)
            .validate[SubmissionStatus].asOpt.value `mustBe` submissionStatus
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String].suchThat(!SubmissionStatus.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValues =>

          JsString(invalidValues).validate[SubmissionStatus] `mustBe` JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(SubmissionStatus.values)

      forAll(gen) {
        submissionStatus =>

          Json.toJson(submissionStatus) `mustBe` JsString(submissionStatus.toString)
      }
    }
  }
}

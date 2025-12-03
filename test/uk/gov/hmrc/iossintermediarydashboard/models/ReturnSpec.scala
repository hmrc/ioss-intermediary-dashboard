package uk.gov.hmrc.iossintermediarydashboard.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class ReturnSpec extends BaseSpec {

  private val arbReturn: Return = arbitraryReturn.arbitrary.sample.value

  "Return" - {

    "must deserialise/serialise from and to Return" in {

      val json = Json.obj(
        "period" -> arbReturn.period,
        "firstDay" -> arbReturn.firstDay,
        "lastDay" -> arbReturn.lastDay,
        "dueDate" -> arbReturn.dueDate,
        "submissionStatus" -> arbReturn.submissionStatus,
        "inProgress" -> arbReturn.inProgress,
        "isOldest" -> arbReturn.isOldest
      )

      val expectedResult = Return(
        period = arbReturn.period,
        firstDay = arbReturn.firstDay,
        lastDay = arbReturn.lastDay,
        dueDate = arbReturn.dueDate,
        submissionStatus = arbReturn.submissionStatus,
        inProgress = arbReturn.inProgress,
        isOldest = arbReturn.isOldest
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[Return] `mustBe` JsSuccess[Return](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[Return] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "period" -> arbReturn.period,
        "firstDay" -> arbReturn.firstDay,
        "lastDay" -> arbReturn.lastDay,
        "dueDate" -> arbReturn.dueDate,
        "submissionStatus" -> 123456,
        "inProgress" -> arbReturn.inProgress,
        "isOldest" -> arbReturn.isOldest
      )

      json.validate[Return] `mustBe` a[JsError]
    }
  }

  ".fromPeriod" - {

    "must create return from supplied period" in {

      val period: Period = arbReturn.period

      val result: Return = Return.fromPeriod(period, arbReturn.submissionStatus, arbReturn.inProgress, arbReturn.isOldest)

      val expectedResult = Return(
        period = period,
        firstDay = period.firstDay,
        lastDay = period.lastDay,
        dueDate = period.paymentDeadline,
        submissionStatus = arbReturn.submissionStatus,
        inProgress = arbReturn.inProgress,
        isOldest = arbReturn.isOldest
      )

      result `mustBe` expectedResult
    }
  }
}

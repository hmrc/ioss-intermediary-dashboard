package uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.models.Period
import uk.gov.hmrc.iossintermediarydashboard.models.Period.{getNext, getRunningPeriod, toEtmpPeriodString}
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations.EtmpObligationsFulfilmentStatus.{Fulfilled, Open}

import java.time.LocalDate

class EtmpObligationDetailsSpec extends BaseSpec {

  private val etmpObligationDetails: EtmpObligationDetails = arbitraryEtmpObligationDetails.arbitrary.sample.value

  "EtmpObligationDetails" - {

    "must deserialise/serialise from and to EtmpObligationDetails" in {

      val json = Json.obj(
        "status" -> etmpObligationDetails.status,
        "periodKey" -> etmpObligationDetails.periodKey
      )

      val expectedResult = EtmpObligationDetails(
        status = etmpObligationDetails.status,
        periodKey = etmpObligationDetails.periodKey
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[EtmpObligationDetails] `mustBe` JsSuccess[EtmpObligationDetails](expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EtmpObligationDetails] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "status" -> "123456"
      )

      json.validate[EtmpObligationDetails] `mustBe` a[JsError]
    }

    ".getFulfilledPeriods" - {

      "must return all fulfilled periods from EtmpObligationDetails" in {

        val commencementDate: LocalDate = LocalDate.now(stubClock)

        val period1: Period = getRunningPeriod(commencementDate)
        val period2: Period = getNext(period1)
        val period3: Period = getNext(period2)
        val period4: Period = getNext(period3)

        val obligationDetails = Seq(
          createEtmpObligationDetails(Fulfilled, toEtmpPeriodString(period1)),
          createEtmpObligationDetails(Open, toEtmpPeriodString(period2)),
          createEtmpObligationDetails(Fulfilled, toEtmpPeriodString(period3)),
          createEtmpObligationDetails(Open, toEtmpPeriodString(period4),
          )
        )

        val expectedResult: List[Period] = List(period1, period3)

        val allFulfilledPeriods: List[Period] = EtmpObligationDetails
          .FromEtmpObligationsToPeriods(obligationDetails)
          .getFulfilledPeriods

        allFulfilledPeriods `mustBe` expectedResult
      }

      "must return an empty list when there are no fulfilled periods from EtmpObligationDetails" in {

        val commencementDate: LocalDate = LocalDate.now(stubClock)

        val period1: Period = getRunningPeriod(commencementDate)
        val period2: Period = getNext(period1)
        val period3: Period = getNext(period2)
        val period4: Period = getNext(period3)

        val obligationDetails = Seq(
          createEtmpObligationDetails(Open, toEtmpPeriodString(period1)),
          createEtmpObligationDetails(Open, toEtmpPeriodString(period2)),
          createEtmpObligationDetails(Open, toEtmpPeriodString(period3)),
          createEtmpObligationDetails(Open, toEtmpPeriodString(period4),
          )
        )

        val expectedResult: List[Period] = List.empty

        val allFulfilledPeriods: List[Period] = EtmpObligationDetails
          .FromEtmpObligationsToPeriods(obligationDetails)
          .getFulfilledPeriods

        allFulfilledPeriods `mustBe` expectedResult
      }
    }
  }
}

private def createEtmpObligationDetails(status: EtmpObligationsFulfilmentStatus, periodKey: String): EtmpObligationDetails = {
  EtmpObligationDetails(
    status = status,
    periodKey = periodKey
  )
}

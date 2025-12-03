package uk.gov.hmrc.iossintermediarydashboard.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.PrivateMethodTester.PrivateMethod
import org.scalatest.{BeforeAndAfterEach, PrivateMethodTester}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpObligationsConnector
import uk.gov.hmrc.iossintermediarydashboard.models.Period.{getNext, getRunningPeriod, toEtmpPeriodString}
import uk.gov.hmrc.iossintermediarydashboard.models.SubmissionStatus.{Complete, Due, Next, Overdue}
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations.EtmpObligationsFulfilmentStatus.{Fulfilled, Open}
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations.{EtmpObligation, EtmpObligationDetails, EtmpObligationIdentification, EtmpObligations, EtmpObligationsQueryParameters}
import uk.gov.hmrc.iossintermediarydashboard.models.responses.EtmpObligationsError
import uk.gov.hmrc.iossintermediarydashboard.models.{Period, PeriodWithStatus, StandardPeriod, SubmissionStatus}
import uk.gov.hmrc.iossintermediarydashboard.utils.FutureSyntax.FutureOps

import java.time.{LocalDate, ZoneId}
import scala.concurrent.{ExecutionContext, Future}

class ObligationsServiceSpec extends BaseSpec with PrivateMethodTester with BeforeAndAfterEach {

  private implicit lazy val ec: ExecutionContext = ExecutionContext.global

  private val mockEtmpObligationsConnector: EtmpObligationsConnector = mock[EtmpObligationsConnector]

  private val etmpObligations: EtmpObligations = arbitraryEtmpObligations.arbitrary.sample.value

  private val etmpObligationsQueryParameters: EtmpObligationsQueryParameters =
    arbitraryEtmpObligationsQueryParameters.arbitrary.sample.value

  private val clientReferenceNumberB: String = "IM9001234568"

  override def beforeEach(): Unit = {
    Mockito.reset(mockEtmpObligationsConnector)
  }

  "ObligationsService" - {

    ".getPeriodsWithStatus" - {

      val service: ObligationsService = new ObligationsService(mockEtmpObligationsConnector, stubClock)

      "for each client" - {

        "when all obligations are fulfilled" - {

          "must return the current list of periods with status from the commencement date and the next period" in {

            val commencementDate: LocalDate = LocalDate.now(stubClock).minusMonths(3)

            val period1: Period = StandardPeriod(commencementDate.getYear, commencementDate.getMonth)
            val period2: Period = getNext(period1)
            val period3: Period = getNext(period2)
            val nextPeriod: Period = getNext(period3)

            val completedObligations: EtmpObligations = EtmpObligations(
              obligations = Seq(
                EtmpObligation(
                  identification = EtmpObligationIdentification(iossNumber),
                  obligationDetails = Seq(
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(period1)
                    ),
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(period2)
                    ),
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(period3)
                    )
                  )
                ),
                EtmpObligation(
                  identification = EtmpObligationIdentification(clientReferenceNumberB),
                  obligationDetails = Seq(
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(StandardPeriod(commencementDate.getYear, commencementDate.getMonth))
                    ),
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(StandardPeriod(commencementDate.getYear, commencementDate.getMonth.plus(1)))
                    ),
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(StandardPeriod(commencementDate.getYear, commencementDate.getMonth.plus(2)))
                    )
                  )
                )
              )
            )

            when(mockEtmpObligationsConnector.getObligations(any(), any())) thenReturn Right(completedObligations).toFuture

            val result = service.getPeriodsWithStatus(intermediaryNumber, commencementDate).futureValue

            val expectedList: Map[String, List[PeriodWithStatus]] = Map(
              iossNumber -> List(
                PeriodWithStatus(iossNumber, period1, Complete),
                PeriodWithStatus(iossNumber, period2, Complete),
                PeriodWithStatus(iossNumber, period3, Complete),
                PeriodWithStatus(iossNumber, nextPeriod, Next)
              ),
              clientReferenceNumberB -> List(
                PeriodWithStatus(clientReferenceNumberB, period1, Complete),
                PeriodWithStatus(clientReferenceNumberB, period2, Complete),
                PeriodWithStatus(clientReferenceNumberB, period3, Complete),
                PeriodWithStatus(clientReferenceNumberB, nextPeriod, Next)
              )
            )

            result `mustBe` expectedList
          }
        }

        "when all obligations are not fulfilled" - {

          "must return a list of periods with status dating from the commencement date to the current period" in {

            val commencementDate: LocalDate = LocalDate.now(stubClock).minusMonths(3)

            val period1: Period = StandardPeriod(commencementDate.getYear, commencementDate.getMonth)
            val period2: Period = getNext(period1)
            val period3: Period = getNext(period2)
            val nextPeriod: Period = getNext(period3)

            val withUnfulfilledObligations: EtmpObligations = EtmpObligations(
              obligations = Seq(
                EtmpObligation(
                  identification = EtmpObligationIdentification(iossNumber),
                  obligationDetails = Seq(
                    EtmpObligationDetails(
                      status = Open,
                      periodKey = toEtmpPeriodString(period1)
                    ),
                    EtmpObligationDetails(
                      status = Open,
                      periodKey = toEtmpPeriodString(period2)
                    ),
                    EtmpObligationDetails(
                      status = Open,
                      periodKey = toEtmpPeriodString(period3)
                    )
                  )
                ),
                EtmpObligation(
                  identification = EtmpObligationIdentification(clientReferenceNumberB),
                  obligationDetails = Seq(
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(period1)
                    ),
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(period2)
                    ),
                    EtmpObligationDetails(
                      status = Fulfilled,
                      periodKey = toEtmpPeriodString(period3)
                    )
                  )
                )
              )
            )

            when(mockEtmpObligationsConnector.getObligations(any(), any())) thenReturn Right(withUnfulfilledObligations).toFuture

            val result = service.getPeriodsWithStatus(intermediaryNumber, commencementDate).futureValue

            val expectedList: Map[String, List[PeriodWithStatus]] = Map(
              clientReferenceNumberB -> List(
                PeriodWithStatus(clientReferenceNumberB, period1, Complete),
                PeriodWithStatus(clientReferenceNumberB, period2, Complete),
                PeriodWithStatus(clientReferenceNumberB, period3, Complete),
                PeriodWithStatus(clientReferenceNumberB, nextPeriod, Next)
              ),
              iossNumber -> List(
                PeriodWithStatus(iossNumber, period1, Overdue),
                PeriodWithStatus(iossNumber, period2, Overdue),
                PeriodWithStatus(iossNumber, period3, Due)
              )
            )

            result `mustBe` expectedList
          }
        }

        "must return the next period with status from the commencement date when the commencement date is the current period" in {

          val commencementDate: LocalDate = LocalDate.now(stubClock)
          val currentPeriod: Period = getRunningPeriod(commencementDate)

          val startingObligation: EtmpObligations = EtmpObligations(
            obligations = Seq(
              EtmpObligation(
                identification = EtmpObligationIdentification(iossNumber),
                obligationDetails = Seq(
                  EtmpObligationDetails(
                    status = Open,
                    periodKey = toEtmpPeriodString(currentPeriod)
                  )
                )
              )
            )
          )

          when(mockEtmpObligationsConnector.getObligations(any(), any())) thenReturn Right(startingObligation).toFuture

          val result = service.getPeriodsWithStatus(intermediaryNumber, commencementDate).futureValue

          val expectedList: Map[String, List[PeriodWithStatus]] = Map(
            iossNumber -> List(
              PeriodWithStatus(iossNumber, currentPeriod, Next)
            )
          )

          result `mustBe` expectedList
        }

        "must not return periods before the commencementDate" in {

          val commencementDate: LocalDate = LocalDate.now(stubClock)
          val periodBeforeCommencementDate: Period = getRunningPeriod(commencementDate.minusMonths(1))

          val startingObligation: EtmpObligations = EtmpObligations(
            obligations = Seq(
              EtmpObligation(
                identification = EtmpObligationIdentification(iossNumber),
                obligationDetails = Seq(
                  EtmpObligationDetails(
                    status = Open,
                    periodKey = toEtmpPeriodString(periodBeforeCommencementDate)
                  )
                )
              )
            )
          )

          when(mockEtmpObligationsConnector.getObligations(any(), any())) thenReturn Right(startingObligation).toFuture

          val result = service.getPeriodsWithStatus(intermediaryNumber, commencementDate).futureValue

          val expectedList: Map[String, List[PeriodWithStatus]] = Map(
            iossNumber -> List(
              PeriodWithStatus(iossNumber, getNext(periodBeforeCommencementDate), Next)
            )
          )

          result `mustBe` expectedList
          result must not contain periodBeforeCommencementDate
        }

        "must not return periods later than today" in {

          val today: LocalDate = LocalDate.now(stubClock)
          val commencementDate: LocalDate = today.minusMonths(2)
          val currentPeriod: Period = getRunningPeriod(today)
          val nextPeriodAfterCurrent: Period = getNext(currentPeriod)

          val startingObligation: EtmpObligations = EtmpObligations(
            obligations = Seq(
              EtmpObligation(
                identification = EtmpObligationIdentification(iossNumber),
                obligationDetails = Seq(
                  EtmpObligationDetails(
                    status = Open,
                    periodKey = toEtmpPeriodString(nextPeriodAfterCurrent)
                  )
                )
              )
            )
          )

          when(mockEtmpObligationsConnector.getObligations(any(), any())) thenReturn Right(startingObligation).toFuture

          val result = service.getPeriodsWithStatus(intermediaryNumber, commencementDate).futureValue

          val expectedList: Map[String, List[PeriodWithStatus]] = Map(
            iossNumber -> List(
              PeriodWithStatus(iossNumber, getRunningPeriod(commencementDate), Overdue),
              PeriodWithStatus(iossNumber, getNext(getRunningPeriod(commencementDate)), Due)
            )
          )

          result `mustBe` expectedList
          result must not contain nextPeriodAfterCurrent
        }
      }
    }

    ".addNextIfAllCompleted" - {

      val service: ObligationsService = new ObligationsService(mockEtmpObligationsConnector, stubClock)

      "must return the current periods and the next period if all other listed periods are submission status complete" in {

        val commencementDate: LocalDate = LocalDate.now(stubClock)
        val periodWithStatus: PeriodWithStatus = arbitraryPeriodWithStatus.arbitrary.sample.value
          .copy(period = getRunningPeriod(commencementDate))

        val nextPeriod: Period = getNext(periodWithStatus.period)
        val periodAfterNext: Period = getNext(nextPeriod)
        val nextPeriodAfterLast: Period = getNext(periodAfterNext)

        val currentPeriodsWithStatus: List[PeriodWithStatus] = List(
          PeriodWithStatus(iossNumber, periodWithStatus.period, Complete),
          PeriodWithStatus(iossNumber, nextPeriod, Complete),
          PeriodWithStatus(iossNumber, periodAfterNext, Complete)
        )

        val addNextIfAllCompleted = PrivateMethod[List[PeriodWithStatus]](Symbol("addNextIfAllCompleted"))

        val result = service invokePrivate addNextIfAllCompleted(iossNumber, currentPeriodsWithStatus, commencementDate)

        result `mustBe` currentPeriodsWithStatus :+ PeriodWithStatus(iossNumber, nextPeriodAfterLast, Next)
      }

      "must return the current periods if some of the other listed periods are submission status compete" in {

        val commencementDate: LocalDate = LocalDate.now(stubClock)
        val periodWithStatus: PeriodWithStatus = arbitraryPeriodWithStatus.arbitrary.sample.value
          .copy(period = getRunningPeriod(commencementDate))

        val nextPeriod: Period = getNext(periodWithStatus.period)
        val periodAfterNext: Period = getNext(nextPeriod)

        val currentPeriodsWithStatus: List[PeriodWithStatus] = List(
          PeriodWithStatus(iossNumber, periodWithStatus.period, Complete),
          PeriodWithStatus(iossNumber, nextPeriod, Next),
          PeriodWithStatus(iossNumber, periodAfterNext, Next)
        )

        val addNextIfAllCompleted = PrivateMethod[List[PeriodWithStatus]](Symbol("addNextIfAllCompleted"))

        val result = service invokePrivate addNextIfAllCompleted(iossNumber, currentPeriodsWithStatus, commencementDate)

        result `mustBe` currentPeriodsWithStatus
      }

      "must return the current periods if none of the other listed periods are compete" in {

        val commencementDate: LocalDate = LocalDate.now(stubClock)
        val periodWithStatus: PeriodWithStatus = arbitraryPeriodWithStatus.arbitrary.sample.value
          .copy(period = getRunningPeriod(commencementDate))

        val nextPeriod: Period = getNext(periodWithStatus.period)
        val periodAfterNext: Period = getNext(nextPeriod)

        val allPeriodsWithStatus: List[PeriodWithStatus] = List(
          PeriodWithStatus(iossNumber, periodWithStatus.period, Due),
          PeriodWithStatus(iossNumber, nextPeriod, SubmissionStatus.Next),
          PeriodWithStatus(iossNumber, periodAfterNext, SubmissionStatus.Next)
        )

        val addNextIfAllCompleted = PrivateMethod[List[PeriodWithStatus]](Symbol("addNextIfAllCompleted"))

        val result = service invokePrivate addNextIfAllCompleted(iossNumber, allPeriodsWithStatus, commencementDate)

        result `mustBe` allPeriodsWithStatus
      }


      "must return the next period if there are no other current periods available" in {

        val commencementDate: LocalDate = LocalDate.now(stubClock)
        val nextPeriodWithStatus: PeriodWithStatus = PeriodWithStatus(
          iossNumber = iossNumber,
          period = getRunningPeriod(commencementDate),
          status = Next
        )

        val addNextIfAllCompleted = PrivateMethod[List[PeriodWithStatus]](Symbol("addNextIfAllCompleted"))

        val result = service invokePrivate addNextIfAllCompleted(iossNumber, List.empty, commencementDate)

        result `mustBe` List(nextPeriodWithStatus)
      }
    }

    ".getNextPeriod" - {

      val service = new ObligationsService(mockEtmpObligationsConnector, stubClock)

      "must return the current period when the commencement date is on or before the last day of the current period" in {

        val stubDate: LocalDate = LocalDate.ofInstant(stubClock.instant(), ZoneId.systemDefault())
        val currentPeriod: Period = getRunningPeriod(stubDate)

        val commencementDate = currentPeriod.lastDay

        val getNextPeriod = PrivateMethod[Period](Symbol("getNextPeriod"))

        val result = service invokePrivate getNextPeriod(List.empty, commencementDate)

        result `mustBe` currentPeriod
      }

      "must return the next period when the commencement date is after the last day of the current period" in {

        val stubDate: LocalDate = LocalDate.ofInstant(stubClock.instant(), ZoneId.systemDefault())
        val currentPeriod: Period = getRunningPeriod(stubDate)
        val nextPeriod: Period = getNext(currentPeriod)

        val commencementDate = currentPeriod.lastDay.plusDays(1)

        val getNextPeriod = PrivateMethod[Period](Symbol("getNextPeriod"))

        val result = service invokePrivate getNextPeriod(List.empty, commencementDate)

        result `mustBe` nextPeriod
      }

      "must return the next period of a given list of periods" in {

        val stubDate: LocalDate = LocalDate.ofInstant(stubClock.instant(), ZoneId.systemDefault())
        val currentPeriod: Period = getRunningPeriod(stubDate)

        val period1: Period = getNext(currentPeriod)
        val period2: Period = getNext(period1)
        val period3: Period = getNext(period2)

        val nextPeriods: Seq[Period] = List(
          period1, period2, period3
        )

        val commencementDate = currentPeriod.lastDay.plusDays(1)

        val getNextPeriod = PrivateMethod[Period](Symbol("getNextPeriod"))

        val result = service invokePrivate getNextPeriod(nextPeriods, commencementDate)

        result `mustBe` getNext(period3)
      }
    }

    ".getObligations" - {

      val service = new ObligationsService(mockEtmpObligationsConnector, stubClock)

      "must return EtmpObligations when connector returns Right with a valid payload" in {

        when(mockEtmpObligationsConnector.getObligations(any(), any())) thenReturn Right(etmpObligations).toFuture

        val getObligations = PrivateMethod[Future[EtmpObligations]](Symbol("getObligations"))

        val result = service invokePrivate getObligations(intermediaryNumber, etmpObligationsQueryParameters, ec)

        result.futureValue `mustBe` etmpObligations
      }

      "must throw an exception when connector returns Left error" in {

        val errorMessage: String = "There was an error retrieving obligations"
        val error: EtmpObligationsError = EtmpObligationsError(INTERNAL_SERVER_ERROR.toString, errorMessage)

        when(mockEtmpObligationsConnector.getObligations(any(), any())) thenReturn Left(error).toFuture

        val getObligations = PrivateMethod[Future[EtmpObligations]](Symbol("getObligations"))

        val result = service invokePrivate getObligations(intermediaryNumber, etmpObligationsQueryParameters, ec)

        whenReady(result.failed) { exp =>
          exp `mustBe` a[Exception]
          exp.getMessage `mustBe` errorMessage
        }
      }
    }
  }
}

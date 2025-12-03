package uk.gov.hmrc.iossintermediarydashboard.services

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.models.*
import uk.gov.hmrc.iossintermediarydashboard.models.Period.getNext
import uk.gov.hmrc.iossintermediarydashboard.models.SubmissionStatus.{Complete, Due, Overdue}
import uk.gov.hmrc.iossintermediarydashboard.utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class ReturnsServiceSpec extends BaseSpec {

  private val mockObligationsService: ObligationsService = mock[ObligationsService]

  private val commencementDate: LocalDate = LocalDate.now(stubClock).minusMonths(6)

  private val iossNumber2: String = "IM9001234568"

  private val firstCompletePeriodWithStatus: PeriodWithStatus = PeriodWithStatus(
    iossNumber = iossNumber,
    period = StandardPeriod(
      year = commencementDate.getYear,
      month = commencementDate.getMonth
    ),
    status = Complete
  )

  private val completePeriodWithStatus2 = firstCompletePeriodWithStatus
    .copy(period = getNext(firstCompletePeriodWithStatus.period))

  private val completePeriodWithStatus3 = completePeriodWithStatus2
    .copy(period = getNext(completePeriodWithStatus2.period))

  private val incompletePeriodWithStatus1 = completePeriodWithStatus3
    .copy(period = getNext(completePeriodWithStatus3.period), status = Overdue)

  private val incompletePeriodWithStatus2 = incompletePeriodWithStatus1
    .copy(period = getNext(incompletePeriodWithStatus1.period), status = Overdue)

  private val incompletePeriodWithStatus3 = incompletePeriodWithStatus2
    .copy(period = getNext(incompletePeriodWithStatus2.period), status = Due)

  private val mappedPeriodsWithStatus = Map(
    iossNumber -> Seq(
      firstCompletePeriodWithStatus,
      completePeriodWithStatus2,
      completePeriodWithStatus3,
      incompletePeriodWithStatus1,
      incompletePeriodWithStatus2,
      incompletePeriodWithStatus3
    ),
    iossNumber2 -> Seq(
      firstCompletePeriodWithStatus,
      completePeriodWithStatus2,
      completePeriodWithStatus3,
      incompletePeriodWithStatus1,
      incompletePeriodWithStatus2,
      incompletePeriodWithStatus3
    )
  )

  private val completeReturns: Seq[Return] = Seq(
    Return(
      period = firstCompletePeriodWithStatus.period,
      firstDay = firstCompletePeriodWithStatus.period.firstDay,
      lastDay = firstCompletePeriodWithStatus.period.lastDay,
      dueDate = firstCompletePeriodWithStatus.period.paymentDeadline,
      submissionStatus = Complete,
      inProgress = false,
      isOldest = false
    ),
    Return(
      period = completePeriodWithStatus2.period,
      firstDay = completePeriodWithStatus2.period.firstDay,
      lastDay = completePeriodWithStatus2.period.lastDay,
      dueDate = completePeriodWithStatus2.period.paymentDeadline,
      submissionStatus = Complete,
      inProgress = false,
      isOldest = false
    ),
    Return(
      period = completePeriodWithStatus3.period,
      firstDay = completePeriodWithStatus3.period.firstDay,
      lastDay = completePeriodWithStatus3.period.lastDay,
      dueDate = completePeriodWithStatus3.period.paymentDeadline,
      submissionStatus = Complete,
      inProgress = false,
      isOldest = false
    )
  )

  private val incompleteReturns: Seq[Return] = Seq(
    Return(
      period = incompletePeriodWithStatus1.period,
      firstDay = incompletePeriodWithStatus1.period.firstDay,
      lastDay = incompletePeriodWithStatus1.period.lastDay,
      dueDate = incompletePeriodWithStatus1.period.paymentDeadline,
      submissionStatus = Overdue,
      inProgress = false,
      isOldest = true
    ),
    Return(
      period = incompletePeriodWithStatus2.period,
      firstDay = incompletePeriodWithStatus2.period.firstDay,
      lastDay = incompletePeriodWithStatus2.period.lastDay,
      dueDate = incompletePeriodWithStatus2.period.paymentDeadline,
      submissionStatus = Overdue,
      inProgress = false,
      isOldest = false
    ),
    Return(
      period = incompletePeriodWithStatus3.period,
      firstDay = incompletePeriodWithStatus3.period.firstDay,
      lastDay = incompletePeriodWithStatus3.period.lastDay,
      dueDate = incompletePeriodWithStatus3.period.paymentDeadline,
      submissionStatus = Due,
      inProgress = false,
      isOldest = false
    )
  )

  "ReturnsService" - {

    ".getCurrentReturns" - {

      "must return Seq[CurrentReturns] when obligations are retrieved for all intermediary clients" in {

        when(mockObligationsService.getPeriodsWithStatus(any(), any())(any())) thenReturn mappedPeriodsWithStatus.toFuture

        val service = new ReturnsService(mockObligationsService)

        val result = service.getCurrentReturns(intermediaryNumber, commencementDate).futureValue

        val expectedResult: Seq[CurrentReturns] = Seq(
          CurrentReturns(
            iossNumber = iossNumber,
            incompleteReturns = incompleteReturns,
            completedReturns = completeReturns
          ),
          CurrentReturns(
            iossNumber = iossNumber2,
            incompleteReturns = incompleteReturns,
            completedReturns = completeReturns
          )
        )

        result `mustBe` expectedResult
      }

      "must return a Seq[CurrentReturns] when obligations are retrieved and no returns exist for clients" in {

        val emptyMappedPeriodsWithStatus: Map[String, Seq[Nothing]] = Map(
          iossNumber -> Seq.empty,
          iossNumber2 -> Seq.empty
        )

        when(mockObligationsService.getPeriodsWithStatus(any(), any())(any())) thenReturn emptyMappedPeriodsWithStatus.toFuture

        val service = new ReturnsService(mockObligationsService)

        val result = service.getCurrentReturns(intermediaryNumber, commencementDate).futureValue

        val expectedResult: Seq[CurrentReturns] = Seq(
          CurrentReturns(
            iossNumber = iossNumber,
            incompleteReturns = Seq.empty,
            completedReturns = Seq.empty
          ),
          CurrentReturns(
            iossNumber = iossNumber2,
            incompleteReturns = Seq.empty,
            completedReturns = Seq.empty
          )
        )

        result `mustBe` expectedResult
      }
    }
  }
}

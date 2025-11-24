package uk.gov.hmrc.iossintermediarydashboard.services

import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpObligationsConnector
import uk.gov.hmrc.iossintermediarydashboard.models.Period.getNext
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.{EtmpObligations, EtmpObligationsQueryParameters}
import uk.gov.hmrc.iossintermediarydashboard.models.{Period, PeriodWithStatus, StandardPeriod, SubmissionStatus}
import uk.gov.hmrc.iossintermediarydashboard.utils.Formatters.etmpDateFormatter
import uk.gov.hmrc.iossintermediarydashboard.utils.FutureSyntax.FutureOps

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ObligationsService @Inject()(
                                    etmpObligationsConnector: EtmpObligationsConnector,
                                    clock: Clock
                                  ) {

  private val today: LocalDate = LocalDate.now(clock)

  def getPeriodsEWithStatus(
                             intermediaryNumber: String,
                             commencementDate: LocalDate
                           )(implicit ec: ExecutionContext): Future[Seq[PeriodWithStatus]] = {

    val allPeriods: Seq[Period] = getAllPeriodsBetween(commencementDate, today)

    val etmpObligationsQueryParameters = EtmpObligationsQueryParameters(
      fromDate = commencementDate.format(etmpDateFormatter),
      toDate = today.plusMonths(1).withDayOfMonth(1).minusDays(1).format(etmpDateFormatter),
      None
    )

    for {
      etmpObligations <- getObligations(intermediaryNumber, etmpObligationsQueryParameters)
    } yield {
      val allFulfilledPeriods: List[PeriodWithStatus] = allPeriods.map { period =>
        decideStatus(period, etmpObligations.getFulfilledPeriods)
      }.toList

      addNextIfAllCompleted(allFulfilledPeriods, commencementDate)
    }
  }

  private def getObligations(
                              intermediaryNumber: String,
                              queryParameters: EtmpObligationsQueryParameters
                            )(implicit ec: ExecutionContext): Future[EtmpObligations] = {
    etmpObligationsConnector.getObligations(intermediaryNumber, queryParameters).flatMap {
      case Right(etmpObligations: EtmpObligations) => etmpObligations.toFuture
      case Left(error) => throw new Exception(error.body)
    }
  }

  private def addNextIfAllCompleted(currentPeriods: List[PeriodWithStatus], commencementLocalDate: LocalDate): List[PeriodWithStatus] = {
    val nextPeriod: Period = getNextPeriod(currentPeriods.map(_.period), commencementLocalDate)
    if (currentPeriods.forall(_.status == SubmissionStatus.Complete)) {
      currentPeriods ++ Seq(PeriodWithStatus(nextPeriod, SubmissionStatus.Next))
    } else {
      currentPeriods
    }
  }

  private def getNextPeriod(periods: List[Period], commencementLocalDate: LocalDate): Period = {
    val runningPeriod: Period = Period.getRunningPeriod(today)
    if (periods.nonEmpty) {
      getNext(periods.maxBy(_.lastDay.toEpochDay))
    } else {
      if (commencementLocalDate.isAfter(runningPeriod.lastDay)) {
        Period.getRunningPeriod(commencementLocalDate)
      } else {
        runningPeriod
      }
    }
  }

  private def getAllPeriodsBetween(commencementDate: LocalDate, endDate: LocalDate): List[Period] = {
    val startPeriod = StandardPeriod(commencementDate.getYear, commencementDate.getMonth)
    getPeriodsUntilDate(startPeriod, endDate)
  }

  private def getPeriodsUntilDate(currentPeriod: Period, endDate: LocalDate): List[Period] = {
    if (currentPeriod.lastDay.isBefore(endDate)) {
      List(currentPeriod) ++ getPeriodsUntilDate(getNext(currentPeriod), endDate)
    } else {
      List.empty
    }
  }

  private def decideStatus(period: Period, fulfilledPeriods: List[Period]): PeriodWithStatus = {
    if (fulfilledPeriods.contains(period)) {
      PeriodWithStatus(period, SubmissionStatus.Complete)
    } else if (LocalDate.now(clock).isAfter(period.paymentDeadline)) {
      PeriodWithStatus(period, SubmissionStatus.Overdue)
    } else {
      PeriodWithStatus(period, SubmissionStatus.Due)
    }
  }
}

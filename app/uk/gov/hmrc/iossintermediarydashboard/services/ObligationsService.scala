/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.iossintermediarydashboard.services

import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpObligationsConnector
import uk.gov.hmrc.iossintermediarydashboard.models.Period.getNext
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations.{EtmpObligations, EtmpObligationsQueryParameters}
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.{EtmpClientDetails, EtmpExclusion}
import uk.gov.hmrc.iossintermediarydashboard.models.{Period, PeriodWithStatus, StandardPeriod, SubmissionStatus}
import uk.gov.hmrc.iossintermediarydashboard.utils.Formatters.etmpDateFormatter

import java.time.{Clock, LocalDate}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ObligationsService @Inject()(
                                    etmpObligationsConnector: EtmpObligationsConnector,
                                    checkExclusionsService: CheckExclusionsService,
                                    clock: Clock
                                  ) {

  private val today: LocalDate = LocalDate.now(clock)

  def getPeriodsWithStatus(
                            intermediaryNumber: String,
                            commencementDate: LocalDate,
                            clientDetailsForExclusion: Seq[EtmpClientDetails]
                          )(implicit ec: ExecutionContext): Future[Map[String, Seq[PeriodWithStatus]]] = {

    val allPeriodsToDate: Seq[Period] = getAllPeriodsBetween(commencementDate, today)

    val etmpObligationsQueryParameters = EtmpObligationsQueryParameters(
      fromDate = commencementDate.format(etmpDateFormatter),
      toDate = today.plusMonths(1).withDayOfMonth(1).minusDays(1).format(etmpDateFormatter),
      None
    )

    for {
      etmpObligations <- getObligations(intermediaryNumber, etmpObligationsQueryParameters)
    } yield {

      etmpObligations.obligations.groupBy(_.identification.referenceNumber).toList.collect {
        (iossNumber, allObligationsForClient) =>
          val clientExcluded: Boolean = 
            clientDetailsForExclusion.find(_.clientIossID == iossNumber) match
            case Some(value) if value.clientExcluded => true
            case None => false

          allObligationsForClient.flatMap { etmpObligation =>

            val allCurrentPeriodsForClient: List[PeriodWithStatus] = allPeriodsToDate.map { period =>
              determineStatus(iossNumber, period, etmpObligation.obligationDetails.getFulfilledPeriods, clientExcluded)
            }.toList

            addNextIfAllCompleted(iossNumber, allCurrentPeriodsForClient, commencementDate)
          }
      }.flatten.groupBy(_.iossNumber)
    }
  }

  private def getObligations(
                              intermediaryNumber: String,
                              queryParameters: EtmpObligationsQueryParameters
                            )(implicit ec: ExecutionContext): Future[EtmpObligations] = {
    etmpObligationsConnector.getObligations(intermediaryNumber, queryParameters).map {
      case Right(etmpObligations: EtmpObligations) => etmpObligations
      case Left(error) => throw new Exception(error.body)
    }
  }

  private def addNextIfAllCompleted(iossNumber: String, currentPeriods: List[PeriodWithStatus], commencementLocalDate: LocalDate): List[PeriodWithStatus] = {
    val nextPeriod: Period = getNextPeriod(currentPeriods.map(_.period), commencementLocalDate)
    if (currentPeriods.forall(_.status == SubmissionStatus.Complete)) {
      currentPeriods ++ Seq(PeriodWithStatus(iossNumber, nextPeriod, SubmissionStatus.Next))
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

  private def determineStatus(iossNumber: String, period: Period, fulfilledPeriods: List[Period], clientExcluded: Boolean): PeriodWithStatus = {
    
    if (fulfilledPeriods.contains(period)) {
      PeriodWithStatus(iossNumber, period, SubmissionStatus.Complete)
    } else if (checkExclusionsService.isPeriodExpired(period, clientExcluded)) {
      PeriodWithStatus(iossNumber, period, SubmissionStatus.Expired)
    } else if (checkExclusionsService.isPeriodExcluded(period, clientExcluded)) {
      PeriodWithStatus(iossNumber, period, SubmissionStatus.Excluded)
    } else if (LocalDate.now(clock).isAfter(period.paymentDeadline)) {
      PeriodWithStatus(iossNumber, period, SubmissionStatus.Overdue)
    } else {
      PeriodWithStatus(iossNumber, period, SubmissionStatus.Due)
    }
  }
}

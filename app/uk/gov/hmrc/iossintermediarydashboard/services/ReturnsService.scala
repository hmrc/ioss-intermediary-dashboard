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

import uk.gov.hmrc.iossintermediarydashboard.models.SubmissionStatus.Complete
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.EtmpExclusion
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.EtmpExclusionReason.Reversal
import uk.gov.hmrc.iossintermediarydashboard.models.{CurrentReturns, Period, PeriodWithStatus, Return, SubmissionStatus}
import uk.gov.hmrc.iossintermediarydashboard.models.Period.getPrevious

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnsService @Inject(
                              obligationsService: ObligationsService
                            )(implicit ec: ExecutionContext) {

  def getCurrentReturns(
                         intermediaryNumber: String,
                         parsedCommencementDate: LocalDate,
                         exclusions: List[EtmpExclusion]
                       ): Future[Seq[CurrentReturns]] = {
    for {
      availablePeriodsWithStatus <- obligationsService.getPeriodsWithStatus(intermediaryNumber, parsedCommencementDate, exclusions)
    } yield currentReturnsFromPeriodWithStatus(availablePeriodsWithStatus, exclusions)
  }

  private def currentReturnsFromPeriodWithStatus(periodsWithStatus: Map[String, Seq[PeriodWithStatus]], exclusions: List[EtmpExclusion]): Seq[CurrentReturns] = {

    periodsWithStatus.map {
      case (iossNumber, clientObligations) =>

        val incompletePeriods: Seq[PeriodWithStatus] = clientObligations
          .filterNot(periodsWithStatus => periodsWithStatus.status == Complete)

        val completedPeriods: Seq[PeriodWithStatus] = clientObligations
          .filter(periodsWithStatus => periodsWithStatus.status == Complete)

        val oldestPeriod: Option[PeriodWithStatus] = incompletePeriods.sortBy(_.period).headOption

        val incompleteReturns: Seq[Return] =
          incompletePeriods.sortBy(_.period).map { incompletePeriod =>
            Return.fromPeriod(
              period = incompletePeriod.period,
              submissionStatus = incompletePeriod.status,
              inProgress = false, // TODO VEI-204
              isOldest = oldestPeriod.contains(incompletePeriod)
            )
          }

        val completedReturns: Seq[Return] = completedPeriods.sortBy(_.period).map { periodsWithStatus =>
          Return.fromPeriod(
            period = periodsWithStatus.period,
            submissionStatus = periodsWithStatus.status,
            inProgress = false,
            isOldest = false
          )
        }

        val finalReturnCompleted = hasSubmittedFinalReturn(exclusions, clientObligations)

        CurrentReturns(
          iossNumber = iossNumber,
          incompleteReturns = incompleteReturns,
          completedReturns = completedReturns,
          finalReturnsCompleted = finalReturnCompleted
        )
    }.toSeq
  }

  private def hasSubmittedFinalReturn(exclusions: List[EtmpExclusion], periodsWithStatus: Seq[PeriodWithStatus]): Boolean = {
    exclusions.headOption.filterNot(_.exclusionReason == Reversal) match {
      case Some(EtmpExclusion(_, _, effectiveDate, _)) =>
        periodsWithStatus.exists {
          periodWithStatus =>

            val runningPeriod = Period.getRunningPeriod(effectiveDate)

            val periodToCheck = if (runningPeriod.firstDay == effectiveDate) {
              getPrevious(runningPeriod)
            } else {
              runningPeriod
            }
            periodWithStatus.period == periodToCheck &&
              periodWithStatus.status == SubmissionStatus.Complete
        }
      case _ => false
    }
  }
}

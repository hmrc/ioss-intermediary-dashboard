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
import uk.gov.hmrc.iossintermediarydashboard.models.{CurrentReturns, PeriodWithStatus, Return}

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnsService @Inject(
                              obligationsService: ObligationsService
                            )(implicit ec: ExecutionContext) {

  def getCurrentReturns(
                         intermediaryNumber: String,
                         parsedCommencementDate: LocalDate
                       ): Future[Seq[CurrentReturns]] = {
    for {
      availablePeriodsWithStatus <- obligationsService.getPeriodsWithStatus(intermediaryNumber, parsedCommencementDate)
    } yield currentReturnsFromPeriodWithStatus(availablePeriodsWithStatus)
  }

  private def currentReturnsFromPeriodWithStatus(periodsWithStatus: Map[String, Seq[PeriodWithStatus]]): Seq[CurrentReturns] = {

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

        CurrentReturns(
          iossNumber = iossNumber,
          incompleteReturns = incompleteReturns,
          completedReturns = completedReturns
        )
    }.toSeq
  }
}

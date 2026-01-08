/*
 * Copyright 2024 HM Revenue & Customs
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

import uk.gov.hmrc.iossintermediarydashboard.config.Constants.excludedReturnAndPaymentExpiry
import uk.gov.hmrc.iossintermediarydashboard.models.Period
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.{EtmpClientDetails, EtmpExclusion}
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.EtmpExclusionReason.Reversal

import java.time.{Clock, LocalDate}
import javax.inject.Inject

class CheckExclusionsService @Inject()(clock: Clock) {

  private def hasActiveWindowExpired(dueDate: LocalDate): Boolean = {
    val today = LocalDate.now(clock)
    today.isAfter(dueDate.plusYears(excludedReturnAndPaymentExpiry))
  }
  
  def isPeriodExcluded(period: Period, isClientExcluded: Boolean): Boolean = {

    isClientExcluded match {
      case true => !hasActiveWindowExpired(period.paymentDeadline)
      case false =>  false
    }
  }

  def isPeriodExpired(period: Period, isClientExcluded: Boolean): Boolean = {
    isClientExcluded match
      case true => hasActiveWindowExpired(period.paymentDeadline)
      case false => false
  }
}

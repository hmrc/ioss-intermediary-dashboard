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

package uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.iossintermediarydashboard.models.Period

case class EtmpObligationDetails(
                                  status: EtmpObligationsFulfilmentStatus,
                                  periodKey: String
                                )

object EtmpObligationDetails {

  implicit val format: OFormat[EtmpObligationDetails] = Json.format[EtmpObligationDetails]

  implicit class FromEtmpObligationsToPeriods(etmpObligationDetails: Seq[EtmpObligationDetails]) {
    def getFulfilledPeriods: List[Period] = {
      etmpObligationDetails
        .filter(_.status == EtmpObligationsFulfilmentStatus.Fulfilled)
        .map(p => Period.fromKey(p.periodKey))
        .toList
    }
  }
}

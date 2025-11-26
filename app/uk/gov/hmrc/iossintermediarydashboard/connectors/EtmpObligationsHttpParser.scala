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

package uk.gov.hmrc.iossintermediarydashboard.connectors

import play.api.Logging
import play.api.http.Status.OK
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations.EtmpObligations
import uk.gov.hmrc.iossintermediarydashboard.models.responses.{ErrorResponse, EtmpObligationsError, InvalidJson}

object EtmpObligationsHttpParser extends Logging {

  type EtmpObligationsResponse = Either[ErrorResponse, EtmpObligations]

  implicit object EtmpObligationsReads extends HttpReads[EtmpObligationsResponse] {

    override def read(method: String, url: String, response: HttpResponse): EtmpObligationsResponse = {
      response.status match {
        case OK =>
          response.json.validate[EtmpObligations] match {
            case JsSuccess(etmpObligations, _) => Right(etmpObligations)
            case JsError(errors) =>
              logger.warn(s"Failed when trying to parse EtmpObligations JSON with errors: $errors.")
              Left(InvalidJson)
          }

        case status =>
          logger.info(s"Received response from ETMP Obligations with status ${response.status} and body ${response.body}.")
          if (response.body.isEmpty) {
            logger.error(s"Response from ETMP Obligations with status ${status} has an empty response body.")
            Left(EtmpObligationsError(s"UNEXPECTED_$status", "The response body was empty."))
          } else {
            logger.error(s"There was an unexpected error from ETMP Obligations with status $status and response body ${response.body}.")
            Left(EtmpObligationsError(status.toString, response.body))
          }
      }
    }
  }
}

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
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.EtmpDisplayRegistration
import uk.gov.hmrc.iossintermediarydashboard.models.responses.{ErrorResponse, InvalidJson, ServerError}

object EtmpRegistrationHttpParser extends Logging {

  type EtmpDisplayRegistrationResponse = Either[ErrorResponse, EtmpDisplayRegistration]

  implicit object EtmpDisplayRegistrationReads extends HttpReads[EtmpDisplayRegistrationResponse] {

    override def read(method: String, url: String, response: HttpResponse): EtmpDisplayRegistrationResponse = {
      response.status match {
        case OK =>
          (response.json \ "etmpDisplayRegistration").validate[EtmpDisplayRegistration] match {
            case JsSuccess(etmpDisplayRegistration, _) => Right(etmpDisplayRegistration)
            case JsError(errors) =>
              logger.error(s"Failed when parsing ETMP Display Registration response JSON with status ${response.status} and error $errors")
              Left(InvalidJson)
          }

        case status =>
          logger.error(s"An error occurred when retrieving ETMP Display Registration with status $status and response body ${response.body}")
          Left(ServerError)
      }
    }
  }
}

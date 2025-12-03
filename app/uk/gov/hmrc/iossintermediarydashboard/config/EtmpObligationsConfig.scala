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

package uk.gov.hmrc.iossintermediarydashboard.config

import play.api.Configuration
import play.api.http.HeaderNames.*
import play.api.http.MimeTypes
import uk.gov.hmrc.iossintermediarydashboard.utils.Formatters
import uk.gov.hmrc.iossintermediarydashboard.utils.Formatters.dateTimeFormatter

import java.time.{Clock, LocalDateTime}
import javax.inject.Inject

class EtmpObligationsConfig @Inject()(
                                       config: Configuration,
                                       clock: Clock
                                     ) {

  val baseUrl: Service = config.get[Service]("microservice.services.etmp-obligations")
  val authorizationToken: String = config.get[String]("microservice.services.etmp-obligations.authorizationToken")
  val environment: String = config.get[String]("microservice.services.etmp-obligations.environment")
  val idType: String = config.get[String]("microservice.services.etmp-obligations.idType")
  val regimeType: String = config.get[String]("microservice.services.etmp-obligations.regimeType")

  private val XCorrelationId: String = "X-Correlation-Id"

  def headers(correlationId: String): Seq[(String, String)] = Seq(
    CONTENT_TYPE -> MimeTypes.JSON,
    ACCEPT -> MimeTypes.JSON,
    AUTHORIZATION -> s"Bearer $authorizationToken",
    "Environment" -> environment,
    DATE -> dateTimeFormatter.format(LocalDateTime.now(clock)),
    XCorrelationId -> correlationId,
    X_FORWARDED_HOST -> "MDTP"
  )
}
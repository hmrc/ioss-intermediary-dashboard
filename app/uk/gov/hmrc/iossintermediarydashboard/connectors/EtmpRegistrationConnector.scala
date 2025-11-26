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
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps}
import uk.gov.hmrc.iossintermediarydashboard.config.EtmpDisplayRegistrationConfig
import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpRegistrationHttpParser.{EtmpDisplayRegistrationReads, EtmpDisplayRegistrationResponse}
import uk.gov.hmrc.iossintermediarydashboard.models.responses.GatewayTimeout

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EtmpRegistrationConnector @Inject()(
                                           httpClientV2: HttpClientV2,
                                           etmpDisplayRegistrationConfig: EtmpDisplayRegistrationConfig
                                         )(implicit ec: ExecutionContext) extends Logging {

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private def displayRegistrationHeaders(correlationId: String): Seq[(String, String)] = etmpDisplayRegistrationConfig.eisEtmpGetHeaders(correlationId)

  def getRegistration(intermediaryNumber: String): Future[EtmpDisplayRegistrationResponse] = {

    val correlationId = UUID.randomUUID.toString
    val headersWithCorrelationId = displayRegistrationHeaders(correlationId)

    httpClientV2.get(url"${etmpDisplayRegistrationConfig.baseUrl}vec/iossregistration/viewreg/v1/$intermediaryNumber")
      .setHeader(headersWithCorrelationId: _*)
      .execute[EtmpDisplayRegistrationResponse]
      .recover {
        case e: HttpException =>
          logger.error(s"There wss an error retrieving ???? with status ${e.responseCode} and body ${e.message}")
          Left(GatewayTimeout)
      }
  }
}

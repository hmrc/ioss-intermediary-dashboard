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
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps}
import uk.gov.hmrc.iossintermediarydashboard.config.{EtmpObligationsConfig, Service}
import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpObligationsHttpParser.{EtmpObligationsReads, EtmpObligationsResponse}
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.EtmpObligationsQueryParameters
import uk.gov.hmrc.iossintermediarydashboard.models.responses.GatewayTimeout

import java.net.URL
import java.time.Clock
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class EtmpObligationsConnector @Inject()(
                                               httpClientV2: HttpClientV2,
                                               etmpObligationsConfig: EtmpObligationsConfig,
                                               clock: Clock
                                             )(implicit ec: ExecutionContext) extends Logging {

  private implicit val emptyHc: HeaderCarrier = HeaderCarrier()

  private val etmpObligationsBaseUrl: Service = etmpObligationsConfig.baseUrl

  private def getObligationUrl(intermediaryNumber: String): URL = {
    url"$etmpObligationsBaseUrl${etmpObligationsConfig.idType}/$intermediaryNumber/${etmpObligationsConfig.regimeType}"
  }

  def getObligations(intermediaryNumber: String, queryParameters: EtmpObligationsQueryParameters): Future[EtmpObligationsResponse] = {
    httpClientV2.get(getObligationUrl(intermediaryNumber))
      // TODO -> Add Headers
      .transform(_.withQueryStringParameters(queryParameters.toSeqQueryParams: _*))
      .execute[EtmpObligationsResponse]
  }.recover {
    case e: HttpException =>
      logger.error(s"")
      Left(GatewayTimeout)
  }
}

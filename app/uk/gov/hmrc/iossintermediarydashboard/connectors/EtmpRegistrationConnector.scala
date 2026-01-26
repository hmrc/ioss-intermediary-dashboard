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

import play.api.{Configuration, Logging}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpException, StringContextOps}
import uk.gov.hmrc.iossintermediarydashboard.config.Service
import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpRegistrationHttpParser.{EtmpDisplayRegistrationReads, EtmpDisplayRegistrationResponse}
import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpRegistrationHttpParser.{EtmpNetpDisplayRegistrationReads, EtmpNetpDisplayRegistrationResponse}
import uk.gov.hmrc.iossintermediarydashboard.models.responses.GatewayTimeout

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EtmpRegistrationConnector @Inject()(
                                           httpClientV2: HttpClientV2,
                                           config: Configuration
                                         )(implicit ec: ExecutionContext) extends Logging {

  private val displayRegistrationUrl: Service = config.get[Service]("microservice.services.ioss-intermediary-registration")
  private val iossRegistrationUrl: Service = config.get[Service]("microservice.services.ioss-netp-registration")


  def getRegistration(intermediaryNumber: String)(implicit hc: HeaderCarrier): Future[EtmpDisplayRegistrationResponse] = {

    httpClientV2.get(url"${displayRegistrationUrl}/get-registration/$intermediaryNumber")
      .execute[EtmpDisplayRegistrationResponse]
      .recover {
        case e: HttpException =>
          logger.error(s"There was an unexpected error retrieving ETMP Display Registration with status ${e.responseCode} and body ${e.message}")
          Left(GatewayTimeout)
      }
  }
  
  def getIossNetpRegistration(iossNumber: String)(implicit hc: HeaderCarrier): Future[EtmpNetpDisplayRegistrationResponse] = {
    val thisThing = httpClientV2.get(url"${iossRegistrationUrl}/registrations/$iossNumber")
      .execute[EtmpNetpDisplayRegistrationResponse]
      
      thisThing.foreach(unwrapped => println(s"\n\n $unwrapped"))
    
      thisThing.recover {
        case e: HttpException =>
          logger.error(s"There was an unexpected error retrieving ETMP Display Registration with status ${e.responseCode} and body ${e.message}")
          Left(GatewayTimeout)
      }
  }
}

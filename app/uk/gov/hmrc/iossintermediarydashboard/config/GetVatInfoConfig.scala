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

import javax.inject.Inject

class GetVatInfoConfig @Inject()(config: Configuration) {

  val baseUrl: Service = config.get[Service]("microservice.services.get-vat-info")
  val authorizationToken: String = config.get[String]("microservice.services.get-vat-info.authorizationToken")
  val environment: String = config.get[String]("microservice.services.get-vat-info.environment")
}

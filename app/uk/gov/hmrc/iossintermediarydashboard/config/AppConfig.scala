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

import javax.inject.{Inject, Singleton}
import play.api.Configuration

@Singleton
class AppConfig @Inject()(config: Configuration) {

  val appName: String = config.get[String]("appName")

  val vatEnrolment: String = config.get[String]("features.enrolment.vat-enrolment-key")
  val vatEnrolmentIdentifierName: String = config.get[String]("features.enrolment.vat-enrolment-identifier-name")
  val intermediaryEnrolment: String = config.get[String]("features.enrolment.intermediary-enrolment-key")
  val intermediaryEnrolmentIdentifierName: String = config.get[String]("features.enrolment.intermediary-enrolment-identifier-name")
}

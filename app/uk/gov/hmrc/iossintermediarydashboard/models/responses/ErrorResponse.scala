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

package uk.gov.hmrc.iossintermediarydashboard.models.responses

sealed trait ErrorResponse {
  val body: String
}

case object InvalidVrn extends ErrorResponse {
  override val body: String = "Invalid VRN"
}

case object InvalidJson extends ErrorResponse {
  override val body: String = "Invalid Response"
}

case object NotFound extends ErrorResponse {
  override val body = "Not found"
}

case object Conflict extends ErrorResponse {
  override val body = "Conflict"
}

case object ServerError extends ErrorResponse {
  override val body = "Internal server error"
}

case object ServiceUnavailable extends ErrorResponse {
  override val body: String = "Service unavailable"
}

case object GatewayTimeout extends ErrorResponse {
  override val body: String = "Gateway timeout"
}

case class UnexpectedResponseStatus(status: Int, body: String) extends ErrorResponse




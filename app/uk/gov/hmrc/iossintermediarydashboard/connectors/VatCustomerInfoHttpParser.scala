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

import uk.gov.hmrc.http.{HttpReads, HttpResponse}
import uk.gov.hmrc.iossintermediarydashboard.models.des.VatCustomerInfo
import uk.gov.hmrc.iossintermediarydashboard.models.responses.ErrorResponse

object VatCustomerInfoHttpParser extends BaseHttpParser {

  override val serviceName: String = "DES"

  type VatCustomerInfoResponse = Either[ErrorResponse, VatCustomerInfo]

  implicit object VatCustomerInfoReads extends HttpReads[VatCustomerInfoResponse] {
    override def read(method: String, url: String, response: HttpResponse): VatCustomerInfoResponse =
      parseResponse[VatCustomerInfo](response)(VatCustomerInfo.desReads)
  }
}


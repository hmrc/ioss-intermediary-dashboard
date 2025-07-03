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

package uk.gov.hmrc.iossintermediarydashboard.models.des

import play.api.libs.json.{JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.models.DesAddress

import java.time.LocalDate

class VatCustomerInfoSpec extends BaseSpec {

  "VatCustomerInfo" - {

    "must deserialise" - {

      "when all optional fields are present" in {

        val json = Json.obj(
          "approvedInformation" -> Json.obj(
            "PPOB" -> Json.obj(
              "address" -> Json.obj(
                "line1" -> "line 1",
                "line2" -> "line 2",
                "line3" -> "line 3",
                "line4" -> "line 4",
                "line5" -> "line 5",
                "postCode" -> "postcode",
                "countryCode" -> "CC"
              )
            ),
            "customerDetails" -> Json.obj(
              "effectiveRegistrationDate" -> "2020-01-02",
              "partyType" -> "ZZ",
              "organisationName" -> "Foo",
              "individual" -> Json.obj(
                "firstName" -> "A",
                "middleName" -> "B",
                "lastName" -> "C"
              ),
              "singleMarketIndicator" -> false
            ),
            "deregistration" -> Json.obj(
              "effectDateOfCancellation" -> "2021-01-02"
            )
          )
        )

        val expectedResult = VatCustomerInfo(
          desAddress = DesAddress("line 1", Some("line 2"), Some("line 3"), Some("line 4"), Some("line 5"), Some("postcode"), "CC"),
          registrationDate = Some(LocalDate.of(2020, 1, 2)),
          organisationName = Some("Foo"),
          singleMarketIndicator = false,
          individualName = Some("A B C"),
          deregistrationDecisionDate = Some(LocalDate.of(2021, 1, 2))
        )

        json.validate[VatCustomerInfo](VatCustomerInfo.desReads) mustBe JsSuccess(expectedResult)
      }

      "when all optional fields are absent" in {

        val json = Json.obj(
          "approvedInformation" -> Json.obj(
            "PPOB" -> Json.obj(
              "address" -> Json.obj(
                "line1" -> "line 1",
                "countryCode" -> "CC"
              )
            ),
            "customerDetails" -> Json.obj(
              "singleMarketIndicator" -> false
            )
          )
        )

        val expectedResult = VatCustomerInfo(
          desAddress = DesAddress("line 1", None, None, None, None, None, "CC"),
          registrationDate = None,
          organisationName = None,
          singleMarketIndicator = false,
          individualName = None,
          deregistrationDecisionDate = None
        )

        json.validate[VatCustomerInfo](VatCustomerInfo.desReads) mustBe JsSuccess(expectedResult)
      }
    }
  }
}


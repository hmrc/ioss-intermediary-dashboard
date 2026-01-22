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

package uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec

class EtmpNetpDisplayRegistrationSpec extends BaseSpec {

  private val etmpNetpDisplayRegistration: EtmpNetpDisplayRegistration = arbitraryNetpEtmpDisplayRegistration.arbitrary.sample.value

  "EtmpNetpDisplayRegistration" - {

    "must deserialise/serialise from and to EtmpNetpDisplayRegistration" in {

      val json = Json.obj(
        "customerIdentification" -> etmpNetpDisplayRegistration.customerIdentification,
        "tradingNames" -> etmpNetpDisplayRegistration.tradingNames,
        "clientDetails" -> etmpNetpDisplayRegistration.clientDetails,
        "otherAddress" -> etmpNetpDisplayRegistration.otherAddress,
        "schemeDetails" -> etmpNetpDisplayRegistration.schemeDetails,
        "exclusions" -> etmpNetpDisplayRegistration.exclusions,
        "adminUse" -> etmpNetpDisplayRegistration.adminUse
      )

      val expectedResult = EtmpNetpDisplayRegistration(
        customerIdentification = etmpNetpDisplayRegistration.customerIdentification,
        tradingNames = etmpNetpDisplayRegistration.tradingNames,
        clientDetails = etmpNetpDisplayRegistration.clientDetails,
        otherAddress = etmpNetpDisplayRegistration.otherAddress,
        schemeDetails = etmpNetpDisplayRegistration.schemeDetails,
        exclusions = etmpNetpDisplayRegistration.exclusions,
        adminUse = etmpNetpDisplayRegistration.adminUse
      )

      json.validate[EtmpNetpDisplayRegistration] `mustBe` JsSuccess(expectedResult)
      Json.toJson(expectedResult) `mustBe` json
    }

    "must deserialise/serialise from and to EtmpNetpDisplayRegistration with optional values not present" in {

      val json = Json.obj(
        "customerIdentification" -> etmpNetpDisplayRegistration.customerIdentification,
        "tradingNames" -> Seq.empty[String],
        "clientDetails" -> Seq.empty[String],
        "schemeDetails" -> etmpNetpDisplayRegistration.schemeDetails,
        "exclusions" -> Seq.empty[String],
        "adminUse" -> etmpNetpDisplayRegistration.adminUse
      )

      val expectedResult = EtmpNetpDisplayRegistration(
        customerIdentification = etmpNetpDisplayRegistration.customerIdentification,
        tradingNames = Seq.empty,
        clientDetails = Seq.empty,
        otherAddress = None,
        schemeDetails = etmpNetpDisplayRegistration.schemeDetails,
        exclusions = Seq.empty,
        adminUse = etmpNetpDisplayRegistration.adminUse
      )

      json.validate[EtmpNetpDisplayRegistration] `mustBe` JsSuccess(expectedResult)
      Json.toJson(expectedResult) `mustBe` json
    }


    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EtmpNetpDisplayRegistration] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "tradingNames" -> 123456
      )

      json.validate[EtmpNetpDisplayRegistration] `mustBe` a[JsError]
    }
  }
}

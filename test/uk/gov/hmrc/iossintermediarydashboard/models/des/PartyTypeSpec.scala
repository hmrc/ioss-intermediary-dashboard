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

import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsString, JsSuccess}
import uk.gov.hmrc.iossintermediarydashboard.models.des.PartyType.{OtherPartyType, VatGroup}

class PartyTypeSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with OptionValues {

  "PartyType" - {

    "must deserialise from the string `Z2` to VatGroup" in {

      JsString("Z2").validate[PartyType] mustBe JsSuccess(VatGroup)
    }

    "must deserialise from any string other than `Z2` to `OtherPartyType`" in {

      forAll(arbitrary[String]) {
        value =>
          whenever(value != "Z2") {
            JsString(value).validate[PartyType] mustBe JsSuccess(OtherPartyType)
          }
      }
    }
  }
}

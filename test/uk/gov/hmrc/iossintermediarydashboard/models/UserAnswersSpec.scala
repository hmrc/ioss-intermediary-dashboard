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

package uk.gov.hmrc.iossintermediarydashboard.models

import play.api.libs.json.{JsError, JsSuccess, Json}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.models.des.VatCustomerInfo

class UserAnswersSpec extends BaseSpec {

  private val userAnswers: UserAnswers = arbitraryUserAnswers.arbitrary.sample.value

  "UserAnswersSpec" - {

    "must serialise/deserialise to and from UserAnswers" - {

      "with optional answers present" in {

        val json = Json.obj(
          "_id" -> userAnswers.id,
          "journeyId" -> userAnswers.journeyId,
          "data" -> userAnswers.data,
          "vatInfo" -> userAnswers.vatInfo,
          "lastUpdated" -> Json.obj(
            "$date" -> Json.obj(
              "$numberLong" -> userAnswers.lastUpdated.toEpochMilli.toString
            )
          )
        )

        val expectedAnswers: UserAnswers = UserAnswers(
          id = userAnswers.id,
          journeyId = userAnswers.journeyId,
          data = userAnswers.data,
          vatInfo = userAnswers.vatInfo,
          lastUpdated = userAnswers.lastUpdated
        )

        Json.toJson(expectedAnswers) `mustBe` json
        json.validate[UserAnswers] `mustBe` JsSuccess(expectedAnswers)
      }
    }

    "with optional answers missing" in {

      val json = Json.obj(
        "_id" -> userAnswers.id,
        "journeyId" -> userAnswers.journeyId,
        "data" -> userAnswers.data,
        "lastUpdated" -> Json.obj(
          "$date" -> Json.obj(
            "$numberLong" -> userAnswers.lastUpdated.toEpochMilli.toString
          )
        )
      )

      val expectedAnswers: UserAnswers = UserAnswers(
        id = userAnswers.id,
        journeyId = userAnswers.journeyId,
        data = userAnswers.data,
        vatInfo = None,
        lastUpdated = userAnswers.lastUpdated
      )

      json.validate[UserAnswers] `mustBe` JsSuccess(expectedAnswers)
      Json.toJson(expectedAnswers) `mustBe` json
    }

    "must handle missing fields during deserialisation" in {

      val json = Json.obj()

      json.validate[UserAnswers] mustBe a[JsError]
    }

    "must handle invalid data during deserialisation" in {

      val json = Json.obj(
        "id" -> 123456,
        "journeyId" -> userAnswers.journeyId,
        "data" -> userAnswers.data,
        "vatInfo" -> userAnswers.vatInfo,
        "lastUpdated" -> userAnswers.lastUpdated
      )

      json.validate[UserAnswers] mustBe a[JsError]
    }
  }
}

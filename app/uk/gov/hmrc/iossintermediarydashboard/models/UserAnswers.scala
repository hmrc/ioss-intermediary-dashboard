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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.iossintermediarydashboard.models.des.VatCustomerInfo
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant

final case class UserAnswers(
                              id: String,
                              journeyId: String,
                              data: JsObject,
                              vatInfo: Option[VatCustomerInfo],
                              lastUpdated: Instant
                            )

object UserAnswers {

  val reads: Reads[UserAnswers] = {

    (
      (__ \ "_id").read[String] and
        (__ \ "journeyId").read[String] and
        (__ \ "data").read[JsObject] and
        (__ \ "vatInfo").readNullable[VatCustomerInfo] and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
      )(UserAnswers.apply _)
  }

  val writes: OWrites[UserAnswers] = {

    (
      (__ \ "_id").write[String] and
        (__ \ "journeyId").write[String] and
        (__ \ "data").write[JsObject] and
        (__ \ "vatInfo").writeNullable[VatCustomerInfo] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      )(userAnswers => Tuple.fromProductTyped(userAnswers))
  }

  implicit val format: OFormat[UserAnswers] = OFormat(reads, writes)
}

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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*
import uk.gov.hmrc.iossintermediarydashboard.models.DesAddress

import java.time.LocalDate

case class VatCustomerInfo(
                            desAddress: DesAddress,
                            registrationDate: Option[LocalDate],
                            organisationName: Option[String],
                            individualName: Option[String],
                            singleMarketIndicator: Boolean,
                            deregistrationDecisionDate: Option[LocalDate]
                          )

object VatCustomerInfo {

  private def fromDesPayload(
                              address: DesAddress,
                              registrationDate: Option[LocalDate],
                              partyType: Option[PartyType], // To be implemented at a later date
                              organisationName: Option[String],
                              individualFirstName: Option[String],
                              individualMiddleName: Option[String],
                              individualLastName: Option[String],
                              singleMarketIndicator: Boolean,
                              deregistrationDecisionDate: Option[LocalDate]
                            ): VatCustomerInfo = {

    val firstName = individualFirstName.fold("")(fn => s"$fn ")
    val middleName = individualMiddleName.fold("")(mn => s"$mn ")
    val lastName = individualLastName.fold("")(ln => s"$ln")

    VatCustomerInfo(
      desAddress = address,
      registrationDate = registrationDate,
      organisationName = organisationName,
      individualName = if (individualFirstName.isEmpty && individualMiddleName.isEmpty && individualLastName.isEmpty) {
        None
      } else {
        Some(s"$firstName$middleName$lastName")
      },
      singleMarketIndicator = singleMarketIndicator,
      deregistrationDecisionDate = deregistrationDecisionDate
    )
  }

  val desReads: Reads[VatCustomerInfo] = {
    (
      (__ \ "approvedInformation" \ "PPOB" \ "address").read[DesAddress] and
        (__ \ "approvedInformation" \ "customerDetails" \ "effectiveRegistrationDate").readNullable[LocalDate] and
        (__ \ "approvedInformation" \ "customerDetails" \ "partyType").readNullable[PartyType] and
        (__ \ "approvedInformation" \ "customerDetails" \ "organisationName").readNullable[String] and
        (__ \ "approvedInformation" \ "customerDetails" \ "individual" \ "firstName").readNullable[String] and
        (__ \ "approvedInformation" \ "customerDetails" \ "individual" \ "middleName").readNullable[String] and
        (__ \ "approvedInformation" \ "customerDetails" \ "individual" \ "lastName").readNullable[String] and
        (__ \ "approvedInformation" \ "customerDetails" \ "singleMarketIndicator").read[Boolean] and
        (__ \ "approvedInformation" \ "deregistration" \ "effectDateOfCancellation").readNullable[LocalDate]
      )(VatCustomerInfo.fromDesPayload _)
  }

  implicit val standardReads: Reads[VatCustomerInfo] =
    Json.reads[VatCustomerInfo]

  implicit val writes: OWrites[VatCustomerInfo] =
    Json.writes[VatCustomerInfo]
}

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

package uk.gov.hmrc.iossintermediarydashboard.generators

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.iossintermediarydashboard.models.des.VatCustomerInfo
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations.*
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.*
import uk.gov.hmrc.iossintermediarydashboard.models.{Period, *}

import java.time.*
import java.time.temporal.ChronoUnit

trait Generators {

  implicit lazy val arbitraryUkAddress: Arbitrary[UkAddress] = {
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        county <- Gen.option(arbitrary[String])
        postCode <- arbitrary[String]
      } yield UkAddress(line1, line2, townOrCity, county, postCode)
    }
  }

  implicit val arbitraryAddress: Arbitrary[Address] = {
    Arbitrary {
      Gen.oneOf(
        arbitrary[UkAddress],
        arbitrary[InternationalAddress],
        arbitrary[DesAddress]
      )
    }
  }

  implicit lazy val arbitraryInternationalAddress: Arbitrary[InternationalAddress] = {
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        townOrCity <- arbitrary[String]
        stateOrRegion <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        country <- arbitrary[Country]
      } yield InternationalAddress(line1, line2, townOrCity, stateOrRegion, postCode, country)
    }
  }

  implicit lazy val arbitraryDesAddress: Arbitrary[DesAddress] = {
    Arbitrary {
      for {
        line1 <- arbitrary[String]
        line2 <- Gen.option(arbitrary[String])
        line3 <- Gen.option(arbitrary[String])
        line4 <- Gen.option(arbitrary[String])
        line5 <- Gen.option(arbitrary[String])
        postCode <- Gen.option(arbitrary[String])
        countryCode <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
      } yield DesAddress(line1, line2, line3, line4, line5, postCode, countryCode)
    }
  }

  implicit lazy val arbitraryVrn: Arbitrary[Vrn] = {
    Arbitrary {
      for {
        chars <- Gen.listOfN(9, Gen.numChar)
      } yield Vrn(chars.mkString(""))
    }
  }

  implicit val arbitraryVatCustomerInfo: Arbitrary[VatCustomerInfo] = {
    Arbitrary {
      for {
        desAddress <- arbitraryDesAddress.arbitrary
        registrationDate <- arbitrary[LocalDate]
        organisationName <- arbitrary[String]
        individualName <- arbitrary[String]
        singleMarketIndicator <- arbitrary[Boolean]
        deregistrationDecisionDate <- arbitrary[LocalDate]
      }
      yield
        VatCustomerInfo(
          desAddress = desAddress,
          registrationDate = Some(registrationDate),
          organisationName = Some(organisationName),
          individualName = Some(individualName),
          singleMarketIndicator = singleMarketIndicator,
          deregistrationDecisionDate = Some(deregistrationDecisionDate)
        )
    }
  }

  implicit lazy val arbitraryUserAnswers: Arbitrary[UserAnswers] = {
    Arbitrary {
      for {
        id <- arbitrary[String]
        journeyId <- arbitrary[String]
        data = JsObject(Seq("test" -> Json.toJson("test")))
        vatInfo <- arbitraryVatCustomerInfo.arbitrary
        lastUpdated = Instant.now().truncatedTo(ChronoUnit.MILLIS)
      } yield {
        UserAnswers(
          id = id,
          journeyId = journeyId,
          data = data,
          vatInfo = Some(vatInfo),
          lastUpdated = lastUpdated
        )
      }
    }
  }

  def datesBetween(min: LocalDate, max: LocalDate): Gen[LocalDate] = {

    def toMillis(date: LocalDate): Long =
      date.atStartOfDay.atZone(ZoneOffset.UTC).toInstant.toEpochMilli

    Gen.choose(toMillis(min), toMillis(max)).map {
      millis =>
        Instant.ofEpochMilli(millis).atOffset(ZoneOffset.UTC).toLocalDate
    }
  }

  implicit lazy val arbitraryDate: Arbitrary[LocalDate] = {
    Arbitrary {
      datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2025, 12, 31))
    }
  }

  implicit lazy val generatePeriodKey: String = {

    val year: String = arbitraryDate.arbitrary.sample.map(_.getYear.toString).head
    val monthRepresentation: Seq[String] = Seq("AA", "AB", "AC", "AD", "AE", "AF", "AG", "AH", "AI", "AJ", "AK", "AL")
    val randomMonth: String = Gen.oneOf(monthRepresentation).sample.head
    s"${year.substring(2, 4)}$randomMonth"
  }

  implicit lazy val arbitraryEtmpObligationDetails: Arbitrary[EtmpObligationDetails] = {
    Arbitrary {
      for {
        status <- Gen.oneOf(EtmpObligationsFulfilmentStatus.values)
        periodKey = generatePeriodKey
      } yield {
        EtmpObligationDetails(
          status = status,
          periodKey = periodKey
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpObligationIdentification: Arbitrary[EtmpObligationIdentification] = {
    Arbitrary {
      for {
        referenceNumber <- arbitrary[String]
      } yield {
        EtmpObligationIdentification(
          referenceNumber = referenceNumber
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpObligation: Arbitrary[EtmpObligation] = {
    Arbitrary {
      for {
        identification <- arbitraryEtmpObligationIdentification.arbitrary
        obligationDetails <- Gen.listOfN(2, arbitraryEtmpObligationDetails.arbitrary)
      } yield {
        EtmpObligation(
          identification = identification,
          obligationDetails = obligationDetails
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpObligations: Arbitrary[EtmpObligations] = {
    Arbitrary {
      for {
        obligations <- Gen.listOfN(2, arbitraryEtmpObligation.arbitrary)
      } yield {
        EtmpObligations(
          obligations = obligations
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpObligationsQueryParameters: Arbitrary[EtmpObligationsQueryParameters] = {
    Arbitrary {
      for {
        fromDate <- arbitraryDate.arbitrary
        toDate <- arbitraryDate.arbitrary
        status <- Gen.oneOf(EtmpObligationsFulfilmentStatus.values)

      } yield {
        EtmpObligationsQueryParameters(
          fromDate = fromDate.toString,
          toDate = toDate.toString,
          status = Some(status.toString)
        )
      }
    }
  }

  implicit lazy val arbitraryPeriod: Arbitrary[Period] = {
    Arbitrary {
      for {
        year <- Gen.choose(2022, 2099)
        month <- Gen.oneOf(Month.values.toSeq)
      } yield StandardPeriod(
        year = year,
        month = month
      )
    }
  }

  implicit lazy val arbitraryPeriodWithStatus: Arbitrary[PeriodWithStatus] = {
    Arbitrary {
      for {
        iossNumber <- arbitrary[String]
        period <- arbitraryPeriod.arbitrary
        submissionStatus <- Gen.oneOf(SubmissionStatus.values)
      } yield PeriodWithStatus(
        iossNumber = iossNumber,
        period = period,
        status = submissionStatus
      )
    }
  }

  implicit lazy val arbitraryOtherIossIntermediaryRegistrations: Arbitrary[EtmpOtherIossIntermediaryRegistrations] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        intermediaryNumber <- genIntermediaryNumber
      } yield {
        EtmpOtherIossIntermediaryRegistrations(
          issuedBy = issuedBy,
          intermediaryNumber = intermediaryNumber
        )
      }
    }
  }

  implicit lazy val arbitraryIntermediaryDetails: Arbitrary[EtmpIntermediaryDetails] = {
    Arbitrary {
      for {
        otherIossIntermediaryRegistrations <- Gen.listOfN(2, arbitraryOtherIossIntermediaryRegistrations.arbitrary)
      } yield {
        EtmpIntermediaryDetails(
          otherIossIntermediaryRegistrations = otherIossIntermediaryRegistrations
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpOtherAddress: Arbitrary[EtmpOtherAddress] = {
    Arbitrary {
      for {
        issuedBy <- Gen.listOfN(2, Gen.alphaChar).map(_.mkString)
        tradingName <- Gen.listOfN(20, Gen.alphaChar).map(_.mkString)
        addressLine1 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        addressLine2 <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        townOrCity <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        regionOrState <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
        postcode <- Gen.listOfN(35, Gen.alphaChar).map(_.mkString)
      } yield EtmpOtherAddress(
        issuedBy,
        Some(tradingName),
        addressLine1,
        Some(addressLine2),
        townOrCity,
        Some(regionOrState),
        postcode
      )
    }
  }

  implicit lazy val arbitraryEtmpDisplaySchemeDetails: Arbitrary[EtmpDisplaySchemeDetails] = {
    Arbitrary {
      for {
        commencementDate <- arbitrary[LocalDate].map(_.toString)
        euRegistrationDetails <- Gen.listOfN(3, arbitraryEtmpDisplayEuRegistrationDetails.arbitrary)
        contactName <- Gen.alphaStr
        businessTelephoneNumber <- Gen.alphaNumStr
        businessEmailId <- Gen.alphaStr
        unusableStatus <- arbitrary[Boolean]
        nonCompliant <- Gen.oneOf("1", "2")
      } yield {
        EtmpDisplaySchemeDetails(
          commencementDate = commencementDate,
          euRegistrationDetails = euRegistrationDetails,
          contactName = contactName,
          businessTelephoneNumber = businessTelephoneNumber,
          businessEmailId = businessEmailId,
          unusableStatus = unusableStatus,
          nonCompliantReturns = Some(nonCompliant),
          nonCompliantPayments = Some(nonCompliant)
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpExclusion: Arbitrary[EtmpExclusion] = {
    Arbitrary {
      for {
        exclusionReason <- Gen.oneOf(EtmpExclusionReason.values)
        effectiveDate <- arbitrary[LocalDate]
        decisionDate <- arbitrary[LocalDate]
        quarantine <- arbitrary[Boolean]
      } yield {
        EtmpExclusion(
          exclusionReason = exclusionReason,
          effectiveDate = effectiveDate,
          decisionDate = decisionDate,
          quarantine = quarantine
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpClientDetails: Arbitrary[EtmpClientDetails] = {
    Arbitrary {
      for {
        clientName <- Gen.alphaStr
        clientIossID <- Gen.alphaNumStr
        clientExcluded <- arbitrary[Boolean]
      } yield {
        EtmpClientDetails(
          clientName = clientName,
          clientIossID = clientIossID,
          clientExcluded = clientExcluded
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpDisplayRegistration: Arbitrary[EtmpDisplayRegistration] = {
    Arbitrary {
      for {
        customerIdentification <- arbitraryEtmpCustomerIdentification.arbitrary
        tradingNames <- Gen.listOfN(3, arbitraryEtmpTradingName.arbitrary)
        clientDetails <- Gen.listOfN(3, arbitraryEtmpClientDetails.arbitrary)
        intermediaryDetails <- arbitraryIntermediaryDetails.arbitrary
        otherAddress <- arbitraryEtmpOtherAddress.arbitrary
        schemeDetails <- arbitraryEtmpDisplaySchemeDetails.arbitrary
        exclusions <- Gen.listOfN(1, arbitraryEtmpExclusion.arbitrary)
        bankDetails <- arbitraryEtmpBankDetails.arbitrary
        adminUse <- arbitraryEtmpAdminUse.arbitrary
      } yield {
        EtmpDisplayRegistration(
          customerIdentification = customerIdentification,
          tradingNames = tradingNames,
          clientDetails = clientDetails,
          intermediaryDetails = Some(intermediaryDetails),
          otherAddress = Some(otherAddress),
          schemeDetails = schemeDetails,
          exclusions = exclusions,
          bankDetails = bankDetails,
          adminUse = adminUse
        )
      }
    }
  }

  implicit lazy val arbitraryBic: Arbitrary[Bic] = {
    val asciiCodeForA = 65
    val asciiCodeForN = 78
    val asciiCodeForP = 80
    val asciiCodeForZ = 90

    Arbitrary {
      for {
        firstChars <- Gen.listOfN(6, Gen.alphaUpperChar).map(_.mkString)
        char7 <- Gen.oneOf(Gen.alphaUpperChar, Gen.choose(2, 9).map(_.toString.head))
        char8 <- Gen.oneOf(
          Gen.choose(asciiCodeForA, asciiCodeForN).map(_.toChar),
          Gen.choose(asciiCodeForP, asciiCodeForZ).map(_.toChar),
          Gen.choose(0, 9).map(_.toString.head)
        )
        lastChars <- Gen.option(Gen.listOfN(3, Gen.oneOf(Gen.alphaUpperChar, Gen.numChar)).map(_.mkString))
      } yield Bic(s"$firstChars$char7$char8${lastChars.getOrElse("")}").get
    }
  }

  implicit lazy val arbitraryIban: Arbitrary[Iban] = {
    Arbitrary {
      Gen.oneOf(
        "GB94BARC10201530093459",
        "GB33BUKB20201555555555",
        "DE29100100100987654321",
        "GB24BKEN10000031510604",
        "GB27BOFI90212729823529",
        "GB17BOFS80055100813796",
        "GB92BARC20005275849855",
        "GB66CITI18500812098709",
        "GB15CLYD82663220400952",
        "GB26MIDL40051512345674",
        "GB76LOYD30949301273801",
        "GB25NWBK60080600724890",
        "GB60NAIA07011610909132",
        "GB29RBOS83040210126939",
        "GB79ABBY09012603367219",
        "GB21SCBL60910417068859",
        "GB42CPBK08005470328725"
      ).map(v => Iban(v).toOption.get)
    }
  }

  implicit lazy val arbitraryEtmpAdminUse: Arbitrary[EtmpAdminUse] = {
    Arbitrary {
      for {
        changeDate <- arbitrary[LocalDateTime]
      } yield EtmpAdminUse(changeDate = Some(changeDate))
    }
  }

  implicit lazy val arbitraryEtmpBankDetails: Arbitrary[EtmpBankDetails] = {
    Arbitrary {
      for {
        accountName <- arbitrary[String]
        bic <- arbitraryBic.arbitrary
        iban <- arbitraryIban.arbitrary
      } yield {
        EtmpBankDetails(
          accountName = accountName,
          bic = Some(bic),
          iban = iban
        )
      }
    }
  }

  implicit lazy val arbitraryEtmpCustomerIdentification: Arbitrary[EtmpCustomerIdentification] = {
    Arbitrary {
      for {
        etmpIdType <- Gen.oneOf(EtmpIdType.values)
        vrn <- Gen.alphaStr
      } yield EtmpCustomerIdentification(etmpIdType, vrn)
    }
  }

  implicit lazy val arbitraryCountry: Arbitrary[Country] = {
    Arbitrary {
      Gen.oneOf(Country.euCountries)
    }
  }

  implicit lazy val genEuTaxReference: Gen[String] = {
    Gen.listOfN(20, Gen.alphaNumChar).map(_.mkString)
  }

  implicit lazy val arbitraryEuVatNumber: Gen[String] = {
    for {
      vatNumber <- Gen.alphaNumStr
    } yield vatNumber
  }

  implicit lazy val arbitraryEtmpTradingName: Arbitrary[EtmpTradingName] = {
    Arbitrary {
      for {
        tradingName <- Gen.alphaStr
      } yield EtmpTradingName(tradingName)
    }
  }

  implicit lazy val arbitraryEtmpDisplayEuRegistrationDetails: Arbitrary[EtmpDisplayEuRegistrationDetails] = {
    Arbitrary {
      for {
        issuedBy <- arbitraryCountry.arbitrary.map(_.code)
        vatNumber <- arbitraryEuVatNumber
        taxIdentificationNumber <- genEuTaxReference
        fixedEstablishmentTradingName <- arbitraryEtmpTradingName.arbitrary.map(_.tradingName)
        fixedEstablishmentAddressLine1 <- Gen.alphaStr
        fixedEstablishmentAddressLine2 <- Gen.alphaStr
        townOrCity <- Gen.alphaStr
        regionOrState <- Gen.alphaStr
        postcode <- Gen.alphaStr
      } yield {
        EtmpDisplayEuRegistrationDetails(
          issuedBy = issuedBy,
          vatNumber = Some(vatNumber),
          taxIdentificationNumber = Some(taxIdentificationNumber),
          fixedEstablishmentTradingName = fixedEstablishmentTradingName,
          fixedEstablishmentAddressLine1 = fixedEstablishmentAddressLine1,
          fixedEstablishmentAddressLine2 = Some(fixedEstablishmentAddressLine2),
          townOrCity = townOrCity,
          regionOrState = Some(regionOrState),
          postcode = Some(postcode)
        )
      }
    }
  }

  implicit lazy val genIntermediaryNumber: Gen[String] = {
    for {
      intermediaryNumber <- Gen.listOfN(12, Gen.alphaChar).map(_.mkString)
    } yield intermediaryNumber
  }

  implicit lazy val arbitraryReturn: Arbitrary[Return] = {
    Arbitrary {
      for {
        periodWithStatus <- arbitraryPeriodWithStatus.arbitrary
      } yield {
        Return(
          period = periodWithStatus.period,
          firstDay = periodWithStatus.period.firstDay,
          lastDay = periodWithStatus.period.lastDay,
          dueDate = periodWithStatus.period.paymentDeadline,
          submissionStatus = periodWithStatus.status,
          inProgress = false,
          isOldest = false
        )
      }
    }
  }

  implicit lazy val arbitraryCurrentReturns: Arbitrary[CurrentReturns] = {
    Arbitrary {
      for {
        iossNumber <- arbitrary[String]
        incompleteReturns <- Gen.listOfN(2, arbitraryReturn.arbitrary)
        completedReturns <- Gen.listOfN(2, arbitraryReturn.arbitrary)
      } yield {
        CurrentReturns(
          iossNumber = iossNumber,
          incompleteReturns = incompleteReturns,
          completedReturns = completedReturns
        )
      }
    }
  }
}


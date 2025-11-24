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
import uk.gov.hmrc.iossintermediarydashboard.models.*
import uk.gov.hmrc.iossintermediarydashboard.models.des.VatCustomerInfo
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.{EtmpObligation, EtmpObligationDetails, EtmpObligations, EtmpObligationsFulfilmentStatus, EtmpObligationsQueryParameters}

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate, Month, ZoneOffset}

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

  implicit lazy val arbitraryCountry: Arbitrary[Country] = {
    Arbitrary {
      for {
        char1 <- Gen.alphaUpperChar
        char2 <- Gen.alphaUpperChar
        name <- arbitrary[String]
      } yield Country(s"$char1$char2", name)
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

  implicit lazy val arbitraryEtmpObligation: Arbitrary[EtmpObligation] = {
    Arbitrary {
      for {
        obligationDetails <- Gen.listOfN(2, arbitraryEtmpObligationDetails.arbitrary)
      } yield {
        EtmpObligation(
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
        period <- arbitraryPeriod.arbitrary
        submissionStatus <- Gen.oneOf(SubmissionStatus.values)
      } yield PeriodWithStatus(
        period = period,
        status = submissionStatus
      )
    }
  }
}


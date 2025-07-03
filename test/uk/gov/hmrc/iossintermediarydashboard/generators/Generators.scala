/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.iossintermediarydashboard.generators

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.iossintermediarydashboard.models.*
import uk.gov.hmrc.iossintermediarydashboard.models.des.VatCustomerInfo

import java.time.temporal.ChronoUnit
import java.time.{Instant, LocalDate}

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
  
}

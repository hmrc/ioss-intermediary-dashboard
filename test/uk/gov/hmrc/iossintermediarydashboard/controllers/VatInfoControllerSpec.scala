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

package uk.gov.hmrc.iossintermediarydashboard.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.connectors.GetVatInfoConnector
import uk.gov.hmrc.iossintermediarydashboard.models.*
import uk.gov.hmrc.iossintermediarydashboard.models.des.VatCustomerInfo
import uk.gov.hmrc.iossintermediarydashboard.models.responses.*
import uk.gov.hmrc.iossintermediarydashboard.utils.FutureSyntax.FutureOps

import java.time.LocalDate

class VatInfoControllerSpec extends BaseSpec {

  private val mockConnector = mock[GetVatInfoConnector]
  private val vatNumber = "123456789"

  ".get" - {

    "must return OK and vat information when the connector returns vat info" in {

      val vatInfo = VatCustomerInfo(
        registrationDate = Some(LocalDate.now),
        desAddress = DesAddress("line1", None, None, None, None, Some("AA11 1AA"), "GB"),
        organisationName = Some("Foo"),
        singleMarketIndicator = false,
        individualName = None,
        deregistrationDecisionDate = None
      )

      when(mockConnector.getVatCustomerDetails(any())(any())) thenReturn Right(vatInfo).toFuture

      val app = applicationBuilder()
        .overrides(bind[GetVatInfoConnector].toInstance(mockConnector))
        .build()

      running(app) {

        val request = FakeRequest(GET, routes.VatInfoController.get(vatNumber).url)
        val result = route(app, request).value

        status(result) `mustBe` OK
        contentAsJson(result) `mustBe` Json.toJson(vatInfo)
      }
    }

    "must return NotFound when the connector returns Not Found" in {

      when(mockConnector.getVatCustomerDetails(any())(any())) thenReturn Left(NotFound).toFuture

      val app = applicationBuilder().overrides(bind[GetVatInfoConnector].toInstance(mockConnector)).build()

      running(app) {

        val request = FakeRequest(GET, routes.VatInfoController.get(vatNumber).url)
        val result = route(app, request).value

        status(result) `mustBe` NOT_FOUND
      }
    }

    "must return INTERNAL_SERVER_ERROR when the connector returns a failure other than Not Found" in {

      val response = Gen.oneOf(InvalidJson, ServerError, ServiceUnavailable, InvalidVrn).sample.value

      when(mockConnector.getVatCustomerDetails(any())(any())) thenReturn Left(response).toFuture

      val app = applicationBuilder().overrides(bind[GetVatInfoConnector].toInstance(mockConnector)).build()

      running(app) {

        val request = FakeRequest(GET, routes.VatInfoController.get(vatNumber).url)
        val result = route(app, request).value

        status(result) `mustBe` INTERNAL_SERVER_ERROR
      }
    }
  }
}


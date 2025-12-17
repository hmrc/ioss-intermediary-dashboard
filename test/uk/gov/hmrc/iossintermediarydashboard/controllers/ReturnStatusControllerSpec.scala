package uk.gov.hmrc.iossintermediarydashboard.controllers

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{verifyNoInteractions, when}
import org.scalacheck.Gen
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.models.CurrentReturns
import uk.gov.hmrc.iossintermediarydashboard.services.ReturnsService
import uk.gov.hmrc.iossintermediarydashboard.utils.FutureSyntax.FutureOps

class ReturnStatusControllerSpec extends BaseSpec {

  private val mockReturnsService: ReturnsService = mock[ReturnsService]

  private val currentReturns: Seq[CurrentReturns] = Gen.listOfN(3, arbitraryCurrentReturns.arbitrary).sample.value

  "ReturnStatusController" - {

    ".getCurrentReturns" - {

      "must return OK with a Seq[CurrentReturns] payload when invoked" in {

        when(mockReturnsService.getCurrentReturns(any(), any(), any())) thenReturn currentReturns.toFuture

        val app = applicationBuilder()
          .overrides(bind[ReturnsService].toInstance(mockReturnsService))
          .build()

        running(app) {

          val request = FakeRequest(GET, routes.ReturnStatusController.getCurrentReturns(intermediaryNumber).url)
          val result = route(app, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.toJson(currentReturns)
        }
      }

      "must return an empty JSON array when there are no returns retrieved" in {

        when(mockReturnsService.getCurrentReturns(any(), any(), any())) thenReturn Seq.empty.toFuture

        val app = applicationBuilder()
          .overrides(bind[ReturnsService].toInstance(mockReturnsService))
          .build()

        running(app) {

          val request = FakeRequest(GET, routes.ReturnStatusController.getCurrentReturns(intermediaryNumber).url)
          val result = route(app, request).value

          status(result) `mustBe` OK
          contentAsJson(result) `mustBe` Json.arr()
        }
      }
      
    }
  }
}

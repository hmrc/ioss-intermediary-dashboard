package uk.gov.hmrc.iossintermediarydashboard.connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import play.api.http.Status.{GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.Json
import play.api.test.Helpers.running
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.EtmpDisplayRegistration
import uk.gov.hmrc.iossintermediarydashboard.models.responses.{GatewayTimeout, InvalidJson, ServerError}

class EtmpRegistrationConnectorSpec extends BaseSpec with WireMockHelper {

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  private def application: Application = applicationBuilder()
    .configure(
      "microservice.services.ioss-intermediary-registration.host" -> "127.0.0.1",
      "microservice.services.ioss-intermediary-registration.port" -> server.port
    )
    .build()

  "EtmpRegistrationConnector" - {

    ".getRegistration" - {

      val url: String = s"/ioss-intermediary-registration/get-registration/$intermediaryNumber"

      "must return  Right(EtmpDisplayRegistration) for a given intermediary number when the server returns OK with a valid payload" in {

        val expectedJsonResponse: String = s"""{"etmpDisplayRegistration": ${Json.toJson(etmpDisplayRegistration)}}"""

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(expectedJsonResponse)
            )
        )

        running(application) {

          val connector = application.injector.instanceOf[EtmpRegistrationConnector]

          val result = connector.getRegistration(intermediaryNumber).futureValue

          result `mustBe` Right(etmpDisplayRegistration)
        }
      }

      "must return Left(InvalidJson) when the server returns OK but is unable to parse the JSON correctly" in {

        val invalidJsonResponse: String = """{"INVALID": "ERROR"}"""

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(invalidJsonResponse)
            )
        )

        val connector = application.injector.instanceOf[EtmpRegistrationConnector]

        val result = connector.getRegistration(intermediaryNumber).futureValue

        result `mustBe` Left(InvalidJson)
      }

      "must return Left(ServerError) when the server returns an error" in {

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
            )
        )

        val connector = application.injector.instanceOf[EtmpRegistrationConnector]

        val result = connector.getRegistration(intermediaryNumber).futureValue

        result `mustBe` Left(ServerError)
      }

      "must return Left(GatewayTimeout) when a HTTP exception is thrown" in {

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse()
              .withStatus(GATEWAY_TIMEOUT)
              .withFixedDelay(21000)
            )
        )

        running(application) {

          val connector = application.injector.instanceOf[EtmpRegistrationConnector]

          val result = connector.getRegistration(intermediaryNumber)

          whenReady(result, Timeout(Span(30, Seconds))) { exp =>
            exp `mustBe` Left(GatewayTimeout)
          }
        }
      }
    }
  }
}

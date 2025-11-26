package uk.gov.hmrc.iossintermediarydashboard.connectors

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.matching.EqualToPattern
import org.scalatest.concurrent.PatienceConfiguration.Timeout
import org.scalatest.time.{Seconds, Span}
import play.api.Application
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.running
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.obligations.{EtmpObligations, EtmpObligationsQueryParameters}
import uk.gov.hmrc.iossintermediarydashboard.models.responses.{EtmpObligationsError, GatewayTimeout, InvalidJson}
import uk.gov.hmrc.iossintermediarydashboard.utils.Formatters.etmpDateFormatter

import java.time.LocalDate

class EtmpObligationsConnectorSpec extends BaseSpec with WireMockHelper {

  private val idType: String = "IOSS"
  private val regimeType: String = "IOSS"

  private val dateFrom = LocalDate.now(stubClock).format(etmpDateFormatter)
  private val dateTo = LocalDate.now(stubClock).format(etmpDateFormatter)

  private val etmpObligationsQueryParameters: EtmpObligationsQueryParameters = EtmpObligationsQueryParameters(
    fromDate = dateFrom,
    toDate = dateTo,
    status = None
  )

  private val etmpObligations: EtmpObligations = arbitraryEtmpObligations.arbitrary.sample.value

  private def application: Application = applicationBuilder()
    .configure(
      "microservice.services.etmp-obligations.host" -> "127.0.0.1",
      "microservice.services.etmp-obligations.port" -> server.port,
      "microservice.services.etmp-obligations.authorizationToken" -> "auth-token",
      "microservice.services.etmp-obligations.environment" -> "test-environment",
      "microservice.services.etmp-obligations.idType" -> idType,
      "microservice.services.etmp-obligations.regimeType" -> regimeType
    )
    .build()

  "EtmpObligationsConnector" - {

    ".getObligations" - {

      val url: String = s"/ioss-intermediary-dashboard-stub/enterprise/obligation-data/$idType/$intermediaryNumber/$regimeType"
      val urlWithQueryParams: String = s"$url?from=${etmpObligationsQueryParameters.fromDate}&to=${etmpObligationsQueryParameters.toDate}"

      "must return Right(OK) with a valid ETMP obligations response when the server returns OK with a valid response payload with no status present" in {

        val expectedJsonResponse: JsValue = Json.toJson(etmpObligations)

        server.stubFor(
          get(urlEqualTo(urlWithQueryParams))
            .withQueryParam("from", new EqualToPattern(dateFrom))
            .withQueryParam("to", new EqualToPattern(dateTo))
            .withHeader("Authorization", equalTo("Bearer auth-token"))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(expectedJsonResponse.toString)
            )
        )

        running(application) {

          val connector = application.injector.instanceOf[EtmpObligationsConnector]

          val result = connector.getObligations(intermediaryNumber, etmpObligationsQueryParameters).futureValue

          result `mustBe` Right(etmpObligations)
        }
      }

      "must return Right(OK) with a valid ETMP obligations response when the server returns OK with a valid response payload with a status present" in {

        val status: String = "O"
        val etmpObligationsQueryParametersWithStatus: EtmpObligationsQueryParameters = etmpObligationsQueryParameters.copy(status = Some(status))
        val urlWithQueryParams: String = s"$url?from=${etmpObligationsQueryParameters.fromDate}&to=${etmpObligationsQueryParameters.toDate}&status=$status"

        val expectedJsonResponse: JsValue = Json.toJson(etmpObligations)

        server.stubFor(
          get(urlEqualTo(urlWithQueryParams))
            .withQueryParam("from", new EqualToPattern(dateFrom))
            .withQueryParam("to", new EqualToPattern(dateTo))
            .withHeader("Authorization", equalTo("Bearer auth-token"))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(expectedJsonResponse.toString)
            )
        )

        running(application) {

          val connector = application.injector.instanceOf[EtmpObligationsConnector]

          val result = connector.getObligations(intermediaryNumber, etmpObligationsQueryParametersWithStatus).futureValue

          result `mustBe` Right(etmpObligations)
        }
      }

      "must return Left(InvalidJso)n when server responds with OK but JSON response cannot be parsed correctly" in {

        val invalidJson: String = """{"JSON": "INVALID"}"""

        server.stubFor(
          get(urlEqualTo(urlWithQueryParams))
            .withQueryParam("from", new EqualToPattern(dateFrom))
            .withQueryParam("to", new EqualToPattern(dateTo))
            .withHeader("Authorization", equalTo("Bearer auth-token"))
            .willReturn(aResponse()
              .withStatus(OK)
              .withBody(invalidJson)
            )
        )

        running(application) {

          val connector = application.injector.instanceOf[EtmpObligationsConnector]

          val result = connector.getObligations(intermediaryNumber, etmpObligationsQueryParameters).futureValue

          result `mustBe` Left(InvalidJson)
        }
      }

      "must return Left(EtmpObligationsError) when the server returns a response with an empty response body" in {

        server.stubFor(
          get(urlEqualTo(urlWithQueryParams))
            .withQueryParam("from", new EqualToPattern(dateFrom))
            .withQueryParam("to", new EqualToPattern(dateTo))
            .withHeader("Authorization", equalTo("Bearer auth-token"))
            .willReturn(aResponse()
              .withStatus(NOT_FOUND)
            )
        )

        running(application) {

          val connector = application.injector.instanceOf[EtmpObligationsConnector]

          val result = connector.getObligations(intermediaryNumber, etmpObligationsQueryParameters).futureValue

          val expectedResponse: EtmpObligationsError = EtmpObligationsError(s"UNEXPECTED_$NOT_FOUND", "The response body was empty.")

          result `mustBe` Left(expectedResponse)
        }
      }

      Seq(NOT_FOUND, UNPROCESSABLE_ENTITY, BAD_REQUEST, CONFLICT).foreach { status =>
        s"must return Left(EtmpObligationsError) when the server returns status $status" in {

          val errorResponseJson: String = """{}"""

          server.stubFor(
            get(urlEqualTo(urlWithQueryParams))
              .withQueryParam("from", new EqualToPattern(dateFrom))
              .withQueryParam("to", new EqualToPattern(dateTo))
              .withHeader("Authorization", equalTo("Bearer auth-token"))
              .willReturn(aResponse()
                .withStatus(status)
                .withBody(errorResponseJson)
              )
          )

          running(application) {

            val connector = application.injector.instanceOf[EtmpObligationsConnector]

            val result = connector.getObligations(intermediaryNumber, etmpObligationsQueryParameters).futureValue

            val expectedResponse: EtmpObligationsError = EtmpObligationsError(status.toString, errorResponseJson)

            result `mustBe` Left(expectedResponse)
          }
        }
      }

      "must return Left(GatewayTimeout) when an HTTP exception is thrown" in {

        server.stubFor(
          get(urlEqualTo(urlWithQueryParams))
            .withQueryParam("from", new EqualToPattern(dateFrom))
            .withQueryParam("to", new EqualToPattern(dateTo))
            .withHeader("Authorization", equalTo("Bearer auth-token"))
            .willReturn(aResponse()
              .withStatus(GATEWAY_TIMEOUT)
              .withFixedDelay(21000)
            )
        )

        running(application) {

          val connector = application.injector.instanceOf[EtmpObligationsConnector]

          val result = connector.getObligations(intermediaryNumber, etmpObligationsQueryParameters)

          whenReady(result, Timeout(Span(30, Seconds))) { exp =>
            exp `mustBe` Left(GatewayTimeout)
          }
        }
      }
    }
  }
}

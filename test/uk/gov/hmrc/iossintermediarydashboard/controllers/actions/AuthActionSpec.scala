package uk.gov.hmrc.iossintermediarydashboard.controllers.actions

import com.google.inject.Inject
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.BeforeAndAfterEach
import play.api.inject.bind
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.iossintermediarydashboard.base.BaseSpec
import uk.gov.hmrc.iossintermediarydashboard.config.AppConfig
import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpRegistrationConnector
import uk.gov.hmrc.iossintermediarydashboard.controllers.actions.TestAuthRetrievals.*
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.EtmpDisplayRegistration
import uk.gov.hmrc.iossintermediarydashboard.models.responses.{ErrorResponse, ServerError}
import uk.gov.hmrc.iossintermediarydashboard.utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends BaseSpec with BeforeAndAfterEach {

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockEtmpRegistrationConnector: EtmpRegistrationConnector = mock[EtmpRegistrationConnector]

  private val vatEnrolment = Enrolments(Set(Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "Activated")))
  private val intermediaryEnrolment = Enrolments(Set(Enrolment("HMRC-IOSS-INT", Seq(EnrolmentIdentifier("IntNumber", "IN9001234567")), "Activated")))
  private val vatAndIntermediaryEnrolment = Enrolments(Set(Enrolment("HMRC-MTD-VAT", Seq(EnrolmentIdentifier("VRN", "123456789")), "Activated"), Enrolment("HMRC-IOSS-INT", Seq(EnrolmentIdentifier("IntNumber", "IN9001234567")), "Activated")))

  private type RetrievalsType = Option[String] ~ Enrolments

  private val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  class Harness(authAction: AuthAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockAuthConnector,
      mockEtmpRegistrationConnector
    )
  }

  "Auth Action" - {

    "when the user is not logged in" - {

      "must return Unauthorized" in {

        val application = applicationBuilder().build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          val authAction = new AuthActionImpl(
            new FakeFailingAuthConnector(new MissingBearerToken),
            bodyParsers,
            mockEtmpRegistrationConnector,
            appConfig
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) `mustBe` UNAUTHORIZED
          verifyNoInteractions(mockAuthConnector)
          verifyNoInteractions(mockEtmpRegistrationConnector)
        }
      }
    }

    "when the user attempts to log in but auth unable to retrieve authorisation data" - {

      "must throw an UnauthorizedException" in {

        val application = applicationBuilder()
          .overrides(
            bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(None ~ Enrolments(Set.empty)))

          val authAction = new AuthActionImpl(
            mockAuthConnector,
            bodyParsers,
            mockEtmpRegistrationConnector,
            appConfig
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          whenReady(result.failed) { exp =>
            exp `mustBe` a[UnauthorizedException]
            exp.getMessage `mustBe` "Unable to retrieve authorisation data"
          }

          verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
          verifyNoInteractions(mockEtmpRegistrationConnector)
        }
      }
    }

    "when the user is logged in without a VAT enrolment nor Intermediary enrolment" - {

      "must return Unauthorized" in {

        val application = applicationBuilder()
          .overrides(
            bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some("id") ~ Enrolments(Set.empty)))

          val authAction = new AuthActionImpl(
            mockAuthConnector,
            bodyParsers,
            mockEtmpRegistrationConnector,
            appConfig
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) `mustBe` UNAUTHORIZED
          verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
          verifyNoInteractions(mockEtmpRegistrationConnector)
        }
      }
    }

    "when the user is logged in without a VAT enrolment but with an Intermediary enrolment" - {

      "must throw an IllegalStateException" in {

        val application = applicationBuilder()
          .overrides(
            bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some("id") ~ intermediaryEnrolment))

          val authAction = new AuthActionImpl(
            mockAuthConnector,
            bodyParsers,
            mockEtmpRegistrationConnector,
            appConfig
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          whenReady(result.failed) { exp =>
            exp `mustBe` a[IllegalStateException]
            exp.getMessage `mustBe` "Missing VAT enrolment"
          }

          verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
          verifyNoInteractions(mockEtmpRegistrationConnector)
        }
      }
    }

    "when the user is logged in with a VAT enrolment but without an Intermediary enrolment" - {

      "must return UNAUTHORIZED" in {

        val application = applicationBuilder()
          .overrides(
            bind[AuthConnector].toInstance(mockAuthConnector)
          )
          .build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some("id") ~ vatEnrolment))

          val authAction = new AuthActionImpl(
            mockAuthConnector,
            bodyParsers,
            mockEtmpRegistrationConnector,
            appConfig
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) `mustBe` UNAUTHORIZED
          verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
          verifyNoInteractions(mockEtmpRegistrationConnector)
        }
      }
    }

    "when the user is logged in with a VAT enrolment and a Intermediary enrolment" - {

      "must return OK with the retrieval of an ETMP Display Registration" in {

        val application = applicationBuilder()
          .overrides(
            bind[AuthConnector].toInstance(mockAuthConnector),
            bind[EtmpRegistrationConnector].toInstance(mockEtmpRegistrationConnector)
          )
          .build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some("id") ~ vatAndIntermediaryEnrolment))

          when(mockEtmpRegistrationConnector.getRegistration(any())(any())) thenReturn Right(etmpDisplayRegistration).toFuture

          val authAction = new AuthActionImpl(
            mockAuthConnector,
            bodyParsers,
            mockEtmpRegistrationConnector,
            appConfig
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) `mustBe` OK
          verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
          verify(mockEtmpRegistrationConnector, times(1)).getRegistration(any())(any())
        }
      }

      "must throw an Exception when the server fails to retrieve an ETMP Display Registration" in {

        val error: ErrorResponse = ServerError

        val application = applicationBuilder()
          .overrides(
            bind[AuthConnector].toInstance(mockAuthConnector),
            bind[EtmpRegistrationConnector].toInstance(mockEtmpRegistrationConnector)
          )
          .build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[AppConfig]

          when(mockAuthConnector.authorise[RetrievalsType](any(), any())(any(), any()))
            .thenReturn(Future.successful(Some("id") ~ vatAndIntermediaryEnrolment))

          when(mockEtmpRegistrationConnector.getRegistration(any())(any())) thenReturn Left(ServerError).toFuture

          val authAction = new AuthActionImpl(
            mockAuthConnector,
            bodyParsers,
            mockEtmpRegistrationConnector,
            appConfig
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          whenReady(result.failed) { exp =>
            exp `mustBe` a[Exception]
            exp.getMessage `mustBe` s"There was an error retrieving registration with error: ${error.body}."
          }
          verify(mockAuthConnector, times(1)).authorise(any(), any())(any(), any())
          verify(mockEtmpRegistrationConnector, times(1)).getRegistration(any())(any())
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}

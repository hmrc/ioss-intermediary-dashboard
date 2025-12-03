package uk.gov.hmrc.iossintermediarydashboard.controllers.actions

import org.scalatest.OptionValues
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.*
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.iossintermediarydashboard.config.AppConfig
import uk.gov.hmrc.iossintermediarydashboard.connectors.EtmpRegistrationConnector
import uk.gov.hmrc.iossintermediarydashboard.generators.Generators
import uk.gov.hmrc.iossintermediarydashboard.models.etmp.registration.EtmpDisplayRegistration
import uk.gov.hmrc.iossintermediarydashboard.models.requests.AuthorisedRequest

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class FakeAuthAction @Inject()(bodyParsers: BodyParsers.Default)
  extends AuthActionImpl(mock[AuthConnector], bodyParsers, mock[EtmpRegistrationConnector], mock[AppConfig])
    with OptionValues
    with Generators {

  private val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  override def invokeBlock[A](request: Request[A], block: AuthorisedRequest[A] => Future[Result]): Future[Result] = {
    block(AuthorisedRequest(request, "id", Vrn("123456789"), "IN9001234567", etmpDisplayRegistration))
  }
}
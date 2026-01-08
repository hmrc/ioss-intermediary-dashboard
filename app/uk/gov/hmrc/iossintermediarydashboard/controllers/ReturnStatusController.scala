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

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.iossintermediarydashboard.controllers.actions.DefaultAuthenticatedControllerComponents
import uk.gov.hmrc.iossintermediarydashboard.models.CurrentReturns
import uk.gov.hmrc.iossintermediarydashboard.services.ReturnsService
import uk.gov.hmrc.iossintermediarydashboard.utils.Formatters.etmpDateFormatter
import uk.gov.hmrc.iossintermediarydashboard.utils.FutureSyntax.FutureOps
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import java.time.LocalDate
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ReturnStatusController @Inject()(
                                        cc: DefaultAuthenticatedControllerComponents,
                                        returnsService: ReturnsService
                                      )(implicit ec: ExecutionContext) extends BackendController(cc) {

  def getCurrentReturns(intermediaryNumber: String): Action[AnyContent] = cc.auth().async {
    implicit request =>

      if(request.registration.clientDetails.isEmpty) {
        val emptyCurrentReturns: Seq[CurrentReturns] = Seq.empty // TODO -SCG- Note If no NETP (clients) returns empty
        Ok(Json.toJson(emptyCurrentReturns)).toFuture
      } else {
        val parsedCommencementDate: LocalDate = LocalDate.parse(request.registration.schemeDetails.commencementDate, etmpDateFormatter)
        val exclusions = request.registration.exclusions.toList  //TODO -SCG- Note - Exclusions for the intermediary not the client
        val clientExclusions = request.registration.clientDetails //TODO -SCG- Note - Exclusions for the client but not the reason (AC- if an exclusion is reversed, then the above rules don't apply)

        val futureCurrentReturns: Future[Seq[CurrentReturns]] = returnsService.getCurrentReturns(intermediaryNumber, parsedCommencementDate, exclusions, clientExclusions)

        for {
          currentReturns <- futureCurrentReturns
        } yield Ok(Json.toJson(currentReturns))
      }
  }
}

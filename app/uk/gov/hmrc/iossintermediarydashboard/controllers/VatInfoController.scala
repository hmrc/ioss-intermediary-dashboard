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
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.iossintermediarydashboard.connectors.GetVatInfoConnector
import uk.gov.hmrc.iossintermediarydashboard.logging.Logging
import uk.gov.hmrc.iossintermediarydashboard.models.responses.NotFound as DesNotFound
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class VatInfoController @Inject()(
                                   cc: ControllerComponents,
                                   getVatInfoConnector: GetVatInfoConnector
                                 )(implicit ec: ExecutionContext) extends BackendController(cc) with Logging {

  def get(vrn: String): Action[AnyContent] = Action.async {
    implicit request =>
      getVatInfoConnector.getVatCustomerDetails(Vrn(vrn)).map {
        case Right(response) =>
          Ok(Json.toJson(response)) 
        case Left(DesNotFound) =>
          NotFound
        case _ =>
          InternalServerError
      }
  }
}

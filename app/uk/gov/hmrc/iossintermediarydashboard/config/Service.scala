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

package uk.gov.hmrc.iossintermediarydashboard.config

import play.api.{ConfigLoader, Configuration}

final case class Service(host: String, port: String, protocol: String, basePath: String) {

  def baseUrl: String = s"$protocol://$host:$port/$basePath"

  override def toString: String = baseUrl
}

object Service {

  implicit lazy val configLoader: ConfigLoader[Service] = ConfigLoader {
    config =>
      prefix =>

        val service  = Configuration(config).get[Configuration](prefix)
        val host     = service.get[String]("host")
        val port     = service.get[String]("port")
        val protocol = service.get[String]("protocol")
        val basePath = service.get[String]("basePath")

        Service(host, port, protocol, basePath)
  }
}

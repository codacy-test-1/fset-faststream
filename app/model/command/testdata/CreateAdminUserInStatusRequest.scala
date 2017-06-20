/*
 * Copyright 2017 HM Revenue & Customs
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

package model.command.testdata

import play.api.libs.json.{ Json, OFormat }

object CreateAdminUserInStatusRequest {

  case class CreateAdminUserInStatusRequest(emailPrefix: Option[String], firstName: Option[String], lastName: Option[String],
                                            preferredName: Option[String], role: Option[String], phone: Option[String],
                                            assessor: Option[AssessorRequest]) extends CreateTestDataRequest {
  }

  object CreateAdminUserInStatusRequest {
    implicit val createAdminUserStatusRequestFormat: OFormat[CreateAdminUserInStatusRequest] = Json.format[CreateAdminUserInStatusRequest]
  }

  case class AssessorRequest(skills: Option[List[String]] = None, civilServant: Option[Boolean] = None)

  object AssessorRequest {
    implicit val assessorTestDataFormat: OFormat[AssessorRequest] = Json.format[AssessorRequest]
  }
}

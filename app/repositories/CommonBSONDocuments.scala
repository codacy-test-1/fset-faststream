/*
 * Copyright 2016 HM Revenue & Customs
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

package repositories

import model.ApplicationStatus.ApplicationStatus
import model.ProgressStatuses.ProgressStatus
import org.joda.time.DateTime
import reactivemongo.bson.BSONDocument


trait CommonBSONDocuments {

  def applicationStatusBSON(applicationStatus: ApplicationStatus) = {
    BSONDocument(
      "applicationStatus" -> applicationStatus,
      s"progress-status.$applicationStatus" -> true,
      s"progress-status-timestamp.$applicationStatus" -> DateTime.now()
    )
  }

  def applicationStatusBSON(progressStatus: ProgressStatus) = {
    BSONDocument(
      "applicationStatus" -> progressStatus.applicationStatus,
      s"progress-status.$progressStatus" -> true,
      s"progress-status-timestamp.$progressStatus" -> DateTime.now()
    )
  }
}

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

package model.persisted

import model.persisted.assessor.{ Assessor, AssessorAvailability, AssessorStatus }
import org.joda.time.LocalDate

object AssessorExamples {

  val AssessorUserId = "USR1"

  val AssessorExisting = Assessor(AssessorUserId,
    skills = List("qac", "chair"),
    civilServant = true,
    status = AssessorStatus.AVAILABILITIES_SUBMITTED,
    availability = List(
      AssessorAvailability("london", new LocalDate(2017, 10, 10)),
      AssessorAvailability("london", new LocalDate(2017, 10, 10)),
      AssessorAvailability("newcastle", new LocalDate(2017, 5, 10)),
      AssessorAvailability("newcastle", new LocalDate(2017, 5, 10)
    )))

  val AssessorNew: Assessor = AssessorExamples.AssessorExisting.copy(skills = List("assessor"),
    availability = List.empty,
    status = AssessorStatus.CREATED)

  val AssessorMerged: Assessor = AssessorExamples.AssessorExisting.copy(skills = List("assessor"))


  val AssessorWithAvailability: Assessor = AssessorExisting.copy(
    skills = List(),
    availability = List(AssessorAvailability("london", new LocalDate(2017, 11, 11))))

  val AssessorWithAvailabilityMerged: Assessor = AssessorWithAvailability.copy(
    skills = AssessorExisting.skills,
    availability = AssessorExisting.availability ++ AssessorWithAvailability.availability
  )
}
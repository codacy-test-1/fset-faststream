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

package services.onlinetesting.phase1

import model.ApplicationStatus
import model.ApplicationStatus._
import model.persisted.SchemeEvaluationResult

trait ApplicationStatusCalculator {

  def determineApplicationStatus(originalApplicationStatus: ApplicationStatus,
                                 evaluatedSchemes: List[SchemeEvaluationResult]): Option[ApplicationStatus] = {
    if (originalApplicationStatus == ApplicationStatus.PHASE1_TESTS) {
      // TODO: do the logic
      Some(ApplicationStatus.PHASE1_TESTS_PASSED)
    } else {
      None
    }
  }
}

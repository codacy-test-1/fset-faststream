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

package model

import model.persisted.{ CubiksTest, Phase2TestGroup, TestResult }
import org.joda.time.{ DateTime, DateTimeZone }

object Phase2TestProfileExamples {

  val now = DateTime.now().withZone(DateTimeZone.UTC)
  val testResult = TestResult("Ready", "norm", Some(12.5), None, None, None)

  def getEtrayTest(implicit now: DateTime) = CubiksTest(16196, usedForResults = true, 2, "cubiks", "token", "http://localhost", now, 3,
    testResult = Some(testResult))

  def profile(implicit now: DateTime) = Phase2TestGroup(now, List(getEtrayTest))

  val phase2Test = List(CubiksTest(16196, usedForResults = true, 100, "cubiks", "token1", "http://localhost", now, 2000))
  val phase2TestWithResult = phase2TestWithResults(TestResult("Ready", "norm", Some(20.5), None, None, None))

  def phase2TestWithResults(testResult: TestResult) = {
    phase2Test.map(t => t.copy(testResult = Some(testResult)))
  }
}

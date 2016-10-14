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

import model.EvaluationResults.{ Amber, Green, Red, Result }
import model.SchemeType._
import model.exchange.passmarksettings.{ PassMarkThreshold, Phase1PassMarkSettings }
import model.persisted.{ SchemeEvaluationResult, TestResult }

trait Phase1TestEvaluation {

  def evaluateForGis(schemes: List[SchemeType], sjqTestResult: TestResult, passmark: Phase1PassMarkSettings): List[SchemeEvaluationResult] = {
    schemes map { scheme =>
      val schemePassmark = passmark.findPassmarkForScheme(scheme)
      val sjqResult = evaluateResultsForExercise(sjqTestResult, schemePassmark.schemeThresholds.situational)
      SchemeEvaluationResult(scheme, sjqResult.toString)
    }
  }

  def evaluateForNonGis(schemes: List[SchemeType], sjqTestResult: TestResult, bqTestResult: TestResult,
                        passmark: Phase1PassMarkSettings): List[SchemeEvaluationResult] = {
    schemes map { scheme =>
      val schemePassmark = passmark.findPassmarkForScheme(scheme)
      val sjqResult = evaluateResultsForExercise(sjqTestResult, schemePassmark.schemeThresholds.situational)
      val bqResult = evaluateResultsForExercise(bqTestResult, schemePassmark.schemeThresholds.behavioural)

      val result = (sjqResult, bqResult) match {
        case (Red, _) => Red
        case (_, Red) => Red
        case (Amber, _) => Amber
        case (_, Amber) => Amber
        case (Green, Green) => Green
      }

      SchemeEvaluationResult(scheme, result.toString)
    }
  }

  private def evaluateResultsForExercise(testResult: TestResult, threshold: PassMarkThreshold): Result = {
    val tScore = testResult.tScore.get
    val failmark = threshold.failThreshold
    val passmark = threshold.passThreshold

    if (tScore >= passmark) {
      Green
    } else if (tScore <= failmark) {
      Red
    } else {
      Amber
    }
  }
}
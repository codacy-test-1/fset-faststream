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

package repositories.assessmentcentre

import model.ProgressStatuses.ASSESSMENT_CENTRE_AWAITING_ALLOCATION
import model._
import model.command.ApplicationForFsac
import model.persisted.fsac.{ AnalysisExercise, AssessmentCentreTests }
import model.persisted.SchemeEvaluationResult
import org.scalatest.concurrent.ScalaFutures
import repositories.{ CollectionNames, CommonRepository}
import testkit.MongoRepositorySpec


class AssessmentCentreRepositorySpec extends MongoRepositorySpec with ScalaFutures with CommonRepository {

  val collectionName: String = CollectionNames.APPLICATION

  val Commercial: SchemeId = SchemeId("Commercial")
  val Sdip: SchemeId = SchemeId("Sdip")
  val Generalist: SchemeId = SchemeId("Generalist")
  val ProjectDelivery = SchemeId("Project Delivery")

  "next Application for sift" must {
    "ignore applications in incorrect statuses and return only the Phase3 Passed_Notified applications that are not eligible for sift" in {
      insertApplicationWithPhase3TestNotifiedResults("appId1",
        List(SchemeEvaluationResult(SchemeId("Commercial"), EvaluationResults.Green.toString))).futureValue

      insertApplicationWithPhase3TestNotifiedResults("appId2",
        List(SchemeEvaluationResult(SchemeId("Commercial"), EvaluationResults.Green.toString))).futureValue
      updateApplicationStatus("appId2", ApplicationStatus.PHASE3_TESTS_PASSED)

      insertApplicationWithPhase3TestNotifiedResults("appId3",
        List(SchemeEvaluationResult(SchemeId("Commercial"), EvaluationResults.Green.toString))).futureValue
      updateApplicationStatus("appId3", ApplicationStatus.PHASE3_TESTS_FAILED)

      insertApplicationWithPhase3TestNotifiedResults("appId4",
        List(SchemeEvaluationResult(SchemeId("Project Delivery"), EvaluationResults.Green.toString))).futureValue

      insertApplicationWithPhase3TestNotifiedResults("appId5",
        List(SchemeEvaluationResult(SchemeId("Finance"), EvaluationResults.Green.toString))).futureValue

      insertApplicationWithPhase3TestNotifiedResults("appId6",
        List(SchemeEvaluationResult(SchemeId("Generalist"), EvaluationResults.Red.toString))).futureValue

      whenReady(assessmentCentreRepository.nextApplicationForAssessmentCentre(10)) { appsForAc =>
        appsForAc must contain(
          ApplicationForFsac("appId1", ApplicationStatus.PHASE3_TESTS_PASSED_NOTIFIED,
            List(SchemeEvaluationResult(SchemeId("Commercial"), EvaluationResults.Green.toString)))
        )
        appsForAc must contain(
          ApplicationForFsac("appId4", ApplicationStatus.PHASE3_TESTS_PASSED_NOTIFIED,
            List(SchemeEvaluationResult(SchemeId("Project Delivery"), EvaluationResults.Green.toString)))
        )
        appsForAc.length mustBe 2
      }
    }

    ("return no results when there are only phase 3 applications that aren't in Passed_Notified which don't apply for sift or don't have "
      + "Green/Passed results") in {
      insertApplicationWithPhase3TestNotifiedResults("appId7",
        List(SchemeEvaluationResult(SchemeId("Finance"), EvaluationResults.Green.toString))).futureValue
      insertApplicationWithPhase3TestNotifiedResults("appId8",
        List(SchemeEvaluationResult(SchemeId("Generalist"), EvaluationResults.Green.toString))).futureValue
      updateApplicationStatus("appId8", ApplicationStatus.PHASE3_TESTS_FAILED)
      insertApplicationWithPhase3TestNotifiedResults("appId9",
        List(SchemeEvaluationResult(SchemeId("Finance"), EvaluationResults.Green.toString))).futureValue
      insertApplicationWithPhase3TestNotifiedResults("appId10",
        List(SchemeEvaluationResult(SchemeId("Project Delivery"), EvaluationResults.Red.toString))).futureValue

      whenReady(assessmentCentreRepository.nextApplicationForAssessmentCentre(10)) { appsForAc =>
        appsForAc mustBe Nil
      }
    }
  }

  "progressToFsac" must {
    "progress candidates who have completed the sift phase" in {
      insertApplicationWithSiftComplete("appId11",
        List(SchemeEvaluationResult(SchemeId("Finance"), EvaluationResults.Green.toString),
          SchemeEvaluationResult(Generalist, EvaluationResults.Red.toString),
          SchemeEvaluationResult(DiplomaticService, EvaluationResults.Green.toString)))


      val nextResults = assessmentCentreRepository.nextApplicationForAssessmentCentre(1).futureValue
      nextResults mustBe List(
        ApplicationForFsac("appId11", ApplicationStatus.SIFT,
          List(SchemeEvaluationResult(Finance, EvaluationResults.Green.toString),
            SchemeEvaluationResult(Generalist, EvaluationResults.Red.toString),
            SchemeEvaluationResult(DiplomaticService, EvaluationResults.Green.toString))
        )
      )

      assessmentCentreRepository.progressToAssessmentCentre(nextResults.head, ProgressStatuses.ASSESSMENT_CENTRE_AWAITING_ALLOCATION).futureValue
    }
  }

  "getTests" must {
    "get tests when they exist" in new TestFixture {
      insertApplicationWithAssessmentCentreAwaitingAllocation("appId1")
      assessmentCentreRepository.getTests("appId1").futureValue mustBe expectedAssessmentCentreTests
    }

    "return empty when there are no tests" in new TestFixture {
      insertApplicationWithAssessmentCentreAwaitingAllocation("appId1", withTests = false)
      assessmentCentreRepository.getTests("appId1").futureValue mustBe AssessmentCentreTests()
    }
  }

  "updateTests" must {
    "update the tests key and be retrievable" in new TestFixture {
      insertApplication("appId1", ApplicationStatus.ASSESSMENT_CENTRE,
        additionalProgressStatuses = List(ASSESSMENT_CENTRE_AWAITING_ALLOCATION -> true)
      )

      assessmentCentreRepository.updateTests("appId1", expectedAssessmentCentreTests).futureValue

      assessmentCentreRepository.getTests("appId1").futureValue mustBe expectedAssessmentCentreTests
    }
  }

  trait TestFixture {

    val expectedAssessmentCentreTests = AssessmentCentreTests(
      Some(AnalysisExercise(
        fileId = "fileId1"
      ))
    )

    def insertApplicationWithAssessmentCentreAwaitingAllocation(appId: String, withTests: Boolean = true): Unit = {
      insertApplication(appId, ApplicationStatus.ASSESSMENT_CENTRE,
        additionalProgressStatuses = List(ASSESSMENT_CENTRE_AWAITING_ALLOCATION -> true)
      )

      if (withTests) {
        assessmentCentreRepository.updateTests(appId, expectedAssessmentCentreTests).futureValue
      }
    }
  }
}

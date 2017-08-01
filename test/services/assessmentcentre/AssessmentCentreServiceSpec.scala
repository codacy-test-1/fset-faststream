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

package services.assessmentcentre

import model.EvaluationResults.Green
import model.PassmarkPersistedObjects._
import model.assessmentscores.AssessmentScoresAllExercises
import model.command.ApplicationForFsac
import model.persisted.phase3tests.{ LaunchpadTest, Phase3TestGroup }
import model.persisted.{ PassmarkEvaluation, SchemeEvaluationResult }
import model._
import org.joda.time.DateTime
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.{ OneAppPerSuite, PlaySpec }
import play.api.mvc.Results
import repositories.AssessmentScoresRepository
import repositories.application.GeneralApplicationRepository
import repositories.assessmentcentre.{ AssessmentCentreRepository, CurrentSchemeStatusRepository }
import services.evaluation.AssessmentCentreEvaluationEngine
import services.passmarksettings.AssessmentCentrePassMarkSettingsService
import testkit.{ ExtendedTimeout, FutureHelper }

import scala.concurrent.Future

class AssessmentCentreServiceSpec extends PlaySpec with OneAppPerSuite with Results with ScalaFutures with FutureHelper with MockFactory
  with ExtendedTimeout {

  "An AssessmentCentreService" should {
    "progress candidates to assessment centre, attempting all despite errors" in new ServiceFixture {

      val applicationsToProgressToSift = List(
        ApplicationForFsac("appId1", PassmarkEvaluation("", Some(""),
          List(SchemeEvaluationResult(SchemeId("Commercial"), EvaluationResults.Green.toString)), "", Some("")), Nil),
        ApplicationForFsac("appId2", PassmarkEvaluation("", Some(""),
          List(SchemeEvaluationResult(SchemeId("Commercial"), EvaluationResults.Green.toString)), "", Some("")), Nil),
        ApplicationForFsac("appId3", PassmarkEvaluation("", Some(""),
          List(SchemeEvaluationResult(SchemeId("Commercial"), EvaluationResults.Green.toString)), "", Some("")), Nil))

      (mockAssessmentCentreRepo.progressToAssessmentCentre _)
        .expects(applicationsToProgressToSift.head, ProgressStatuses.ASSESSMENT_CENTRE_AWAITING_ALLOCATION)
        .returning(Future.successful())
      (mockAssessmentCentreRepo.progressToAssessmentCentre _)
        .expects(applicationsToProgressToSift(1), ProgressStatuses.ASSESSMENT_CENTRE_AWAITING_ALLOCATION)
        .returning(Future.failed(new Exception))
      (mockAssessmentCentreRepo.progressToAssessmentCentre _)
        .expects(applicationsToProgressToSift(2), ProgressStatuses.ASSESSMENT_CENTRE_AWAITING_ALLOCATION)
        .returning(Future.successful())

      whenReady(service.progressApplicationsToAssessmentCentre(applicationsToProgressToSift)) {
        results =>
          val failedApplications = Seq(applicationsToProgressToSift(1))
          val passedApplications = Seq(applicationsToProgressToSift.head, applicationsToProgressToSift(2))
          results mustBe SerialUpdateResult(failedApplications, passedApplications)
      }
    }
  }

  "next assessment candidate" should {
    "return an assessment candidate score with application Id" in new ReturnPassMarksFixture {
      (mockAssessmentCentreRepo.nextApplicationReadyForAssessmentScoreEvaluation _)
        .expects(*)
        .returning(Future.successful(Some(applicationId.toString())))

      (mockAssessmentScoresRepo.find _)
        .expects(*)
        .returning(Future.successful(Some(AssessmentScoresAllExercises(applicationId))))

      (mockCurrentSchemeStatusRepo.getTestGroup _)
        .expects(*)
        .returning(Future.successful(
          Some(Phase3TestGroup(
            expirationDate = DateTime.now,
            tests = List.empty[LaunchpadTest],
            evaluation = Some(
              PassmarkEvaluation(passmarkVersion = "version1",
                previousPhasePassMarkVersion = None,
                result = List(
                  SchemeEvaluationResult(schemeId = SchemeId("Commercial"), result = Green.toString)
                ),
                resultVersion = "version1",
                previousPhaseResultVersion = None)
            )
          ))
      ))

      val result = service.nextAssessmentCandidateReadyForEvaluation.futureValue

      result must not be empty
      result.get.passmark mustBe passmarkSettings
      result.get.schemes mustBe List(SchemeId("Commercial"))
      result.get.scores.applicationId mustBe applicationId
    }

    "return none if there is no passmark settings set" in new ServiceFixture {
      (mockAssessmentCentrePassMarkSettingsService.getLatestVersion _).expects().returning(Future.successful(None))

      val result = service.nextAssessmentCandidateReadyForEvaluation.futureValue
      result mustBe empty
    }

    "return none if there is no application ready for assessment score evaluation" in new ReturnPassMarksFixture {
      (mockAssessmentCentreRepo.nextApplicationReadyForAssessmentScoreEvaluation _)
        .expects(*)
        .returning(Future.successful(None))

      val result = service.nextAssessmentCandidateReadyForEvaluation.futureValue
      result mustBe empty
    }
  }

  trait ServiceFixture {
    val mockAppRepo = mock[GeneralApplicationRepository]
    val mockAssessmentCentreRepo = mock[AssessmentCentreRepository]
    val mockAssessmentCentrePassMarkSettingsService = mock[AssessmentCentrePassMarkSettingsService]
    val mockAssessmentScoresRepo = mock[AssessmentScoresRepository]
    val mockCurrentSchemeStatusRepo = mock[CurrentSchemeStatusRepository]
    val mockEvaluationEngine = mock[AssessmentCentreEvaluationEngine]

    val service = new AssessmentCentreService {
      val applicationRepo: GeneralApplicationRepository = mockAppRepo
      val assessmentCentreRepo: AssessmentCentreRepository = mockAssessmentCentreRepo
      val passmarkService: AssessmentCentrePassMarkSettingsService = mockAssessmentCentrePassMarkSettingsService
      val assessmentScoresRepo: AssessmentScoresRepository = mockAssessmentScoresRepo
      val currentSchemeStatusRepo: CurrentSchemeStatusRepository = mockCurrentSchemeStatusRepo
      val evaluationEngine: AssessmentCentreEvaluationEngine = mockEvaluationEngine
    }

    val applicationId = UniqueIdentifier.randomUniqueIdentifier
  }

  trait ReturnPassMarksFixture extends ServiceFixture {
    val threshold = PassMarkSchemeThreshold(10.0, 20.0)
    val passmarkSettings = AssessmentCentrePassMarkSettings(List(
      AssessmentCentrePassMarkScheme("Commercial", Some(threshold)),
      AssessmentCentrePassMarkScheme("DigitalAndTechnology", Some(threshold)),
      AssessmentCentrePassMarkScheme("Finance", Some(threshold))
    ), AssessmentCentrePassMarkInfo("1", DateTime.now, "user"))

    (mockAssessmentCentrePassMarkSettingsService.getLatestVersion _).expects()
      .returning(Future.successful(Some(passmarkSettings)))
  }
}

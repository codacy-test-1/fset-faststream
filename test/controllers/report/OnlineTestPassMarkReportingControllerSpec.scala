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

package controllers.report

import config.TestFixtureBase
import connectors.AuthProviderClient
import controllers.ReportingController
import model.report.onlinetestpassmark.{ ApplicationForOnlineTestPassMarkReportItemExamples, TestResultsForOnlineTestPassMarkReportItemExamples }
import model.report.{ OnlineTestPassMarkReportItem, _ }
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import play.api.test.Helpers._
import play.api.test.{ FakeHeaders, FakeRequest, Helpers }
import repositories.application.ReportingRepository
import repositories.csv.FSACIndicatorCSVRepository
import repositories.{ AssessmentScoresRepository, MediaRepository, QuestionnaireRepository, contactdetails }
import testkit.MockitoImplicits.OngoingStubbingExtension
import testkit.UnitWithAppSpec

import scala.language.postfixOps

class OnlineTestPassMarkReportingControllerSpec extends UnitWithAppSpec {

  "Online test pass mark report" should {
    "return nothing if no application exists" in new TestFixture {
      when(mockReportRepository.onlineTestPassMarkReport(any())).thenReturnAsync(Nil)
      when(mockQuestionRepository.findForOnlineTestPassMarkReport).thenReturnAsync(Map.empty)

      val response = controller.onlineTestPassMarkReport(frameworkId)(request).run
      val result = contentAsJson(response).as[List[OnlineTestPassMarkReportItem]]

      status(response) mustBe OK
      result mustBe empty
    }

    "return nothing if applications exist, but no questionnaires" in new TestFixture {
      when(mockReportRepository.onlineTestPassMarkReport(any())).thenReturnAsync(applications)
      when(mockQuestionRepository.findForOnlineTestPassMarkReport).thenReturnAsync(Map.empty)

      val response = controller.onlineTestPassMarkReport(frameworkId)(request).run
      val result = contentAsJson(response).as[List[OnlineTestPassMarkReportItem]]

      status(response) mustBe OK
      result mustBe empty
    }

    "return applications and questionnaires if applications and questionnaires exist, but no test results" in new TestFixture {
      when(mockReportRepository.onlineTestPassMarkReport(any())).thenReturnAsync(applicationsWithNoTestResults)

      when(mockQuestionRepository.findForOnlineTestPassMarkReport).thenReturnAsync(questionnairesForNoTestResults)

      val response = controller.onlineTestPassMarkReport(frameworkId)(request).run
      val result = contentAsJson(response).as[List[OnlineTestPassMarkReportItem]]

      status(response) mustBe OK
      result must have size 2
      result must contain(OnlineTestPassMarkReportItem(
        ApplicationForOnlineTestPassMarkReportItemExamples.applicationWithNoTestResult1,
        QuestionnaireReportItemExamples.questionnaire1))
      result must contain(OnlineTestPassMarkReportItem(
        ApplicationForOnlineTestPassMarkReportItemExamples.applicationWithNoTestResult2,
        QuestionnaireReportItemExamples.questionnaire2))
    }

    "return applications with questionnaire and test results" in new TestFixture {
      when(mockReportRepository.onlineTestPassMarkReport(any())).thenReturnAsync(applications)

      when(mockQuestionRepository.findForOnlineTestPassMarkReport).thenReturnAsync(questionnaires)

      val response = controller.onlineTestPassMarkReport(frameworkId)(request).run
      val result = contentAsJson(response).as[List[OnlineTestPassMarkReportItem]]

      status(response) mustBe OK

      result must contain theSameElementsAs List(
        OnlineTestPassMarkReportItem(ApplicationForOnlineTestPassMarkReportItemExamples.application1,
          QuestionnaireReportItemExamples.questionnaire1),
        OnlineTestPassMarkReportItem(ApplicationForOnlineTestPassMarkReportItemExamples.application2,
          QuestionnaireReportItemExamples.questionnaire2)
      )
    }
  }

  trait TestFixture extends TestFixtureBase {
    val frameworkId = "FastStream-2016"

    val mockReportRepository = mock[ReportingRepository]
    val mockQuestionRepository = mock[QuestionnaireRepository]
    val mockMediaRepository = mock[MediaRepository]
    val controller = new ReportingController {
      val reportingRepository = mockReportRepository
      val contactDetailsRepository = mock[contactdetails.ContactDetailsRepository]
      val questionnaireRepository = mockQuestionRepository
      val assessmentScoresRepository = mock[AssessmentScoresRepository]
      val mediaRepository: MediaRepository = mockMediaRepository
      val fsacIndicatorCSVRepository: FSACIndicatorCSVRepository = mock[FSACIndicatorCSVRepository]
      val authProviderClient = mock[AuthProviderClient]
    }

    lazy val testResults = Map(
      ApplicationForOnlineTestPassMarkReportExamples.application1.applicationId ->
        TestResultsForOnlineTestPassMarkReportItemExamples.testResults1,
      ApplicationForOnlineTestPassMarkReportExamples.application2.applicationId ->
        TestResultsForOnlineTestPassMarkReportItemExamples.testResults2)

    lazy val applications = List(
      ApplicationForOnlineTestPassMarkReportExamples.application1,
      ApplicationForOnlineTestPassMarkReportExamples.application2)
    lazy val applicationsWithNoTestResults = List(
      ApplicationForOnlineTestPassMarkReportExamples.applicationWithNoTestResult1,
      ApplicationForOnlineTestPassMarkReportExamples.applicationWithNoTestResult2)

    lazy val questionnaires = Map(
      ApplicationForOnlineTestPassMarkReportExamples.application1.applicationId ->
        QuestionnaireReportItemExamples.questionnaire1,
      ApplicationForOnlineTestPassMarkReportExamples.application2.applicationId ->
        QuestionnaireReportItemExamples.questionnaire2)
    lazy val questionnairesForNoTestResults = Map(
      ApplicationForOnlineTestPassMarkReportExamples.applicationWithNoTestResult1.applicationId ->
        QuestionnaireReportItemExamples.questionnaire1,
      ApplicationForOnlineTestPassMarkReportExamples.applicationWithNoTestResult2.applicationId ->
        QuestionnaireReportItemExamples.questionnaire2)

    def request = {
      FakeRequest(Helpers.GET, controllers.routes.ReportingController.onlineTestPassMarkReport(frameworkId).url, FakeHeaders(), "")
        .withHeaders("Content-Type" -> "application/json")
    }
  }

}

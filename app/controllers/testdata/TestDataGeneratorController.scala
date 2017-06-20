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

package controllers.testdata

import java.io.File

import config.MicroserviceAppConfig
import connectors.AuthProviderClient
import factories.UUIDFactory
import model.Exceptions.EmailTakenException
import model.command.testdata._
import model.exchange.testdata._
import model._
import model.command.testdata.CreateAdminUserInStatusRequest.{ AssessorRequest, CreateAdminUserInStatusRequest }
import model.command.testdata.CreateCandidateInStatusRequest.{ CreateCandidateInStatusRequest, _ }
import model.command.testdata.CreateEventRequest.CreateEventRequest
import model.persisted.PassmarkEvaluation
import model.persisted.eventschedules.EventType
import model.testdata.CreateAdminUserInStatusData.CreateAdminUserInStatusData
import model.testdata.CreateCandidateInStatusData.CreateCandidateInStatusData
import model.testdata.CreateEventData.CreateEventData
import org.joda.time.{ LocalDate, LocalTime }
import play.api.Play
import play.api.libs.json.{ JsObject, JsString, JsValue, Json }
import play.api.mvc.{ Action, AnyContent, RequestHeader }
import services.testdata._
import services.testdata.candidate.{ AdminUserStatusGeneratorFactory, StatusGeneratorFactory }
import services.testdata.faker.DataFaker.Random
import uk.gov.hmrc.play.http.HeaderCarrier
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object TestDataGeneratorController extends TestDataGeneratorController

trait TestDataGeneratorController extends BaseController {

  def ping = Action { implicit request =>
    Ok("OK")
  }

  def clearDatabase(generateDefaultUsers: Boolean) = Action.async { implicit request =>
    TestDataGeneratorService.clearDatabase(generateDefaultUsers).map { _ =>
      Ok(Json.parse("""{"message": "success"}"""))
    }
  }

  // scalastyle:off method.length
  def exampleCreateCandidateRequest = Action { implicit request =>
    val example = CreateCandidateInStatusRequest(
     statusData = StatusDataRequest(
       applicationStatus = ApplicationStatus.SUBMITTED.toString,
       previousApplicationStatus = Some(ApplicationStatus.REGISTERED.toString),
       progressStatus = Some(ProgressStatuses.SUBMITTED.toString),
       applicationRoute = Some(ApplicationRoute.Faststream.toString)
     ),
      personalData = Some(PersonalDataRequest(
        emailPrefix = Some(s"testf${Random.number()}"),
        firstName = Some("Kathryn"),
        lastName = Some("Janeway"),
        preferredName = Some("Captain"),
        dateOfBirth = Some("2328-05-20"),
        postCode = Some("QQ1 1QQ"),
        country = Some("America")
      )),
      diversityDetails = Some(DiversityDetailsRequest(
        genderIdentity = Some("Female"),
        sexualOrientation = Some("Straight"),
        ethnicity = Some("White"),
        universityAttended = Some("W01-USW"),
        parentalEmployment = Some("Traditional professional"),
        parentalEmployedOrSelfEmployed = Some("Employed"),
        parentalCompanySize = Some("Small (1 to 24 employees)")
      )),
      assistanceDetails = Some(AssistanceDetailsRequest(
        hasDisability = Some("Yes"),
        hasDisabilityDescription = Some(Random.hasDisabilityDescription),
        setGis = Some(true),
        onlineAdjustments = Some(true),
        onlineAdjustmentsDescription = Some(Random.onlineAdjustmentsDescription),
        assessmentCentreAdjustments = Some(true),
        assessmentCentreAdjustmentsDescription = Some(Random.assessmentCentreAdjustmentDescription)
      )),
      schemeTypes = Some(List(SchemeType.Commercial, SchemeType.European, SchemeType.DigitalAndTechnology)),
      isCivilServant = Some(Random.bool),
      hasFastPass = Some(true),
      hasDegree = Some(Random.bool),
      region = Some("region"),
      loc1scheme1EvaluationResult = Some("loc1 scheme1 result1"),
      loc1scheme2EvaluationResult = Some("loc1 scheme2 result2"),
      confirmedAllocation = Some(Random.bool),
      phase1TestData = Some(Phase1TestDataRequest(
        start = Some("2340-01-01"),
        expiry = Some("2340-01-29"),
        completion = Some("2340-01-16"),
        bqtscore = Some("80"),
        sjqtscore = Some("70")
      )),
      phase2TestData = Some(Phase2TestDataRequest(
        start = Some("2340-01-01"),
        expiry = Some("2340-01-29"),
        completion = Some("2340-01-16"),
        tscore = Some("80")
      )),
      phase3TestData = Some(Phase3TestDataRequest(
        start = Some("2340-01-01"),
        expiry = Some("2340-01-29"),
        completion = Some("2340-01-16"),
        score = Some(12.0),
        receivedBeforeInHours = Some(72),
        generateNullScoresForFewQuestions = Some(false)
      )),
      adjustmentInformation = Some(Adjustments(
        adjustments = Some(List("etrayInvigilated", "videoInvigilated")),
        adjustmentsConfirmed = Some(true),
        etray = Some(AdjustmentDetail(timeNeeded = Some(33), invigilatedInfo = Some("Some comments here")
          , otherInfo = Some("Some other comments here"))),
        video = Some(AdjustmentDetail(timeNeeded = Some(33), invigilatedInfo = Some("Some comments here")
          , otherInfo = Some("Some other comments here")))
      ))
    )

    Ok(Json.toJson(example))
  }
  // scalastyle:on method.length

  def exampleCreateAdminUserRequest = Action { implicit request =>
    val example = CreateAdminUserInStatusRequest(
      emailPrefix = Some("admin_user"),
      firstName = Some("Admin user 1"),
      lastName = Some("lastname"),
      preferredName = Some("Ad"),
      role = Some("assessor"),
      phone = Some("123456789"),
      assessor = Some(AssessorRequest(
        skills = Some(List("assessor", "qac")),
        civilServant = Some(true)
      ))
    )

    Ok(Json.toJson(example))
  }

  def exampleCreateEventRequest = Action { implicit request =>
    val example = CreateEventRequest(
      id = Some(UUIDFactory.generateUUID()),
      eventType = Some(EventType.FSAC),
      location = Some("London"),
      venue = Some("London venue 1"),
      date = Some(LocalDate.now),
      capacity = Some(32),
      minViableAttendees = Some(24),
      attendeeSafetyMargin = Some(30),
      startTime = Some(LocalTime.now()),
      endTime = Some(LocalTime.now()),
      skillRequirements = Some(Map("ASSESSOR" -> 4,
      "CHAIR" -> 1))
    )

    Ok(Json.toJson(example))
  }

  def createAdminUsers(numberToGenerate: Int, emailPrefix: Option[String], role: String): Action[AnyContent] = Action.async { implicit request =>
    try {
      TestDataGeneratorService.createAdminUsers(numberToGenerate, emailPrefix, AuthProviderClient.getRole(role)).map { candidates =>
        Ok(Json.toJson(candidates))
      }
    } catch {
      case _: EmailTakenException => Future.successful(Conflict(JsObject(List(("message",
          JsString("Email has been already taken. Try with another one by changing the emailPrefix parameter"))))))
    }
  }

  def createAdminUsersInStatusPOST(numberToGenerate: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CreateAdminUserInStatusRequest] { createRequest =>
      createAdminUserInStatus(CreateAdminUserInStatusData.apply(createRequest), numberToGenerate)
    }
  }

  private lazy val cubiksUrlFromConfig: String = MicroserviceAppConfig.testDataGeneratorCubiksSecret

  def createCandidatesInStatusPOST(numberToGenerate: Int): Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CreateCandidateInStatusRequest] { createRequest =>
      createCandidateInStatus(CreateCandidateInStatusData.apply(cubiksUrlFromConfig, createRequest), numberToGenerate)
    }
  }

  def createEvent(numberToGenerate: Int) : Action[JsValue] = Action.async(parse.json) { implicit request =>
    withJsonBody[CreateEventRequest] { createRequest =>
      createEvent(CreateEventData.apply(createRequest), numberToGenerate)
    }
  }


  private def createCandidateInStatus(config: (Int) => CreateCandidateInStatusData, numberToGenerate: Int)
    (implicit hc: HeaderCarrier, rh: RequestHeader) = {
    try {
      TestDataGeneratorService.createCandidatesInSpecificStatus(
        numberToGenerate, StatusGeneratorFactory.getGeneratorForCandidates,
        config
      ).map { candidates =>
        Ok(Json.toJson(candidates))
      }
    } catch {
      case _: EmailTakenException => Future.successful(Conflict(JsObject(List(("message",
          JsString("Email has been already taken. Try with another one by changing the emailPrefix parameter"))))))
    }
  }

  private def createAdminUserInStatus(createData: (Int) => CreateAdminUserInStatusData, numberToGenerate: Int)
                                     (implicit hc: HeaderCarrier, rh: RequestHeader) = {
    try {
      TestDataGeneratorService.createAdminUserInSpecificStatus(
        numberToGenerate,
        AdminUserStatusGeneratorFactory.getGeneratorForAdminUsers,
        createData
      ).map { adminUsers =>
        Ok(Json.toJson(adminUsers))
      }
    } catch {
      case _: EmailTakenException => Future.successful(Conflict(JsObject(List(("message",
        JsString("Email has been already taken. Try with another one by changing the emailPrefix parameter"))))))
    }
  }

  private def createEvent(createData: (Int) => CreateEventData, numberToGenerate: Int)
                                     (implicit hc: HeaderCarrier, rh: RequestHeader) = {
    try {
      TestDataGeneratorService.createEvent(
        numberToGenerate,
        createData
      ).map { events =>
        Ok(Json.toJson(events))
      }
    } catch {
      case _: Throwable => Future.successful(Conflict(JsObject(List(("message",
        JsString("There was an exception creating the event"))))))
    }
  }
}

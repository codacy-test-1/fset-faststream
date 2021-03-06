package repositories

import config.Phase1TestsConfig
import model.ApplicationRoute.ApplicationRoute
import model.ApplicationStatus.{ apply => _, _ }
import model.EvaluationResults._
import model.{ ApplicationRoute, ApplicationStatus, Phase, SchemeId }
import model.exchange.passmarksettings.{ PassMarkThreshold, Phase1PassMark, Phase1PassMarkSettings, Phase1PassMarkThresholds }
import model.persisted.{ ApplicationReadyForEvaluation, PassmarkEvaluation, SchemeEvaluationResult }
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.prop._
import reactivemongo.bson.BSONDocument
import reactivemongo.json.ImplicitBSONHandlers
import reactivemongo.json.collection.JSONCollection
import services.onlinetesting.phase1.EvaluatePhase1ResultService
import testkit.MongoRepositorySpec

import scala.concurrent.Future


class Phase1TestEvaluationSpec extends MongoRepositorySpec with CommonRepository
  with TableDrivenPropertyChecks {
  import ImplicitBSONHandlers._

  val collectionName = CollectionNames.APPLICATION
  override val additionalCollections = List(CollectionNames.PHASE1_PASS_MARK_SETTINGS)

  def phase1TestEvaluationService = new EvaluatePhase1ResultService {
    val evaluationRepository = phase1EvaluationRepo
    val gatewayConfig = mockGatewayConfig
    val passMarkSettingsRepo = phase1PassMarkSettingRepo
    val phase1TestsConfigMock = mock[Phase1TestsConfig]
    val phase = Phase.PHASE1

    when(gatewayConfig.phase1Tests).thenReturn(phase1TestsConfigMock)
    when(phase1TestsConfigMock.scheduleIds).thenReturn(Map("sjq" -> 16196, "bq" -> 16194))
  }

  "phase1 evaluation process" should {

    "give pass results when all schemes are passed" in new TestFixture {

        applicationEvaluation("application-1", 80, 80,SchemeId("Commercial"), SchemeId("DigitalAndTechnology")) mustResultIn (
          PHASE1_TESTS_PASSED, SchemeId("Commercial") -> Green, SchemeId("DigitalAndTechnology") -> Green)

        applicationEvaluation("application-2", 79.999, 78.08,SchemeId("HousesOfParliament")) mustResultIn (
          PHASE1_TESTS_PASSED, SchemeId("HousesOfParliament") -> Green)

        applicationEvaluation("application-3", 30, 30,SchemeId("Generalist")) mustResultIn (
          PHASE1_TESTS_PASSED, SchemeId("Generalist") -> Green)
    }

    "give pass results when at-least one scheme is passed" in new TestFixture {

      applicationEvaluation("application-1", 20.002, 20.06,SchemeId("Commercial"), SchemeId("DigitalAndTechnology")) mustResultIn (
        PHASE1_TESTS_PASSED, SchemeId("Commercial") -> Red, SchemeId("DigitalAndTechnology") -> Green)
    }

    "give fail results when none of the schemes are passed" in new TestFixture {

      applicationEvaluation("application-1", 20, 20,SchemeId("DiplomaticServiceEconomics"), SchemeId("DiplomaticServiceEuropean")) mustResultIn (
        PHASE1_TESTS_FAILED, SchemeId("DiplomaticServiceEconomics") -> Red, SchemeId("DiplomaticServiceEuropean") -> Red)
    }

    "leave applicants in amber when all the schemes are in amber" in new TestFixture {

      applicationEvaluation("application-1", 40, 40,SchemeId("DiplomaticServiceEconomics"), SchemeId("DiplomaticServiceEuropean")) mustResultIn (
        PHASE1_TESTS, SchemeId("DiplomaticServiceEconomics") -> Amber, SchemeId("DiplomaticServiceEuropean") -> Amber)

      applicationEvaluation("application-2", 25.015, 25.015, SchemeId("Finance")) mustResultIn (
        PHASE1_TESTS, SchemeId("Finance") -> Amber)
    }

    "leave applicants in amber when at-least one of the scheme is amber and none of the schemes in green" in new TestFixture {

      applicationEvaluation("application-1", 30, 80,SchemeId("Commercial"), SchemeId("European")) mustResultIn (
        PHASE1_TESTS, SchemeId("Commercial") -> Amber, SchemeId("European") -> Red)
    }

    "give pass results for gis candidates" in new TestFixture {

      gisApplicationEvaluation("application-1", 25,SchemeId("Commercial"), SchemeId("DigitalAndTechnology")) mustResultIn (
        PHASE1_TESTS_PASSED, SchemeId("Commercial") -> Amber, SchemeId("DigitalAndTechnology") -> Green)
    }

    "re-evaluate applicants in amber" in new TestFixture {

      {
        applicationEvaluation("application-1", 40, 40,SchemeId("DiplomaticServiceEconomics"), SchemeId("DiplomaticServiceEuropean"))
          mustResultIn (PHASE1_TESTS, SchemeId("DiplomaticServiceEconomics") -> Amber, SchemeId("DiplomaticServiceEuropean") -> Amber)

        applicationReEvaluationWithSettings(
          (SchemeId("DiplomaticServiceEconomics"), 40, 40, 40, 40),
          (SchemeId("DiplomaticServiceEuropean"), 40, 40, 40, 40)
        ) mustResultIn (PHASE1_TESTS_PASSED, SchemeId("DiplomaticServiceEconomics") -> Green, SchemeId("DiplomaticServiceEuropean") -> Green)
      }

      {
        applicationEvaluation("application-2", 25.015, 25.015, SchemeId("Finance")) mustResultIn (
          PHASE1_TESTS, SchemeId("Finance") -> Amber)

        applicationReEvaluationWithSettings(
          (SchemeId("Finance"), 25.015, 25.015, 25.015, 25.015)
        ) mustResultIn (PHASE1_TESTS_PASSED, SchemeId("Finance") -> Green)

      }

    }

    "evaluate sdip scheme to Red for SdipFaststream candidate" in new TestFixture {
      applicationEvaluation("application-1", 80, 80,SchemeId("Commercial"), SchemeId("DigitalAndTechnology"), SchemeId("Sdip"))
        (ApplicationRoute.SdipFaststream)
      mustResultIn (PHASE1_TESTS_PASSED, SchemeId("Commercial") -> Green, SchemeId("DigitalAndTechnology") -> Green)

      applicationReEvaluationWithSettings(
        (SchemeId("Sdip"), 90.00, 90.00, 90.00, 90.00)
      ) mustResultIn (PHASE1_TESTS_PASSED,  SchemeId("Commercial") -> Green, SchemeId("DigitalAndTechnology") -> Green, SchemeId("Sdip") -> Red)
    }

    "evaluate sdip scheme to Green for SdipFaststream candidate" in new TestFixture {
      applicationEvaluation("application-1", 80, 80,SchemeId("Commercial"), SchemeId("DigitalAndTechnology"), SchemeId("Sdip"))
        (ApplicationRoute.SdipFaststream)
      mustResultIn (PHASE1_TESTS_PASSED, SchemeId("Commercial") -> Green, SchemeId("DigitalAndTechnology") -> Green)

      applicationReEvaluationWithSettings( (SchemeId("Sdip"), 80.00, 80.00, 80.00, 80.00) )
      mustResultIn (PHASE1_TESTS_PASSED,  SchemeId("Commercial") -> Green, SchemeId("DigitalAndTechnology") -> Green, SchemeId("Sdip") -> Green)
    }

    "do not evaluate sdip scheme for SdipFaststream candidate when no pass marks set" in new TestFixture {
      applicationEvaluation("application-1", 80, 80,SchemeId("Commercial"), SchemeId("DigitalAndTechnology"), SchemeId("Sdip"))
        (ApplicationRoute.SdipFaststream)
      mustResultIn (PHASE1_TESTS_PASSED, SchemeId("Commercial") -> Green, SchemeId("DigitalAndTechnology") -> Green)

      applicationReEvaluationWithSettings(
        (SchemeId("Finance"), 90.00, 90.00, 90.00, 90.00)
      ) mustResultIn (PHASE1_TESTS_PASSED,  SchemeId("Commercial") -> Green, SchemeId("DigitalAndTechnology") -> Green)
    }

  }

  trait TestFixture {

    // format: OFF
    val phase1PassMarkSettingsTable = Table[SchemeId, Double, Double, Double, Double](
      ("Scheme Name",                       "SJQ Fail Threshold",   "SJQ Pass threshold",   "BQ Fail Threshold",    "BQ Pass Threshold"),
      (SchemeId("Commercial"),                            20.0,                    80.0,                   30.0,                   70.0),
      (SchemeId("DigitalAndTechnology"),                  20.001,                  20.001,                 20.01,                  20.05),
      (SchemeId("DiplomaticService"),                     20.01,                   20.02,                  20.01,                  20.02),
      (SchemeId("DiplomaticServiceEconomics"),            30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("DiplomaticServiceEuropean"),             30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("European"),                              40.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("Finance"),                               25.01,                   25.02,                  25.01,                  25.02),
      (SchemeId("Generalist"),                            30.0,                    30.0,                   30.0,                   30.0),
      (SchemeId("GovernmentCommunicationService"),        30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("GovernmentEconomicsService"),            30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("GovernmentOperationalResearchService"),  30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("GovernmentSocialResearchService"),       30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("GovernmentStatisticalService"),          30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("HousesOfParliament"),                    30.0,                    79.999,                 30.0,                   78.08),
      (SchemeId("HumanResources"),                        30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("ProjectDelivery"),                       30.0,                    70.0,                   30.0,                   70.0),
      (SchemeId("ScienceAndEngineering"),                 69.00,                   69.00,                  78.99,                  78.99)
    )
    // format: ON

    var phase1PassMarkSettings: Phase1PassMarkSettings = _

    var applicationReadyForEvaluation: ApplicationReadyForEvaluation = _

    var passMarkEvaluation: PassmarkEvaluation = _

    def gisApplicationEvaluation(applicationId:String, sjqScore: Double, selectedSchemes: SchemeId*): TestFixture = {
      applicationReadyForEvaluation = insertApplicationWithPhase1TestResults(applicationId, sjqScore, None, isGis = true)(selectedSchemes: _*)
      phase1TestEvaluationService.evaluate(applicationReadyForEvaluation, phase1PassMarkSettings).futureValue
      this
    }

    def applicationEvaluation(applicationId:String, sjqScore: Double, bjqScore: Double, selectedSchemes: SchemeId*)
                             (implicit applicationRoute: ApplicationRoute = ApplicationRoute.Faststream): TestFixture = {
      applicationReadyForEvaluation = insertApplicationWithPhase1TestResults(applicationId, sjqScore, Some(bjqScore),
        isGis = false, applicationRoute = applicationRoute)(selectedSchemes: _*)
      phase1TestEvaluationService.evaluate(applicationReadyForEvaluation, phase1PassMarkSettings).futureValue
      this
    }

    def mustResultIn(expApplicationStatus: ApplicationStatus.ApplicationStatus, expSchemeResults: (SchemeId , Result)*): TestFixture = {
      passMarkEvaluation = phase1EvaluationRepo.getPassMarkEvaluation(applicationReadyForEvaluation.applicationId).futureValue
      val applicationStatus = ApplicationStatus.withName(
        applicationRepository.findStatus(applicationReadyForEvaluation.applicationId).futureValue.status)

      val schemeResults = passMarkEvaluation.result.map {
        SchemeEvaluationResult.unapply(_).map {
          case (schemeType, resultStr) => schemeType -> Result(resultStr)
        }.get
      }
      phase1PassMarkSettings.version mustBe passMarkEvaluation.passmarkVersion
      applicationStatus mustBe expApplicationStatus
      schemeResults.size mustBe expSchemeResults.size
      schemeResults must contain theSameElementsAs expSchemeResults
      applicationReadyForEvaluation = applicationReadyForEvaluation.copy(applicationStatus = expApplicationStatus)
      this
    }

    def applicationReEvaluationWithSettings(newSchemeSettings: (SchemeId, Double, Double, Double, Double)*): TestFixture = {
      val schemePassMarkSettings = phase1PassMarkSettingsTable.filterNot(schemeSetting =>
        newSchemeSettings.map(_._1).contains(schemeSetting._1)) ++ newSchemeSettings
      phase1PassMarkSettings = createPhase1PassMarkSettings(schemePassMarkSettings).futureValue
      phase1TestEvaluationService.evaluate(applicationReadyForEvaluation, phase1PassMarkSettings).futureValue
      this
    }

    private def createPhase1PassMarkSettings(phase1PassMarkSettingsTable:
                                             TableFor5[SchemeId, Double, Double, Double, Double]): Future[Phase1PassMarkSettings] = {
      val schemeThresholds = phase1PassMarkSettingsTable.map {
        fields => Phase1PassMark(fields._1,
          Phase1PassMarkThresholds(PassMarkThreshold(fields._2, fields._3), PassMarkThreshold(fields._4, fields._5)))
      }.toList

      val phase1PassMarkSettings = Phase1PassMarkSettings(
        schemeThresholds,
        "version-1",
        DateTime.now,
        "user-1"
      )
      phase1PassMarkSettingRepo.create(phase1PassMarkSettings).flatMap { _ =>
        phase1PassMarkSettingRepo.getLatestVersion.map(_.get)
      }
    }

    val appCollection = mongo().collection[JSONCollection](collectionName)

    def createUser(userId: String, appId: String) = {
      appCollection.insert(BSONDocument("applicationId" -> appId, "userId" -> userId, "applicationStatus" -> CREATED))
    }

    Future.sequence(List(
      createUser("user-1", "application-1"),
      createUser("user-2", "application-2"),
      createUser("user-3", "application-3"),
      createPhase1PassMarkSettings(phase1PassMarkSettingsTable).map(phase1PassMarkSettings = _)
    )).futureValue
  }

}

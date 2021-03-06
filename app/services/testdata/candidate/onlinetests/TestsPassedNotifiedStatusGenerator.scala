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

package services.testdata.candidate.onlinetests

import model.ProgressStatuses.PHASE3_TESTS_PASSED_NOTIFIED
import model.exchange.testdata.CreateCandidateResponse.CreateCandidateResponse
import model.testdata.CreateCandidateData.CreateCandidateData
import play.api.mvc.RequestHeader
import repositories.application.GeneralApplicationMongoRepository
import repositories._
import services.testdata.candidate.ConstructiveGenerator
import uk.gov.hmrc.play.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Phase3TestsPassedNotifiedStatusGenerator extends TestsPassedNotifiedStatusGenerator {
  val previousStatusGenerator = Phase3TestsPassedStatusGenerator
  val appRepository: GeneralApplicationMongoRepository = applicationRepository
  val notifiedStatus = PHASE3_TESTS_PASSED_NOTIFIED

  override def generate(generationId: Int,
                        generatorConfig:
                        CreateCandidateData)(implicit hc: HeaderCarrier, rh: RequestHeader): Future[CreateCandidateResponse] = {
    for {
      candidate <- previousStatusGenerator.generate(generationId, generatorConfig)
      _ <- appRepository.addProgressStatusAndUpdateAppStatus(candidate.applicationId.get, notifiedStatus)
    } yield candidate
  }
}

trait TestsPassedNotifiedStatusGenerator extends ConstructiveGenerator {

}

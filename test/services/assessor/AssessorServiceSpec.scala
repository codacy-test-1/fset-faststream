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

package services.assessor

import model.command.AssistanceDetailsExchangeExamples
import model.exchange.{ Assessor, AssessorAvailability }
import model.persisted.AssessorExamples
import org.mockito.ArgumentMatchers.{ eq => eqTo, _ }
import org.mockito.Mockito._
import repositories.{ AssessorRepository, LocationsWithVenuesYamlRepository$$ }
import repositories.assistancedetails.AssistanceDetailsRepository
import services.BaseServiceSpec
import services.assessoravailability.AssessorService
import services.assistancedetails.AssistanceDetailsService

import scala.concurrent.duration._
import AssessorExamples._
import model.Exceptions
import model.Exceptions.AssessorNotFoundException
import repositories.events.LocationsWithVenuesRepository

import scala.concurrent.{ Await, Future }

class AssessorServiceSpec extends BaseServiceSpec {

  "save assessor" should {

    "save NEW assessors should be successful" in new TestFixture {

      when(mockAssessorRepository.find(eqTo(AssessorUserId))).thenReturn(Future.successful(None))
      when(mockAssessorRepository.save(eqTo(AssessorNew))).thenReturn(Future.successful(()))
      val response = service.saveAssessor(AssessorUserId, Assessor.apply(AssessorNew)).futureValue
      response mustBe unit
      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
      verify(mockAssessorRepository).save(eqTo(AssessorNew))
    }

    "update EXISTING assessors should update skills and respect availability" in new TestFixture {

      when(mockAssessorRepository.find(eqTo(AssessorUserId))).thenReturn(Future.successful(Some(AssessorExisting)))
      when(mockAssessorRepository.save(eqTo(AssessorMerged))).thenReturn(Future.successful(()))
      val response = service.saveAssessor(AssessorUserId, Assessor.apply(AssessorNew)).futureValue
      response mustBe unit
      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
      verify(mockAssessorRepository).save(eqTo(AssessorMerged))
    }
  }

  "add availability" should {

    "add availability to NON-EXISTING assessors should fail" in new TestFixture {

      when(mockAssessorRepository.find(eqTo(AssessorUserId))).thenReturn(Future.successful(None))

      intercept[AssessorNotFoundException] {
        Await.result(service.addAvailability(AssessorUserId, AssessorAvailability.apply(AssessorWithAvailability)), 10 seconds)
      }
      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
    }

    "add availability to EXISTING assessors" in new TestFixture {

      when(mockAssessorRepository.find(eqTo(AssessorUserId))).thenReturn(Future.successful(Some(AssessorExisting)))
      when(mockAssessorRepository.save(eqTo(AssessorWithAvailabilityMerged))).thenReturn(Future.successful(()))

      val result = service.addAvailability(AssessorUserId, AssessorAvailability.apply(AssessorWithAvailability)).futureValue

      result mustBe unit

      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
      verify(mockAssessorRepository).save(eqTo(AssessorWithAvailabilityMerged))
    }
  }


  "find assessor" should {
    "return assessor details" in new TestFixture {
      when(mockAssessorRepository.find(AssessorUserId)).thenReturn(Future.successful(Some(AssessorExisting)))

      val response = service.findAssessor(AssessorUserId).futureValue

      response mustBe model.exchange.Assessor(AssessorExisting)
      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
    }

    "throw exception when there are no assessor" in new TestFixture {
      when(mockAssessorRepository.find(AssessorUserId)).thenReturn(Future.successful(None))

      intercept[Exceptions.AssessorNotFoundException] {
        Await.result(service.findAssessor(AssessorUserId), 10 seconds)
      }
      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
    }
  }

  "find assessor availability" should {
    "return assessor availability" in new TestFixture {
      when(mockAssessorRepository.find(AssessorUserId)).thenReturn(Future.successful(Some(AssessorWithAvailability)))

      val response = service.findAvailability(AssessorUserId).futureValue

      response mustBe model.exchange.AssessorAvailability(AssessorWithAvailability)
      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
    }

    "throw exception when there are no assessor" in new TestFixture {
      when(mockAssessorRepository.find(AssessorUserId)).thenReturn(Future.successful(None))

      intercept[Exceptions.AssessorNotFoundException] {
        Await.result(service.findAvailability(AssessorUserId), 10 seconds)
      }
      verify(mockAssessorRepository).find(eqTo(AssessorUserId))
    }
  }

  trait TestFixture  {
    val mockAssessorRepository = mock[AssessorRepository]
    val mockAssessmentCentreYamlRepository = mock[LocationsWithVenuesRepository]

    val service = new AssessorService {
      override val assessorRepository: AssessorRepository = mockAssessorRepository
      override val assessmentCentreYamlRepository: LocationsWithVenuesRepository = mockAssessmentCentreYamlRepository
    }
  }
}

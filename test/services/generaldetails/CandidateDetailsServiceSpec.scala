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

package services.generaldetails

import model.{ ApplicationStatus, FastPassDetails }
import model.command.UpdateGeneralDetailsExamples._
import model.persisted.ContactDetailsExamples._
import model.persisted.PersonalDetailsExamples._
import org.mockito.Matchers.{ eq => eqTo, _ }
import org.mockito.Mockito._
import repositories.FastPassDetailsRepository
import repositories.contactdetails.ContactDetailsRepository
import repositories.personaldetails.PersonalDetailsRepository
import services.{ AuditService, BaseServiceSpec }

import scala.concurrent.Future

class CandidateDetailsServiceSpec extends BaseServiceSpec {
  val mockPersonalDetailsRepository = mock[PersonalDetailsRepository]
  val mockContactDetailsRepository = mock[ContactDetailsRepository]
  val mockFastPassDetailsRepository = mock[FastPassDetailsRepository]
  val mockAuditService = mock[AuditService]

  val service = new CandidateDetailsService {
    val pdRepository = mockPersonalDetailsRepository
    val cdRepository = mockContactDetailsRepository
    val fpdRepository = mockFastPassDetailsRepository
    val auditService = mockAuditService
  }

  "update candidate" should {
    "update personal and contact details" in {
      when(mockPersonalDetailsRepository.update(eqTo(AppId), eqTo(UserId), eqTo(JohnDoe), any[Seq[ApplicationStatus.Value]],
        any[ApplicationStatus.Value])).thenReturn(Future.successful(()))
      when(mockContactDetailsRepository.update(UserId, ContactDetailsUK)).thenReturn(emptyFuture)
      when(mockFastPassDetailsRepository.update(AppId, CandidateContactDetailsUK.fastPassDetails)).thenReturn(emptyFuture)

      val response = service.update(AppId, UserId, CandidateContactDetailsUK)

      assertNoExceptions(response)
    }
  }

  "find candidate" should {
    "return personal and contact details" in {
      when(mockPersonalDetailsRepository.find(AppId)).thenReturn(Future.successful(JohnDoe))
      when(mockContactDetailsRepository.find(UserId)).thenReturn(Future.successful(ContactDetailsUK))
      when(mockFastPassDetailsRepository.find(AppId)).thenReturn(Future.successful(FastPassDetails(applicable = false)))

      val response = service.find(AppId, UserId).futureValue

      response mustBe CandidateContactDetailsUK
    }
  }
}

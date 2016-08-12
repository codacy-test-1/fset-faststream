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

package services.assistancedetails

import config.TestFixtureBase
import model.command.AssistanceDetailsExchangeExamples
import org.mockito.Matchers.{eq => eqTo}
import org.mockito.Mockito._
import persisted.AssistanceDetailsExamples
import repositories._
import repositories.assistancedetails.AssistanceDetailsRepository
import services.BaseServiceSpec

import scala.concurrent.Future

class AssistanceDetailsServiceSpec extends BaseServiceSpec {

  "update" should {
    "update assistance details successfully" in new TestFixture {
      when(mockAdRepository.update(eqTo(AppId), eqTo(UserId), eqTo(AssistanceDetailsExamples.DisabilityGisAndAdjustments))
      ).thenReturn(Future.successful(()))

      val response = service.update(AppId, UserId, AssistanceDetailsExchangeExamples.DisabilityGisAndAdjustments).futureValue

      response mustBe (())
    }
  }

  "find candidate" should {
    "return assistance details" in new TestFixture {
      when(mockAdRepository.find(AppId)).thenReturn(Future.successful(AssistanceDetailsExamples.OnlyDisabilityNoGisNoAdjustments))

      val response = service.find(AppId, UserId).futureValue

      response mustBe AssistanceDetailsExchangeExamples.OnlyDisabilityNoGisNoAdjustments
    }
  }

  trait TestFixture  {
    val mockAdRepository = mock[AssistanceDetailsRepository]

    val service = new AssistanceDetailsService {
      val adRepository = mockAdRepository
    }
  }
}

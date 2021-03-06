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

package controllers

import config.TestFixtureBase
import model.exchange.{ CandidateEligibleForEvent, CandidatesEligibleForEventResponse }
import model.persisted.eventschedules.{ Event, EventType, Location, Venue }
import org.joda.time.{ DateTime, LocalDate, LocalTime }
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.test.Helpers.{ contentAsJson, status, _ }
import play.api.test.{ FakeHeaders, FakeRequest, Helpers }
import services.allocation.CandidateAllocationService
import testkit.MockitoImplicits._
import testkit.UnitWithAppSpec


class CandidateAllocationControllerSpec  extends UnitWithAppSpec {

  "Find candidates eligible for event allocation" must {

    "handle no candidates" in new TestFixture {
      when(mockCandidateAllocationService.findCandidatesEligibleForEventAllocation(any[String]))
        .thenReturnAsync(CandidatesEligibleForEventResponse(List.empty, 0))

      val result = controller.findCandidatesEligibleForEventAllocation("London")(
        findCandidatesEligibleForEventAllocationRequest("london")).run
      val jsonResponse = contentAsJson(result)

      (jsonResponse \ "candidates").as[List[CandidateEligibleForEvent]] mustBe List.empty
      (jsonResponse \ "totalCandidates").as[Int] mustBe 0

      status(result) mustBe OK
    }

    "handle candidates" in new TestFixture {
      val candidate = CandidateEligibleForEvent(applicationId = "appId", firstName = "Joe", lastName = "Bloggs",
        needsAdjustment = true, dateReady = DateTime.now())
      when(mockCandidateAllocationService.findCandidatesEligibleForEventAllocation(any[String]))
        .thenReturnAsync(CandidatesEligibleForEventResponse(List(candidate), 1))

      val result = controller.findCandidatesEligibleForEventAllocation("London")(
        findCandidatesEligibleForEventAllocationRequest("london")).run
      val jsonResponse = contentAsJson(result)

      (jsonResponse \ "candidates").as[List[CandidateEligibleForEvent]] mustBe List(candidate)
      (jsonResponse \ "totalCandidates").as[Int] mustBe 1

      status(result) mustBe OK
    }
  }

  trait TestFixture extends TestFixtureBase {
    val mockCandidateAllocationService = mock[CandidateAllocationService]
    val MockVenue = Venue("London FSAC", "Bush House")
    val MockLocation = Location("London")

    val MockEvent = new Event("id", EventType.FSAC, "description", MockLocation, MockVenue,
      LocalDate.now, 32, 10, 5, LocalTime.now, LocalTime.now, Map.empty, List.empty)

    val controller = new CandidateAllocationController {
      val candidateAllocationService: CandidateAllocationService = mockCandidateAllocationService
    }
  }

  def findCandidatesEligibleForEventAllocationRequest(location: String) = {
    FakeRequest(Helpers.GET, controllers.routes.CandidateAllocationController.findCandidatesEligibleForEventAllocation(location).url,
      FakeHeaders(), "").withHeaders("Content-Type" -> "application/json")
  }

}

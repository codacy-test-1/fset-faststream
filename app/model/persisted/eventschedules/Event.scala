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

package model.persisted.eventschedules

import factories.UUIDFactory
import model.persisted.eventschedules.EventType.EventType
import org.joda.time.{ LocalDate, LocalTime }
import play.api.libs.json.{ Json, OFormat }
import reactivemongo.bson.Macros
import model.exchange.{ Event => ExchangeEvent }
import repositories.{ BSONLocalDateHandler, BSONLocalTimeHandler, BSONMapHandler }

case class Event(
  id: String,
  eventType: EventType,
  description: String,
  location: Location,
  venue: Venue,
  date: LocalDate,
  capacity: Int,
  minViableAttendees: Int,
  attendeeSafetyMargin: Int,
  startTime: LocalTime,
  endTime: LocalTime,
  skillRequirements: Map[String, Int],
  sessions: List[Session]
)

object Event {
  implicit val eventFormat: OFormat[Event] = Json.format[Event]
  implicit val eventHandler = Macros.handler[Event]

  def apply(exchangeEvent: ExchangeEvent): Event = {
    new Event(
      id = exchangeEvent.id,
      eventType = exchangeEvent.eventType,
      description = exchangeEvent.description,
      location = exchangeEvent.location,
      venue = exchangeEvent.venue,
      date = exchangeEvent.date,
      capacity = exchangeEvent.capacity,
      minViableAttendees = exchangeEvent.minViableAttendees,
      attendeeSafetyMargin = exchangeEvent.attendeeSafetyMargin,
      startTime = exchangeEvent.startTime,
      endTime = exchangeEvent.endTime,
      skillRequirements = exchangeEvent.skillRequirements,
      sessions = exchangeEvent.sessions.map(Session.apply)
    )
  }
}

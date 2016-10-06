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

package model

import model.ProgressStatuses.{ Phase1Tests, ProgressStatus }

import scala.concurrent.duration.{ DAYS, HOURS, TimeUnit }

sealed case class ReminderNotice(hoursBeforeReminder: Int, progressStatuses: ProgressStatus) {
  require(hoursBeforeReminder > 0, "Hours before reminder was negative")
  require(progressStatuses == Phase1Tests.FIRST_REMINDER || progressStatuses == Phase1Tests.SECOND_REMINDER,
    "progressStatuses value not allowed")

  def timeUnit: TimeUnit = progressStatuses match {
    case Phase1Tests.SECOND_REMINDER => HOURS
    case Phase1Tests.FIRST_REMINDER => DAYS
  }
}

object FirstReminder extends ReminderNotice(72, Phase1Tests.FIRST_REMINDER)
object SecondReminder extends ReminderNotice(24, Phase1Tests.SECOND_REMINDER)
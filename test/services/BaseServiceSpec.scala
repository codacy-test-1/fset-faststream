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

package services

import fixture.UnitSpec
import org.joda.time.{ DateTime, DateTimeZone }
import testkit.FutureHelper

/**
  * Common base class for all service tests
  */
class BaseServiceSpec extends UnitSpec with FutureHelper {
  implicit val now: DateTime = DateTime.now().withZone(DateTimeZone.UTC)

  val AppId = "AppId"
  val UserId = "UserId"
}

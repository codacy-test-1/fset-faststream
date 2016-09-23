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

package scheduler.onlinetesting

import java.util.concurrent.{ ArrayBlockingQueue, ThreadPoolExecutor, TimeUnit }

import config.ScheduledJobConfig
import model.ProgressStatuses.PHASE1_TESTS_SECOND_REMINDER
import model.ReminderNotice
import scheduler.clustering.SingleInstanceScheduledJob
import services.onlinetesting.OnlineTestExpiryService

import scala.concurrent.{ ExecutionContext, Future }

object SecondReminderExpiringTestJob extends SecondReminderExpiringTestJob {
  override val service = OnlineTestExpiryService
}

trait SecondReminderExpiringTestJob extends SingleInstanceScheduledJob with SecondReminderExpiringTestJobConfig {
  val service: OnlineTestExpiryService

  override implicit val ec = ExecutionContext.fromExecutor(new ThreadPoolExecutor(2, 2, 180, TimeUnit.SECONDS, new ArrayBlockingQueue(4)))

  def tryExecute()(implicit ec: ExecutionContext): Future[Unit] = {
    service.processNextTestForReminder(SecondReminder)
  }

}

trait SecondReminderExpiringTestJobConfig extends BasicJobConfig[ScheduledJobConfig] {
  this: SingleInstanceScheduledJob =>
  override val conf = config.MicroserviceAppConfig.secondReminderJobConfig
  val configPrefix = "scheduling.online-testing.second-reminder-expiring-test-job."
  val name = "SecondReminderExpiringTestJob"
  val SecondReminder = ReminderNotice(24, PHASE1_TESTS_SECOND_REMINDER)
}

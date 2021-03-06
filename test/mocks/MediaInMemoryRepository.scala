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

package mocks

import model.persisted.Media
import repositories.MediaRepository

import scala.collection.mutable
import scala.concurrent.Future

object MediaInMemoryRepository extends MediaRepository {

  override def create(addMedia: Media): Future[Unit] = {
    inMemoryRepo += addMedia.userId -> addMedia
    Future.successful(Unit)
  }

  override def find(userId: String): Future[Option[Media]] = ???

  override def cloneAndArchive(originalUserId: String, userIdToArchiveWith: String): Future[Unit] = ???

  override def findAll(): Future[Map[String, Media]] = {
    Future.successful(inMemoryRepo.toMap)
  }

  val inMemoryRepo = new mutable.HashMap[String, Media]
}

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

package repositories

import factories.DateTimeFactory
import model.UniqueIdentifier
import model.assessmentscores._
import testkit.MongoRepositorySpec

class AssessorAssessmentScoresRepositorySpec extends AssessmentScoresRepositorySpec {
  override val collectionName = CollectionNames.ASSESSOR_ASSESSMENT_SCORES
  override def repository = new AssessorAssessmentScoresMongoRepository(DateTimeFactory)
  override lazy val repo = repositories.assessorAssessmentScoresRepository
}

class ReviewerAssessmentScoresRepositorySpec extends AssessmentScoresRepositorySpec {
  override val collectionName = CollectionNames.REVIEWER_ASSESSMENT_SCORES
  override def repository = new ReviewerAssessmentScoresMongoRepository(DateTimeFactory)
  override lazy val repo = repositories.reviewerAssessmentScoresRepository
}

trait AssessmentScoresRepositorySpec extends MongoRepositorySpec {
  def repository: AssessmentScoresMongoRepository
  val repo: AssessmentScoresMongoRepository

  "Assessment Scores Repository" should {
    "create indexes for the repository" in {
      val indexes = indexesWithFields(repo)
      indexes must contain(List("_id"))
      indexes must contain(List("applicationId"))
      indexes.size mustBe 2
    }
  }

  val FsacScores = AssessmentScoresAllExercisesExamples.AssessorOnlyLeadershipExercise
  val ApplicationId = FsacScores.applicationId

  "save" should {
    "create new assessment scores when it does not exist" in {
      repository.save(FsacScores).futureValue
      val result = repository.find(ApplicationId).futureValue
      result mustBe Some(FsacScores)
    }

    "override existing assessment scores when it exists" in {
      repository.save(FsacScores).futureValue
      val FsacScoresRead = repository.find(ApplicationId).futureValue

      val FsacScoresModified =
        FsacScores.copy(leadershipExercise = FsacScores.leadershipExercise.map(_.copy(leadingAndCommunicatingAverage = Some(3.7192))))
      repository.save(FsacScoresModified).futureValue
      val FsacScoresModifiedRead = repository.find(ApplicationId).futureValue

      Some(FsacScoresModified) mustBe FsacScoresModifiedRead
      FsacScoresRead must not be FsacScoresModifiedRead
    }
  }

  "find" should {
    "return None if assessment scores if they do not exist" in {
      val NonExistingApplicationId = UniqueIdentifier.randomUniqueIdentifier
      val result = repository.find(NonExistingApplicationId).futureValue
      result mustBe None
    }

    "return assessment scores if they already exists" in {
      repository.save(FsacScores).futureValue
      val result = repository.find(ApplicationId).futureValue
      result mustBe Some(FsacScores)
    }
  }

  "findAll" should {
    "return empty list when there are no assessment scores" in {
      val result = repository.findAll.futureValue
      result mustBe Nil
    }

    "return all assessment scores when there are some" in {
      val FsacScores2 = AssessmentScoresAllExercisesExamples.AssessorOnlyGroupExercise

      repository.save(FsacScores).futureValue
      repository.save(FsacScores2).futureValue

      val result = repository.findAll.futureValue

      result must have size 2
      result must contain(FsacScores)
      result must contain(FsacScores2)
    }
  }
}

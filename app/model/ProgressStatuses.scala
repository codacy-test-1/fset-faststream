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

package model

import model.ApplicationStatus._
import play.api.libs.json.{ Format, JsString, JsSuccess, JsValue }
import reactivemongo.bson.{ BSON, BSONHandler, BSONString }

import scala.language.implicitConversions

// scalastyle:off number.of.methods
object ProgressStatuses {
  sealed abstract class ProgressStatus(val applicationStatus: ApplicationStatus) {
    def key = toString
  }

  object ProgressStatus {
    implicit val progressStatusFormat = new Format[ProgressStatus] {
      def reads(json: JsValue) = JsSuccess(nameToProgressStatus(json.as[String]))
      def writes(progressStatus: ProgressStatus) = JsString(progressStatus.key)
    }

    implicit object BSONEnumHandler extends BSONHandler[BSONString, ProgressStatus] {
      def read(doc: BSONString) = nameToProgressStatus(doc.value)
      def write(progressStatus: ProgressStatus) = BSON.write(progressStatus.key)
    }

    implicit def progressStatusToString(progressStatus: ProgressStatus): String = progressStatus.getClass.getSimpleName
  }

  case object CREATED extends ProgressStatus(ApplicationStatus.CREATED) {
    override def key = "created"}

  case object PERSONAL_DETAILS extends ProgressStatus(ApplicationStatus.IN_PROGRESS) {
    override def key = "personal-details"}

  case object PARTNER_GRADUATE_PROGRAMMES extends ProgressStatus(ApplicationStatus.IN_PROGRESS) {
    override def key = "partner-graduate-programmes"}

  case object SCHEME_PREFERENCES extends ProgressStatus(ApplicationStatus.IN_PROGRESS) {
    override def key = "scheme-preferences"}

  case object ASSISTANCE_DETAILS extends ProgressStatus(ApplicationStatus.IN_PROGRESS) {
    override def key = "assistance-details"}

  case object PREVIEW extends ProgressStatus(ApplicationStatus.IN_PROGRESS) {
    override def key = "preview"}

  case object SUBMITTED extends ProgressStatus(ApplicationStatus.SUBMITTED)

  case object WITHDRAWN extends ProgressStatus(ApplicationStatus.WITHDRAWN)

  case object PHASE1_TESTS_INVITED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_STARTED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_FIRST_REMINDER extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_SECOND_REMINDER extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_COMPLETED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_EXPIRED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_RESULTS_READY extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_RESULTS_RECEIVED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS)
  case object PHASE1_TESTS_PASSED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS_PASSED)
  case object PHASE1_TESTS_FAILED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS_FAILED)
  case object PHASE1_TESTS_FAILED_NOTIFIED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS_FAILED)

  case object PHASE2_TESTS_INVITED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_STARTED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_FIRST_REMINDER extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_SECOND_REMINDER extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_COMPLETED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_EXPIRED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_RESULTS_READY extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_RESULTS_RECEIVED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS)
  case object PHASE2_TESTS_PASSED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS_PASSED)
  case object PHASE2_TESTS_FAILED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS_FAILED)
  case object PHASE2_TESTS_FAILED_NOTIFIED extends ProgressStatus(ApplicationStatus.PHASE2_TESTS_FAILED)

  case object PHASE3_TESTS_INVITED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS)
  case object PHASE3_TESTS_STARTED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS)
  case object PHASE3_TESTS_FIRST_REMINDER extends ProgressStatus(ApplicationStatus.PHASE3_TESTS)
  case object PHASE3_TESTS_SECOND_REMINDER extends ProgressStatus(ApplicationStatus.PHASE3_TESTS)
  case object PHASE3_TESTS_COMPLETED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS)
  case object PHASE3_TESTS_EXPIRED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS)
  case object PHASE3_TESTS_RESULTS_RECEIVED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS)
  case object PHASE3_TESTS_PASSED_WITH_AMBER extends ProgressStatus(ApplicationStatus.PHASE3_TESTS_PASSED_WITH_AMBER)
  case object PHASE3_TESTS_PASSED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS_PASSED)
  case object PHASE3_TESTS_FAILED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS_FAILED)
  case object PHASE3_TESTS_FAILED_NOTIFIED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS_FAILED)

  // Edip and Sdip status only
  case object PHASE1_TESTS_PASSED_NOTIFIED extends ProgressStatus(ApplicationStatus.PHASE1_TESTS_PASSED_NOTIFIED)
  case object PHASE3_TESTS_PASSED_NOTIFIED extends ProgressStatus(ApplicationStatus.PHASE3_TESTS_PASSED_NOTIFIED)
  case object FAST_PASS_ACCEPTED extends ProgressStatus(ApplicationStatus.FAST_PASS_ACCEPTED)

  case object APPLICATION_ARCHIVED extends ProgressStatus(ApplicationStatus.ARCHIVED)

  case object SIFT_ENTERED extends ProgressStatus(ApplicationStatus.SIFT)
  case object SIFT_READY extends ProgressStatus(ApplicationStatus.SIFT)
  case object SIFT_COMPLETED extends ProgressStatus(ApplicationStatus.SIFT)

  case object ASSESSMENT_CENTRE_AWAITING_ALLOCATION extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_ALLOCATION_UNCONFIRMED extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_ALLOCATION_CONFIRMED extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_FAILED_TO_ATTEND extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_SCORES_ENTERED extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_SCORES_ACCEPTED extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_AWAITING_RE_EVALUATION extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_PASSED extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)
  case object ASSESSMENT_CENTRE_FAILED extends ProgressStatus(ApplicationStatus.ASSESSMENT_CENTRE)


  def getProgressStatusForSdipFsSuccess(applicationStatus: ApplicationStatus): ProgressStatus = {
    case object PHASE1_TESTS_SDIP_FS_PASSED extends ProgressStatus(applicationStatus)
    PHASE1_TESTS_SDIP_FS_PASSED
  }

  def getProgressStatusForSdipFsFailed(applicationStatus: ApplicationStatus): ProgressStatus = {
    case object PHASE1_TESTS_SDIP_FS_FAILED extends ProgressStatus(applicationStatus)
    PHASE1_TESTS_SDIP_FS_FAILED
  }

  def getProgressStatusForSdipFsFailedNotified(applicationStatus: ApplicationStatus): ProgressStatus = {
    case object PHASE1_TESTS_SDIP_FS_FAILED_NOTIFIED extends ProgressStatus(applicationStatus)
    PHASE1_TESTS_SDIP_FS_FAILED_NOTIFIED
  }

  def getProgressStatusForSdipFsPassedNotified(applicationStatus: ApplicationStatus): ProgressStatus = {
    case object PHASE1_TESTS_SDIP_FS_PASSED_NOTIFIED extends ProgressStatus(applicationStatus)
    PHASE1_TESTS_SDIP_FS_PASSED_NOTIFIED
  }

  def nameToProgressStatus(name: String): ProgressStatus = nameToProgressStatusMap(name.toLowerCase)

  // Reflection is generally 'A bad thing' but in this case it ensures that all progress statues are taken into account
  // Had considered an implementation with a macro, but that would need defining in another compilation unit
  // As it is a val in a object, it is only run once upon startup

  // TODO  this does NOT contain all the statuses as it cannot instantiate the SDIP statuses,
  // so we're a few progress statuses short of an application anywhere we're using this list.
  private[model] val allStatuses: Seq[ProgressStatus] = {
    import scala.reflect.runtime.universe._
    val mirror = runtimeMirror(this.getClass.getClassLoader)
    val insMirror = mirror reflect this
    val originType = insMirror.symbol.typeSignature
    val members = originType.members
    members.collect { member =>
      member.typeSignature match {
        case tpe if tpe <:< typeOf[ProgressStatus] && member.isModule =>
          val module = member.asModule
          (mirror reflectModule module).instance.asInstanceOf[ProgressStatus]
      }
    }.toSeq
  }

  private[model] val nameToProgressStatusMap: Map[String, ProgressStatus] = allStatuses.map { value =>
    value.key.toLowerCase -> value
  }.toMap

  def tryToGetDefaultProgressStatus(applicationStatus: ApplicationStatus): Option[ProgressStatus] = {
    val matching = allStatuses.filter(_.applicationStatus == applicationStatus)
    if (matching.size == 1) matching.headOption else None
  }

  @deprecated("This is not exhaustive, do not use please.", "July 2017")
  def progressesByApplicationStatus(applicationStatuses: ApplicationStatus*): Seq[ProgressStatus] = {
    allStatuses.filter(st => applicationStatuses.contains(st.applicationStatus))
  }
}
// scalastyle:on

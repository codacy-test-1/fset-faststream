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

package model.report

import model.command.{ ProgressResponse, ProgressResponseExamples }
import org.scalatestplus.play.PlaySpec

class ProgressStatusesReportLabelsSpec extends PlaySpec {

  import ProgressStatusesReportLabelsSpec._

  "a registered application" should {
    "return registered" in {
      val status = ProgressStatusesReportLabels.progressStatusNameInReports(new ProgressResponse("id", false, false,
        false, false, false, Nil, false, false))
      status must be("registered")
    }
  }

  "a withdrawn application" should {
    "return withdrawn" in {
      ProgressStatusesReportLabels.progressStatusNameInReports(progressResponse) must be("withdrawn")
    }

    "return withdrawn when all other progresses are set" in {
      ProgressStatusesReportLabels.progressStatusNameInReports(completeProgressResponse) must be("withdrawn")
    }
  }

  "a submitted application" should {
    "return submitted" in {
      val customProgress = progressResponse.copy(withdrawn = false)
      ProgressStatusesReportLabels.progressStatusNameInReports(customProgress) must be("submitted")
    }
  }

  "a previewed application" should {
    "return previewed" in {
      val customProgress = ProgressResponseExamples.InPreview
      ProgressStatusesReportLabels.progressStatusNameInReports(customProgress) must be("preview_completed")
    }
  }

  "an application in partner graduate programmes" should {
    "Return partner_graduate_programmes_completed" in {
      val customProgress = ProgressResponseExamples.InPartnerGraduateProgrammes
      ProgressStatusesReportLabels.progressStatusNameInReports(customProgress) must be("partner_graduate_programmes_completed")
    }
  }

  "an application in scheme preferences" should {
    "return scheme_preferences_completed" in {
      val customProgress = ProgressResponseExamples.InSchemePreferences
      ProgressStatusesReportLabels.progressStatusNameInReports(customProgress) must be("scheme_preferences_completed")
    }
  }

  "an application in personal details" should {
    "return personal_details_completed" in {
      val customProgress = ProgressResponseExamples.InPersonalDetails
      ProgressStatusesReportLabels.progressStatusNameInReports(customProgress) must be("personal_details_completed")
    }

    "return personal_details_completed when sections are not completed" in {
      val customProgress = ProgressResponseExamples.InPersonalDetails
      ProgressStatusesReportLabels.progressStatusNameInReports(customProgress) must be("personal_details_completed")
    }
  }
}

object ProgressStatusesReportLabelsSpec {

  val progressResponse = ProgressResponse("1", personalDetails = true, schemePreferences = true,
    partnerGraduateProgrammes = true, assistanceDetails = true, preview = true,
    List("start_questionnaire", "diversity_questionnaire", "education_questionnaire", "occupation_questionnaire"),
    submitted = true, withdrawn = true
  )

  val completeProgressResponse = ProgressResponse("1", personalDetails = true, schemePreferences = true,
    partnerGraduateProgrammes = true, assistanceDetails = true, preview = true,
    List("start_questionnaire", "diversity_questionnaire", "education_questionnaire", "occupation_questionnaire"),
    submitted = true, withdrawn = true
  )
}

package org.sunbird.job.publish.spec

import com.typesafe.config.{Config, ConfigFactory}
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sunbird.job.publish.config.PublishConfig
import org.sunbird.job.publish.core.ObjectData
import org.sunbird.job.publish.helpers.QuestionPdfGenerator
import org.sunbird.job.util.{CloudStorageUtil, HTTPResponse, HttpUtil}


class ObjectPdfGeneratorSpec extends FlatSpec with BeforeAndAfterAll with Matchers with MockitoSugar {

  override protected def beforeAll(): Unit = {
    super.beforeAll()
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
  }

  implicit val mockHttpUtil: HttpUtil = mock[HttpUtil](Mockito.withSettings().serializable())
  val config: Config = ConfigFactory.load("test.conf").withFallback(ConfigFactory.systemEnvironment())
  implicit val publishConfig: PublishConfig = new PublishConfig(config, "")

  implicit val cloudStorageUtil: CloudStorageUtil = new CloudStorageUtil(publishConfig)


  "Object Pdf Generator getPreviewFileUrl" should "return a url of the html file after uploading it to cloud" in {
    val pdfGenerator = new TestQuestionPdfGenerator()
    val fileNameSuffix = System.currentTimeMillis().toString
    val obj = pdfGenerator.getPreviewFileUrl(getObjectList(), getObject(), "questionSetTemplate.vm", fileNameSuffix)
    obj.getOrElse("").isEmpty should be(false)
  }

  "Object PDF generator getPdfFileUrl" should "return a url of the pdf file after uploading it to cloud" in {
    val fileNameSuffix = System.currentTimeMillis().toString
    when(mockHttpUtil.post(s"http://10.5.35.35/print/v1/print/preview/generate?fileUrl=https://sunbirddev.blob.core.windows.net/sunbird-content-dev/questionset/do_xyz/do_xyz_html_${fileNameSuffix}.html", "")).thenReturn(getHttpResponse())
    val pdfGenerator = new TestQuestionPdfGenerator()
    val (pdfUrl, previewUrl) = pdfGenerator.getPdfFileUrl(getObjectList(), getObject(), "questionSetTemplate.vm", "http://10.5.35.35/print", fileNameSuffix)
    pdfUrl.getOrElse("").isEmpty should be(false)
    previewUrl.getOrElse("").isEmpty should be(false)

  }

  "Object PDF generator getPdfFileUrl" should "return a Empty pdfUrl if failed to convert to PDF " in {
    val fileNameSuffix = System.currentTimeMillis().toString
    when(mockHttpUtil.post(s"http://10.5.35.35/print/v1/print/preview/generate?fileUrl=https://sunbirddev.blob.core.windows.net/sunbird-content-dev/questionset/do_xyz/do_xyz_html_${fileNameSuffix}.html", "")).thenReturn(getFailedHttpResponse())
    val pdfGenerator = new TestQuestionPdfGenerator()
    val (pdfUrl, previewUrl) = pdfGenerator.getPdfFileUrl(getObjectList(), getObject(), "questionSetTemplate.vm", "http://10.5.35.35/print", fileNameSuffix)
    pdfUrl.getOrElse("").isEmpty should be(true)
    previewUrl.getOrElse("").isEmpty should be(false)
  }

  private def getObjectList(): List[ObjectData] = {
    val question_1 = new ObjectData("do_123", Map("primaryCategory" -> "Multiple Choice Question", "index" -> 1.asInstanceOf[AnyRef], "IL_UNIQUE_ID" -> "do_123"),
      Some(Map(
        "responseDeclaration" ->
          """
            |{
            |        "response1": {
            |          "maxScore": 1,
            |          "cardinality": "single",
            |          "type": "integer",
            |          "correctResponse": {
            |            "value": 1,
            |            "outcomes": {
            |              "score": 1
            |            }
            |          }
            |        }
            |    }
                    """.stripMargin,
        "interactions" ->
          """
            |{
            |        "response1": {
            |          "type": "choice",
            |          "options": [
            |            {
            |              "label": "<p>2 September 1929</p>",
            |              "value": 0
            |            },
            |            {
            |              "label": "<p>15 October 1931</p>",
            |              "value": 1
            |            },
            |            {
            |              "label": "<p>15 August 1923</p>",
            |              "value": 2
            |            },
            |            {
            |              "label": "<p>29 February 1936</p>",
            |              "value": 3
            |            }
            |          ]
            |        }
            |    }
                    """.stripMargin,
        "body" ->
          """<div class='question-body'>
            |      <div class='question-title'>When was Dr. A.P.J. Abdul Kalam born</div>
            |      <div data-choice-interaction='response1' class='mcq-vertical'></div>
            |    </div>""")), None)
    val question_2 = new ObjectData("do_234", Map("primaryCategory" -> "Multiple Choice Question", "index" -> 2.asInstanceOf[AnyRef], "IL_UNIQUE_ID" -> "do_234"),
      Some(Map(
        "responseDeclaration" ->
          """
            |	{
            |	  "maxScore": 3,
            |      "response1": {
            |        "cardinality": "multiple",
            |        "type": "integer",
            |        "correctResponse": {
            |          "value": [
            |            0,
            |            1,
            |            2
            |          ],
            |          "outcomes": {
            |            "score": 3
            |          }
            |        },
            |        "mapping": [
            |          {
            |            "response": 0,
            |            "outcomes": {
            |              "score": 1
            |            }
            |          },
            |          {
            |            "response": 1,
            |            "outcomes": {
            |              "score": 1
            |            }
            |          },
            |          {
            |            "response": 2,
            |            "outcomes": {
            |              "score": 1
            |            }
            |          }
            |        ]
            |      }
            |    }
                    """.stripMargin,
        "interactions" ->
          """		{
            |        "response1": {
            |          "type": "choice",
            |          "options": [
            |            {
            |              "label": "<p>Failure to Success: Legendary Lives</p>",
            |              "value": 0
            |            },
            |            {
            |              "label": "<p>You Are Born to Blossom</p>",
            |              "value": 1
            |            },
            |            {
            |              "label": "<p>Ignited Minds</p>",
            |              "value": 2
            |            },
            |            {
            |              "label": "<p>A House for Mr. Biswas‎ </p>",
            |              "value": 3
            |            }
            |          ]
            |        }
            |    }
            |    """.stripMargin,
        "body" ->
          """
            |<div class='question-body'>
            |      <div class='question-title'>Which of the following books is written by Dr. A.P.J. Abdul Kalam</div>
            |      <div multiple data-choice-interaction='response1' class='mcq-vertical'></div>
            |    </div>
                    """.stripMargin)))

    val question_3 = new ObjectData("do_345", Map("primaryCategory" -> "Subjective Question", "index" -> 3.asInstanceOf[AnyRef], "IL_UNIQUE_ID" -> "do_345"),
      Some(Map(
        "body" -> " <div>The tenure of APJ Abdul Kalam as Indian President</div>",
        "answer" -> " <div>2002 to 2007</div>"
      )))
    List(question_1, question_2, question_3)
  }

  private def getObject(): ObjectData = {
    new ObjectData("do_xyz", Map("name" -> "Test Question Set", "IL_UNIQUE_ID" -> "do_xyz"))
  }

  private def getHttpResponse(): HTTPResponse = {
    HTTPResponse(200,
      """
        |{
        |"result" : {
        |     "pdfUrl" : "https://dockstorage.blob.core.windows.net/sunbird-content-dock/content/do_11304066349776076815/artifact/do_11304066349776076815_1591877926475.pdf"
        |}
        |}""".stripMargin)
  }

  private def getFailedHttpResponse(): HTTPResponse = {
    HTTPResponse(400,
      """
        |{
        |"result" : {
        |}
        |}""".stripMargin)
  }

}

class TestQuestionPdfGenerator extends QuestionPdfGenerator {
}

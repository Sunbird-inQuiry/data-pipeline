package org.sunbird.job.questionset.publish.domain

import org.apache.commons.lang3.StringUtils
import org.sunbird.job.domain.reader.JobRequest

import java.util
import scala.collection.JavaConverters._

class Event(eventMap: java.util.Map[String, Any], partition: Int, offset: Long) extends JobRequest(eventMap, partition, offset) {

	private val jobName = "questionset-publish"

	private val objectTypes = List("Question", "QuestionImage", "QuestionSet", "QuestionSetImage")
	private val mimeTypes = List("application/vnd.sunbird.question", "application/vnd.sunbird.questionset")

	def eData: Map[String, AnyRef] = readOrDefault("edata", new util.HashMap[String, AnyRef]()).asScala.toMap

	def action: String = readOrDefault[String]("edata.action", "")

	def publishType: String = readOrDefault[String]("edata.publish_type", "")

	def mimeType: String = readOrDefault[String]("edata.metadata.mimeType", "")

	def objectId: String = readOrDefault[String]("edata.metadata.identifier", "")

	def objectType: String = readOrDefault[String]("edata.metadata.objectType", "")

	def lastPublishedBy: String = readOrDefault[String]("edata.metadata.lastPublishedBy", "")
  
	def pkgVersion: Double = {
    val pkgVersion = readOrDefault[Int]("edata.metadata.pkgVersion", 0)
    pkgVersion.toDouble
  }

	def schemaVersion: String = readOrDefault[String]("edata.metadata.schemaVersion", "1.0")

	def validEvent(): Boolean = {
		(StringUtils.equals("publish", action) && StringUtils.isNotBlank(objectId)) && (objectTypes.contains(objectType) && mimeTypes.contains(mimeType))
	}

	def getEventContext(): Map[String, AnyRef] = {
		val mid: String = readOrDefault[String]("mid", "")
		val requestId: String = readOrDefault[String]("edata.requestId", "")
		val defaultFeature = readOrDefault[String]("edata.metadata.objectType", "") match {
			case "Question" | "QuestionImage" => "QuestionPublish"
			case "QuestionSet" | "QuestionSetImage" => "QuestionsetPublish"
			case _ => readOrDefault[String]("edata.action", "")
		}
		val featureName: String = readOrDefault[String]("edata.featureName", defaultFeature)
		Map("mid" -> mid, "requestId" -> requestId, "featureName" -> featureName)
	}
}

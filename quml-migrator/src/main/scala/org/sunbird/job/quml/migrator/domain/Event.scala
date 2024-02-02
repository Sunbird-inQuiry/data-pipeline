package org.sunbird.job.quml.migrator.domain

import org.apache.commons.lang3.StringUtils
import org.sunbird.job.domain.reader.JobRequest

import java.util
import scala.collection.JavaConverters._

class Event(eventMap: java.util.Map[String, Any], partition: Int, offset: Long) extends JobRequest(eventMap, partition, offset) {

	private val jobName = "quml-migrator"

	private val objectTypes = List("Question", "QuestionImage", "QuestionSet", "QuestionSetImage")
	private val mimeTypes = List("application/vnd.sunbird.question", "application/vnd.sunbird.questionset")

	def eData: Map[String, AnyRef] = readOrDefault("edata", new util.HashMap[String, AnyRef]()).asScala.toMap

	def action: String = readOrDefault[String]("edata.action", "")

	def mimeType: String = readOrDefault[String]("edata.metadata.mimeType", "")

	def objectId: String = readOrDefault[String]("edata.metadata.identifier", "")

	def status: String = readOrDefault[String]("edata.metadata.status", "")

	def objectType: String = readOrDefault[String]("edata.metadata.objectType", "")

	def pkgVersion: Double = {
    val pkgVersion = readOrDefault[Int]("edata.metadata.pkgVersion", 0)
    pkgVersion.toDouble
  }

	def schemaVersion: String = readOrDefault[String]("edata.metadata.schemaVersion", "1.0")

	def qumlVersion: Double = {
		val qumlVersion = readOrDefault[Int]("edata.metadata.qumlVersion", 1)
		qumlVersion.toDouble
	}

	def validEvent(): Boolean = {
		(StringUtils.equals("quml-migration", action) && StringUtils.isNotBlank(objectId)) && (objectTypes.contains(objectType) && mimeTypes.contains(mimeType) && !StringUtils.equalsIgnoreCase("1.1", schemaVersion))
	}
}

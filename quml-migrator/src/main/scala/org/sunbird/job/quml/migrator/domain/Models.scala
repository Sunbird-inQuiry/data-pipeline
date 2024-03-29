package org.sunbird.job.quml.migrator.domain

case class MigrationMetadata(identifier: String, objectType: String, mimeType: String, pkgVersion: Double, status: String, qumlVersion: Double, schemaVersion: String)

class ObjectData(val identifier: String, val metadata: Map[String, AnyRef], val extData: Option[Map[String, AnyRef]] = None, val hierarchy: Option[Map[String, AnyRef]] = None) {

  val dbId: String = metadata.getOrElse("identifier", identifier).asInstanceOf[String]

  val dbObjType: String = metadata.getOrElse("objectType", "").asInstanceOf[String]

  val pkgVersion: Int = metadata.getOrElse("pkgVersion", 0.0.asInstanceOf[Number]).asInstanceOf[Number].intValue()

  val mimeType: String = metadata.getOrElse("mimeType", "").asInstanceOf[String]

  def getString(key: String, defaultVal: String): String = metadata.getOrElse(key, defaultVal).asInstanceOf[String]

  def migrationVersion: Double = {
    val migrationVersion = metadata.getOrElse("migrationVersion", 0.0).asInstanceOf[Double]
    migrationVersion
  }

}

case class ExtDataConfig(keyspace: String, table: String, primaryKey: List[String] = List(), propsMapping: Map[String, AnyRef] = Map())

case class ObjectExtData(data: Option[Map[String, AnyRef]] = None, hierarchy: Option[Map[String, AnyRef]] = None)

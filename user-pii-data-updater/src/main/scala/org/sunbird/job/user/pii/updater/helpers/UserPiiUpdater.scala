package org.sunbird.job.user.pii.updater.helpers

import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.sunbird.job.Metrics
import org.sunbird.job.exception.ServerException
import org.sunbird.job.user.pii.updater.domain.{ObjectData, UserPiiEvent}
import org.sunbird.job.user.pii.updater.task.UserPiiUpdaterConfig
import org.sunbird.job.util.{HttpUtil, JSONUtil}

import java.util
import scala.collection.JavaConversions._

trait UserPiiUpdater extends NotificationProcessor {

  private[this] val logger = LoggerFactory.getLogger(classOf[UserPiiUpdater])

  def processResult(userEvent: UserPiiEvent, idMap: util.HashMap[String, String], failedIdMap: util.HashMap[String, String])(implicit config: UserPiiUpdaterConfig, httpUtil: HttpUtil, metrics: Metrics): Unit = {
    if (!idMap.isEmpty && failedIdMap.isEmpty) {
      logger.info(s"UserPiiUpdater ::: processResult :: All PII data processed successfully for user id : ${userEvent.userId}. Total Identifiers affected : ${idMap.keySet()}")
      metrics.incCounter(config.userPiiUpdateSuccessEventCount)
      sendNotification(idMap.toMap, userEvent.userId, userEvent.userName, userEvent.orgAdminUserId)(config, httpUtil)
    } else if (idMap.isEmpty && failedIdMap.isEmpty) {
      logger.info(s"UserPiiUpdater ::: processResult :: Event Skipped for user id : ${userEvent.userId} because no object found for given user.")
      metrics.incCounter(config.userPiiUpdateSkippedEventCount)
    } else if (!idMap.isEmpty && !failedIdMap.isEmpty) {
      logger.info(s"UserPiiUpdater ::: processResult :: All PII data processing completed partially for user id : ${userEvent.userId}. Total Success Identifiers: ${idMap.keySet()} | Total Failed Identifiers : ${failedIdMap.keySet()}")
      throw new ServerException("ERR_PROCESSING_FAILED", s"All PII data processing completed partially for user id : ${userEvent.userId}. Total Success Identifiers: ${idMap.keySet()} | Total Failed Identifiers : ${failedIdMap.keySet()}")
      //metrics.incCounter(config.userPiiUpdatePartialSuccessEventCount)
    } else if (idMap.isEmpty && !failedIdMap.isEmpty) {
      logger.info(s"UserPiiUpdater ::: processResult :: All PII data processing failed for user id : ${userEvent.userId}. Total Failed Identifiers : ${failedIdMap.keySet()}")
      throw new ServerException("ERR_PROCESSING_FAILED", s"All PII data processing failed for user id : ${userEvent.userId}. Total Failed Identifiers : ${failedIdMap.keySet()}")
    }
  }

  def processNestedProp(key: String, node: ObjectData)(implicit config: UserPiiUpdaterConfig): Map[String, String] = {
    val nestedKeys: List[String] = (key.split("\\.")).toList
    val nodeProp = node.getString(nestedKeys(0), "{}")
    val propValue: util.Map[String, AnyRef] = if (StringUtils.isNotBlank(nodeProp)) JSONUtil.deserialize[java.util.Map[String, AnyRef]](nodeProp) else new util.HashMap[String, AnyRef]()
    logger.info("propValue ::: "+propValue)
    val length = nestedKeys.size - 1
    val counter = 1
    setPropValue(propValue, nestedKeys, counter, length, config.user_pii_replacement_value)
    Map(nestedKeys(0) -> node.getString(nestedKeys(0), JSONUtil.serialize(propValue)))
  }
  def setPropValue(data: util.Map[String, AnyRef], keys: List[String], counter: Int, length: Int, propValue: String): Unit = {
    if (counter < length) {
      val nMap: util.Map[String, AnyRef] = data.getOrDefault(keys(counter), new util.HashMap[String, AnyRef]()).asInstanceOf[util.Map[String, AnyRef]]
      setPropValue(nMap, keys, counter + 1, length, propValue)
    } else {
      data.put(keys(counter), propValue)
    }
  }

}

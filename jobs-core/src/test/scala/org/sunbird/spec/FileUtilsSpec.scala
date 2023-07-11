package org.sunbird.spec

import org.scalatest.{FlatSpec, Matchers}
import org.sunbird.job.util.FileUtils

import java.io.File

class FileUtilsSpec extends FlatSpec with Matchers {

  "getBasePath with empty identifier" should "return the path" in {
    val result = FileUtils.getBasePath("")
    result.nonEmpty shouldBe (true)
  }

  "downloadFile " should " download the media source file starting with http or https " in {
    val fileUrl: String = "https://sunbirddevbbpublic.blob.core.windows.net/sunbird-content-staging/content/assets/do_2138334219413176321588/sample3.jpeg"
    val downloadedFile: File = FileUtils.downloadFile(fileUrl, "/tmp/contentBundle")
    assert(downloadedFile.exists())
  }
}

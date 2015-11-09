package org.scurator

import java.io.IOException
import java.net.BindException

import org.apache.curator.test.TestingServer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfterEach, FlatSpec, Matchers}
import org.slf4j.{LoggerFactory, Logger}

/**
  *
  */
trait BaseSCuratorTest extends FlatSpec with Matchers with ScalaFutures with BeforeAndAfterEach {
  protected val logger: Logger = LoggerFactory.getLogger(getClass.getName)

  protected var serverOpt: Option[TestingServer] = None

  private val timeoutSecs: Int = 2
  private val intervalMillis: Int = 100

  implicit val defaultPatience = PatienceConfig(timeout = Span(timeoutSecs, Seconds), interval = Span(intervalMillis, Millis))

  def server: TestingServer = {
    serverOpt.getOrElse(throw new IllegalStateException)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    while (serverOpt.isEmpty) {
      try {
        serverOpt = Some(new TestingServer)
      } catch {
        case e: BindException =>
          logger.warn("Getting bind exception creating TestingServer - retrying to allocate server")
          serverOpt = None
      }
    }
  }

  override def afterEach(): Unit = {
    if (serverOpt.isDefined) {
      try {
        server.close()
      } catch {
        case e: IOException =>
          logger.warn("Issue cleaning up the TestingServer", e)
          e.printStackTrace()
      }
      serverOpt = None
    }
    super.afterEach()
  }
}

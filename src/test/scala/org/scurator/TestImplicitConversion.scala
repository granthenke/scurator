package org.scurator

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryOneTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestImplicitConversion extends BaseSCuratorTest {

  "A SCuratorClient" should "support implicitly wrapping a curator client" in {
    // Create a curator client
    val curatorClient = CuratorFrameworkFactory.newClient(server.getConnectString, new RetryOneTime(1))
    curatorClient.start()

    // Add needed imports (including implicit conversion)
    import org.scurator.SCuratorClient.Implicits._
    import org.scurator.components.ExistsRequest

    import scala.concurrent.ExecutionContext.Implicits.global

    // Call SCuratorClient methods without manualy wrapping
    // If this compiles we are good (but not using compiles matcher so coverage is tracked)
    val result = curatorClient.exists(ExistsRequest(path = "/test")).futureValue
    result should not be 'exists

    curatorClient.close()
  }
}

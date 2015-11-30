package org.scurator

import java.util.concurrent.TimeoutException

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.retry.RetryOneTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components.ExistsRequest

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */
@RunWith(classOf[JUnitRunner])
class TestLifeCyle extends BaseSCuratorTest {

  "A SCuratorClient" should "support starting manually" in {
    // Create a client
    val client = SCuratorClient(CuratorFrameworkFactory.newClient(server.getConnectString, new RetryOneTime(1)))

    // Validate client is not started
    client.state should not be CuratorFrameworkState.STARTED

    // Start the client
    client.start()

    // Validate client is started
    client.state shouldBe CuratorFrameworkState.STARTED

    // Close the client
    client.close()

    // Validate client is stoped
    client.state shouldBe CuratorFrameworkState.STOPPED
  }
  it should "support starting automatically" in {
    // Create and start client
    val client = SCuratorClient(CuratorFrameworkFactory.newClient(server.getConnectString, new RetryOneTime(1)), autoStart = true)

    // Validate client is started
    client.state shouldBe CuratorFrameworkState.STARTED

    // Close the client
    client.close()

    // Validate client is stoped
    client.state shouldBe CuratorFrameworkState.STOPPED
  }
  it should "support blocking until connected with timeout" in {
    val timeoutMillis = 10
    val successMillis = 1000

    // Create a client
    val client = SCuratorClient(CuratorFrameworkFactory.newClient(server.getConnectString, new RetryOneTime(1)))

    // Connect with short timeout
    val connectTimeoutResult = client.connect(timeoutMillis.milliseconds).failed.futureValue
    connectTimeoutResult shouldBe a[TimeoutException]

    // Validate client is not started
    client.state should not be CuratorFrameworkState.STARTED

    // Connect with long timeout
    val connectSuccessResult = client.connect(successMillis.milliseconds)

    // Start the client
    client.start()

    // Validate client is started
    client.state shouldBe CuratorFrameworkState.STARTED

    // Validate the connect result
    whenReady(connectSuccessResult) { result =>
      client.state shouldBe CuratorFrameworkState.STARTED
    }

    // Close the client
    client.close()

    // Validate client is stopped
    client.state shouldBe CuratorFrameworkState.STOPPED
  }
  it should "Not allow start to be called more than once, even when closed" in {
    // Create a client
    val client = SCuratorClient(CuratorFrameworkFactory.newClient(server.getConnectString, new RetryOneTime(1)))

    // Start the client
    client.start()

    // Validate client is started
    client.state shouldBe CuratorFrameworkState.STARTED

    // Close the client
    client.close()

    // Validate client is stopped
    client.state shouldBe CuratorFrameworkState.STOPPED

    // Validate exception when starting the client again
    an[IllegalStateException] should be thrownBy client.start()
  }
}

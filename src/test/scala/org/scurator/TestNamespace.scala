package org.scurator

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.retry.RetryOneTime
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestNamespace extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "use the original namespace" in {
    val testNamespace: String = "testNamespace"
    val fooPath: String = "/foo"

    val namespaceClient = SCuratorClient(
      CuratorFrameworkFactory.builder()
        .connectString(server.getConnectString)
        .retryPolicy(new RetryOneTime(1))
        .namespace(testNamespace)
        .build(), autoStart = true
    )

    namespaceClient.getNamespace shouldBe testNamespace

    // Create a node in the namespace
    val result = namespaceClient.create(CreateRequest(path = fooPath, data = None))

    // Check the response does not include the namespace
    whenReady(result) { result =>
      result.path shouldBe fooPath
    }

    // Check the namespace was used
    whenReady(client.exists(ExistsRequest(path = s"/$testNamespace$fooPath"))) { result =>
      result shouldBe 'exists
    }

    // Close the namespace client
    namespaceClient.close()
  }
  it should "allow creating a child namespace client" in {
    val testNamespace: String = "testNamespace"
    val fooPath: String = "/foo"

    val childNamespaceClient = client.usingNamespace(testNamespace)

    childNamespaceClient.getNamespace shouldBe testNamespace

    // Create a node in the namespace
    val result = childNamespaceClient.create(CreateRequest(path = fooPath, data = None))

    // Check the response does not include the namespace
    whenReady(result) { result =>
      result.path shouldBe fooPath
    }

    // Check (with the original client) the namespace was used
    whenReady(client.exists(ExistsRequest(path = s"/$testNamespace$fooPath"))) { result =>
      result shouldBe 'exists
    }

    // Close the original client
    client.close()

    // Check the childNamespaceClient is closed when the original is closed
    childNamespaceClient.state shouldBe CuratorFrameworkState.STOPPED
  }
}

package org.scurator

import org.apache.zookeeper.Watcher.Event.{EventType, KeeperState}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */
@RunWith(classOf[JUnitRunner])
class TestGetChildren extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "support getting child nodes" in {
    val fooPath: String = "/foo"
    val fooChild1: String = s"$fooPath/1"
    val fooChild2: String = s"$fooPath/2"
    val fooChild3: String = s"$fooPath/3"

    // Create a few nodes
    client.create(CreateRequest(path = fooPath)).futureValue
    client.create(CreateRequest(path = fooChild1)).futureValue
    client.create(CreateRequest(path = fooChild2)).futureValue
    client.create(CreateRequest(path = fooChild3)).futureValue

    // Verify the nodes exists
    val fooExistsResult = client.exists(ExistsRequest(path = fooPath)).futureValue
    fooExistsResult shouldBe 'exists
    val fooChild1ExistsResult = client.exists(ExistsRequest(path = fooChild1)).futureValue
    fooChild1ExistsResult shouldBe 'exists
    val fooChild2ExistsResult = client.exists(ExistsRequest(path = fooChild2)).futureValue
    fooChild2ExistsResult shouldBe 'exists
    val fooChild3ExistsResult = client.exists(ExistsRequest(path = fooChild3)).futureValue
    fooChild3ExistsResult shouldBe 'exists

    // Get the children nodes
    val result = client.getChildren(GetChildrenRequest(path = fooPath))

    whenReady(result) { result =>
      // scalastyle:off magic.number
      result.children.size shouldBe 3
      val sortedChildren = result.children.sorted
      sortedChildren.head shouldBe "1"
      sortedChildren(1) shouldBe "2"
      sortedChildren(2) shouldBe "3"
      // scalastyle:on magic.number
    }
  }
  it should "support getting child nodes with a watcher" in {
    val fooPath: String = "/foo"
    val fooChild1: String = s"$fooPath/1"
    val fooChild2: String = s"$fooPath/2"

    // Create a few nodes
    client.create(CreateRequest(path = fooPath)).futureValue
    client.create(CreateRequest(path = fooChild1)).futureValue

    // Verify the nodes exists
    val fooExistsResult = client.exists(ExistsRequest(path = fooPath)).futureValue
    fooExistsResult shouldBe 'exists
    val fooChild1ExistsResult = client.exists(ExistsRequest(path = fooChild1)).futureValue
    fooChild1ExistsResult shouldBe 'exists

    // Get the children nodes with a watcher
    val resultWithWatcher = client.getChildren(GetChildrenRequest(path = fooPath, watch = true)).futureValue

    // Verify the results
    resultWithWatcher.children.size shouldBe 1
    resultWithWatcher.children.head shouldBe "1"
    resultWithWatcher.watch shouldBe 'isDefined

    val watchEvent = resultWithWatcher.watch.get.event

    // Add a child to trigger the watcher
    client.create(CreateRequest(path = fooChild2)).futureValue

    // Verify the watch event results
    whenReady(watchEvent) { result =>
      result.getPath shouldBe fooPath
      result.getType shouldBe EventType.NodeChildrenChanged
    }
  }
}

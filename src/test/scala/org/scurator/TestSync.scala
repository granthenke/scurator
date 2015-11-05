package org.scurator

import org.apache.zookeeper.Watcher.Event.EventType
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestSync extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "support sync calls" in {
    val fooPath: String = "/foo"

    // Create a node
    client.create(CreateRequest(path = fooPath)).futureValue

    // Call sync on the node
    val result = client.sync(SyncRequest(path = fooPath)).futureValue

    // Validate response
    result.path shouldBe fooPath
  }
}

package org.scurator

import java.util.concurrent.CancellationException

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestWatcher extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "support clearing watchers" in {
    val fooPath: String = "/foo"
    val fooData: Array[Byte] = "foo".getBytes

    // Create a node
    client.create(CreateRequest(path = fooPath)).futureValue

    // Set a watch on the node
    val watcher = client.watch(WatchRequest(path = fooPath)).futureValue.watch

    // Clear the watch
    client.clearWatch(watcher)

    // Verify the watch event fails
    whenReady(watcher.event.failed) { result =>
      result shouldBe a[CancellationException]
    }
  }
}

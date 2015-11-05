package org.scurator

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestCreate extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "support creating nodes with no data" in {
    val fooPath: String = "/foo"

    // Create a node with no data
    val result = client.create(CreateRequest(path = fooPath, data = None))

    // The result should match
    whenReady(result) { result =>
      result.path shouldBe fooPath

      // The foo path should exist
      whenReady(client.exists(ExistsRequest(path = fooPath))) { result =>
        result shouldBe 'exists
      }

      // The foo path should have no data
      whenReady(client.getData(GetDataRequest(path = fooPath))) { result =>
        result.data shouldBe None
      }
    }
  }
  it should "support creating nodes with data" in {
    val fooPath: String = "/foo"
    val fooData: Array[Byte] = "foo".getBytes

    // Create a node with no data
    val result = client.create(CreateRequest(path = fooPath, data = Some(fooData)))

    // The result should match
    whenReady(result) { result =>
      result.path shouldBe fooPath

      // The foo path should exist
      whenReady(client.exists(ExistsRequest(path = fooPath))) { result =>
        result shouldBe 'exists
      }

      // The foo path should have the correct data
      whenReady(client.getData(GetDataRequest(path = fooPath))) { result =>
        result.data.get should contain theSameElementsInOrderAs fooData
      }
    }
  }
  it should "support creating parent nodes when creating nodes" in {
    val parentPath: String = "/parent/path"
    val fooPath: String = s"$parentPath/foo"

    // Create a node with no data
    val result = client.create(CreateRequest(path = fooPath), createParents = true)

    // The result should match
    whenReady(result) { result =>
      result.path shouldBe fooPath

      // The foo path should exist
      whenReady(client.exists(ExistsRequest(path = fooPath))) { result =>
        result shouldBe 'exists
      }
    }
  }
  it should "support creating nodes with a watcher" in {
    val parentPath: String = "/parent/path"
    val fooPath: String = s"$parentPath/foo"

    // Create a node with no data
    val result = client.create(CreateRequest(path = fooPath), createParents = true)

    // The result should match
    whenReady(result) { result =>
      result.path shouldBe fooPath

      // The foo path should exist
      whenReady(client.exists(ExistsRequest(path = fooPath))) { result =>
        result shouldBe 'exists
      }
    }
  }
}

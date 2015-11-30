package org.scurator

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
 *
 */
@RunWith(classOf[JUnitRunner])
class TestDelete extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "support deleting nodes" in {
    val fooPath: String = "/foo"

    // Create a node
    client.create(CreateRequest(path = fooPath)).futureValue

    // Verify the node exists
    val existsResult = client.exists(ExistsRequest(path = fooPath)).futureValue
    existsResult shouldBe 'exists

    // Delete the node
    val result = client.delete(DeleteRequest(path = fooPath))

    whenReady(result) { result =>
      result.path shouldBe fooPath

      // The foo path should not exist
      whenReady(client.exists(ExistsRequest(path = fooPath))) { result =>
        result should not be 'exists
      }
    }
  }
  it should "support support deleting child nodes" in {
    val fooPath: String = "/foo"
    val fooChildAPath: String = s"$fooPath/childA"
    val fooChildA1Path: String = s"$fooChildAPath/1"
    val fooChildA2Path: String = s"$fooChildAPath/2"
    val fooChildBPath: String = s"$fooPath/childB"

    // Create a few nodes
    client.create(CreateRequest(path = fooPath)).futureValue
    client.create(CreateRequest(path = fooChildAPath)).futureValue
    client.create(CreateRequest(path = fooChildA1Path)).futureValue
    client.create(CreateRequest(path = fooChildA2Path)).futureValue
    client.create(CreateRequest(path = fooChildBPath)).futureValue

    // Verify the nodes exists
    val fooExistsResult = client.exists(ExistsRequest(path = fooPath)).futureValue
    fooExistsResult shouldBe 'exists
    val fooChildAExistsResult = client.exists(ExistsRequest(path = fooChildAPath)).futureValue
    fooChildAExistsResult shouldBe 'exists
    val fooChildA1ExistsResult = client.exists(ExistsRequest(path = fooChildA1Path)).futureValue
    fooChildA1ExistsResult shouldBe 'exists
    val fooChildA2ExistsResult = client.exists(ExistsRequest(path = fooChildA2Path)).futureValue
    fooChildA2ExistsResult shouldBe 'exists
    val fooChildBExistsResult = client.exists(ExistsRequest(path = fooChildBPath)).futureValue
    fooChildBExistsResult shouldBe 'exists

    // Delete the node and children
    val result = client.delete(DeleteRequest(path = fooPath), deleteChildren = true)

    whenReady(result) { result =>
      result.path shouldBe fooPath

      // The foo path should not exist (therefore no children do either)
      whenReady(client.exists(ExistsRequest(path = fooPath))) { result =>
        result should not be 'exists
      }
    }
  }
}

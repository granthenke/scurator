package org.scurator

import org.apache.zookeeper.KeeperException.BadVersionException
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestTransactions extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient transaction" should "allow creating multiple nodes" in {
    val fooPath: String = "/foo"
    val fooData: Array[Byte] = "foo".getBytes
    val barPath: String = "/bar"

    // Create 2 nodes
    val trans = client.transaction(Seq(
      CreateRequest(path = fooPath, data = Some(fooData)),
      CreateRequest(path = barPath)
    ))

    // The result should be 2 ordered opResponses with the correct path
    whenReady(trans) { result =>
      result.size shouldBe 2
      result.head shouldBe a[CreateResponse]
      result.head.asInstanceOf[CreateResponse].path shouldBe fooPath
      result(1) shouldBe a[CreateResponse]
      result(1).asInstanceOf[CreateResponse].path shouldBe barPath

      // The foo data should be accurate
      whenReady(client.getData(GetDataRequest(path = fooPath))) { result =>
        result.data.get should contain theSameElementsInOrderAs fooData
      }

      // The bar path should exist
      whenReady(client.exists(ExistsRequest(path = barPath))) { result =>
        result shouldBe 'exists
      }
    }
  }
  it should "support a variety of request types" in {
    val fooPath: String = "/foo"
    val fooData: Array[Byte] = "foo".getBytes
    val barPath: String = "/bar"
    val bazPath: String = "/baz"

    // Set base path and data
    client.create(CreateRequest(path = fooPath)).futureValue
    client.create(CreateRequest(path = barPath)).futureValue

    // Check, SetData, Delete and Create
    val ops = Seq(
      CheckRequest(path = fooPath),
      SetDataRequest(path = fooPath, data = Some(fooData)),
      CheckRequest(path = barPath),
      DeleteRequest(path = barPath),
      CreateRequest(path = bazPath)
    )
    val trans = client.transaction(ops)

    // The result should be 5 ordered opResponses with the correct path
    whenReady(trans) { result =>
      result.size shouldBe ops.size
      result.head shouldBe a[EmptyResponse]
      // scalastyle:off magic.number
      result(1) shouldBe a[SetDataResponse]
      result(1).asInstanceOf[SetDataResponse].stat.getVersion shouldBe 1
      result(1).asInstanceOf[SetDataResponse].stat.getDataLength shouldBe fooData.length
      result(2) shouldBe a[EmptyResponse]
      result(3) shouldBe a[DeleteResponse]
      result(3).asInstanceOf[DeleteResponse].path shouldBe barPath
      result(4) shouldBe a[CreateResponse]
      result(4).asInstanceOf[CreateResponse].path shouldBe bazPath
      // scalastyle:on magic.number

      // The foo data should be accurate
      whenReady(client.getData(GetDataRequest(path = fooPath))) { result =>
        result.data.get should contain theSameElementsInOrderAs fooData
      }

      // The bar path should not exist
      whenReady(client.exists(ExistsRequest(path = barPath))) { result =>
        result should not be 'exists
      }

      // The baz path should not exist
      whenReady(client.exists(ExistsRequest(path = bazPath))) { result =>
        result shouldBe 'exists
      }
    }
  }
  it should "fail if a request fails" in {
    val fooPath: String = "/foo"
    val fooData: Array[Byte] = "foo".getBytes
    val barPath: String = "/bar"

    // Set base path and data
    client.create(CreateRequest(path = fooPath, data = Some(fooData))).futureValue

    // Get the current version
    val version = client.stat(StatRequest(path = fooPath)).futureValue.stat.getVersion
    val trans = client.transaction(Seq(
      CheckRequest(path = fooPath, version = Some(version + 1)),
      CreateRequest(path = barPath)
    ))

    // The transaction should fail with the expected exception
    whenReady(trans.failed) { result =>
      result shouldBe a[BadVersionException]

      // The create request should not have executed
      whenReady(client.exists(ExistsRequest(path = barPath))) { result =>
        result should not be 'exists
      }
    }
  }
}

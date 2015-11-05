package org.scurator

import org.apache.curator.framework.imps.GzipCompressionProvider
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestCompression extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "support creating nodes with compressed data" in {
    val fooPath: String = "/foo"
    val fooData: Array[Byte] = "foo".getBytes
    val fooDataCompressed: Array[Byte] = new GzipCompressionProvider().compress(fooPath, fooData)

    // Create a node with compressed
    val result = client.create(CreateRequest(path = fooPath, data = Some(fooData), compressed = true))

    // The result should match
    whenReady(result) { result =>
      result.path shouldBe fooPath

      // The foo path should exist
      whenReady(client.exists(ExistsRequest(path = fooPath))) { result =>
        result shouldBe 'exists
      }

      // The foo data should be compressed
      whenReady(client.getData(GetDataRequest(path = fooPath))) { result =>
        result.data.get should contain theSameElementsInOrderAs fooDataCompressed
      }

      // The foo data should correctly decompress
      whenReady(client.getData(GetDataRequest(path = fooPath, decompressed = true))) { result =>
        result.data.get should contain theSameElementsInOrderAs fooData
      }
    }
  }
  it should "support setting a node with compressed data" in {
    val fooPath: String = "/foo"
    val fooData: Array[Byte] = "foo".getBytes
    val fooDataCompressed: Array[Byte] = new GzipCompressionProvider().compress(fooPath, fooData)

    // Create a node with no data
    client.create(CreateRequest(path = fooPath)).futureValue
    // Set compressed data on the node
    val result = client.setData(SetDataRequest(path = fooPath, data = Some(fooData), compressed = true))

    whenReady(result) { result =>
      // The foo data should be compressed
      whenReady(client.getData(GetDataRequest(path = fooPath))) { result =>
        result.data.get should contain theSameElementsInOrderAs fooDataCompressed
      }

      // The foo data should correctly decompress
      whenReady(client.getData(GetDataRequest(path = fooPath, decompressed = true))) { result =>
        result.data.get should contain theSameElementsInOrderAs fooData
      }
    }
  }
}

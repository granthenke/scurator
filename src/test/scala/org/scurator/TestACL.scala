package org.scurator

import org.apache.zookeeper.ZooDefs
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scurator.components._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.collection.JavaConverters._

/**
  *
  */
@RunWith(classOf[JUnitRunner])
class TestACL extends BaseSCuratorTest with SCuratorTestClient {

  "A SCuratorClient" should "support getting an ACL from a node" in {
    val fooPath: String = "/foo"

    // Create a node with no ACL
    client.create(CreateRequest(path = fooPath, data = None)).futureValue

    val result = client.getAcl(GetACLRequest(path = fooPath))

    // Verify the default ACL is returned
    whenReady(result) { result =>
      result.acl.size shouldBe 1
      result.acl.head shouldBe ZooDefs.Ids.OPEN_ACL_UNSAFE.asScala.head
    }
  }
  it should "support setting an ACL on a node" in {
    val fooPath: String = "/foo"

    // Create a node with no ACL
    client.create(CreateRequest(path = fooPath, data = None)).futureValue

    // Set an ACL on the nodes
    client.setAcl(SetACLRequest(path = fooPath, acl = ZooDefs.Ids.READ_ACL_UNSAFE.asScala)).futureValue

    // Verify the correct ACL is returned
    whenReady(client.getAcl(GetACLRequest(path = fooPath))) { result =>
      result.acl.size shouldBe 1
      result.acl.head shouldBe ZooDefs.Ids.READ_ACL_UNSAFE.asScala.head
    }
  }
}

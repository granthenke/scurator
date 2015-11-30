package org.scurator

import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.curator.retry.RetryOneTime
import org.scalatest.BeforeAndAfterEach

/**
 *
 */
trait SCuratorTestClient extends BeforeAndAfterEach { self: BaseSCuratorTest =>

  private var clientOpt: Option[SCuratorClient] = None

  def client: SCuratorClient = {
    clientOpt.getOrElse(throw new IllegalStateException)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    if (clientOpt.isEmpty) {
      clientOpt = Some(SCuratorClient(CuratorFrameworkFactory.newClient(server.getConnectString, new RetryOneTime(1)), autoStart = true))
    }
  }

  override def afterEach(): Unit = {
    if (clientOpt.isDefined) {
      if (client.state != CuratorFrameworkState.STOPPED) {
        client.close()
      }
      clientOpt = None
    }
    super.afterEach()
  }

}

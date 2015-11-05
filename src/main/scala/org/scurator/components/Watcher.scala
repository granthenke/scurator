package org.scurator.components

import org.apache.zookeeper.{WatchedEvent, Watcher => ZookeeperWatcher}

import scala.concurrent.{CancellationException, Future, Promise}

class Watcher extends ZookeeperWatcher {
  private val p = Promise[WatchedEvent]()

  override def process(event: WatchedEvent): Unit = {
    p.trySuccess(event)
  }

  def close(): Unit = {
    p.tryFailure(new CancellationException("Watcher closed"))
  }

  val event: Future[WatchedEvent] = p.future
}

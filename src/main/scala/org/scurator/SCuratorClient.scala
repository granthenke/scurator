package org.scurator

import java.util.concurrent.TimeUnit

import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.imps.CuratorFrameworkState
import org.apache.zookeeper.KeeperException.NoNodeException
import org.apache.zookeeper.data.Stat
import org.scurator.components._

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, TimeoutException, blocking}

class SCuratorClient(val underlying: CuratorFramework) {

  /**
   * Start the client
   *
   * This should not be called if autoStart is set to true
   */
  def start(): Unit = underlying.start()

  /**
   * Wait until the client connects
   *
   * @param maxWait the maximum amount of time to wait for a connection
   * @return Future[Unit]: Success if connection has been established, Failure otherwise
   */
  def connect(maxWait: Duration)(implicit executor: ExecutionContext): Future[Unit] = {
    Future {
      val maxWaitMillis = if (maxWait.isFinite() && maxWait >= Duration.Zero) maxWait.toMillis.toInt else -1 // -1 is indefinitely
      val connected = blocking { underlying.blockUntilConnected(maxWaitMillis, TimeUnit.MILLISECONDS) }
      if (!connected) throw new TimeoutException(s"Could not connect within the maximum duration of $maxWait")
    }
  }

  /**
   * Stop the client
   */
  def close(): Unit = underlying.close()

  /**
   * Return the current namespace or "" if none
   *
   * @return namespace
   */
  def getNamespace: String = underlying.getNamespace

  /**
   * Returns the state of this instance
   *
   * @return state
   */
  def state: CuratorFrameworkState = underlying.getState

  /**
   * Returns a facade of the current instance that uses the specified namespace
   * or no namespace if <code>newNamespace</code> is <code>null</code>.
   *
   * @param newNamespace the new namespace or null for none
   * @return facade
   */
  def usingNamespace(newNamespace: String): SCuratorClient = {
    new SCuratorClient(underlying.usingNamespace(newNamespace))
  }

  /**
   * Create a node with the given CreateRequest
   *
   * @param request the CreateRequest to use
   * @param createParents specify if parent nodes should be created
   * @return Future[CreateResponse]
   */
  def create(request: CreateRequest, createParents: Boolean = false)(implicit executor: ExecutionContext): Future[CreateResponse] = {
    Future {
      val builder = underlying.create()
      // This is done in one step due to Curator fluent api type issues. TODO: Create Curator Jira
      // val compressedOpt = request.compressed match {
      //   case false => builder
      //   case true => builder.compressed
      // }
      // val parentOpt = createParents match {
      //   case false => compressedOpt
      //   case true => compressedOpt.creatingParentsIfNeeded
      // }
      val compressedAndParentOpt = (request.compressed, createParents) match {
        case (false, false) => builder
        case (true, false) => builder.compressed
        case (false, true) => builder.creatingParentsIfNeeded
        case (true, true) => builder.compressed.creatingParentsIfNeeded
      }
      val modeOpt = compressedAndParentOpt.withMode(request.mode)
      val aclOpt = request.acl match {
        case None => modeOpt
        case Some(a) => modeOpt.withACL(a.asJava)
      }
      val path = request.data match {
        case None => blocking { aclOpt.forPath(request.path, null) } // scalastyle:ignore
        case Some(d) => blocking { aclOpt.forPath(request.path, d) }
      }
      CreateResponse(path)
    }
  }

  /**
   * Delete a node with the given DeleteRequest
   *
   * @param request the DeleteRequest to use
   * @param deleteChildren specify if children nodes should be deleted
   * @return Future[DeleteResponse]
   */
  def delete(request: DeleteRequest, deleteChildren: Boolean = false)(implicit executor: ExecutionContext): Future[DeleteResponse] = {
    Future {
      val builder = underlying.delete()
      val deleteChildrenOpt = deleteChildren match {
        case false => builder
        case true => builder.deletingChildrenIfNeeded
      }
      val versionOpt = request.version match {
        case None => deleteChildrenOpt
        case Some(v) => deleteChildrenOpt.withVersion(v)
      }
      blocking { versionOpt.forPath(request.path) }
      DeleteResponse(request.path)
    }
  }

  /**
   * Check if a node exists with the given ExistsRequest
   *
   * @param request the ExistsRequest to use
   * @return Future[ExistsResponse]
   */
  def exists(request: ExistsRequest)(implicit executor: ExecutionContext): Future[ExistsResponse] = {
    Future {
      val builder = underlying.checkExists
      val (watchOpt, watcher) = request.watch match {
        case false => (builder, None)
        case true =>
          val w = Some(new Watcher())
          (builder.usingWatcher(w.get), w)
      }
      val result = blocking { watchOpt.forPath(request.path) }
      val statOpt = Option(result)
      ExistsResponse(request.path, statOpt.isDefined, statOpt, watcher)
    }
  }

  /**
   * Get a node's ACL with the given GetACLRequest
   *
   * @param request the GetACLRequest to use
   * @return Future[GetACLResponse]
   */
  def getAcl(request: GetACLRequest)(implicit executor: ExecutionContext): Future[GetACLResponse] = {
    Future {
      val builder = underlying.getACL
      val (statOpt, stat) = {
        val s = new Stat()
        (builder.storingStatIn(s), s)
      }
      val acl = blocking { statOpt.forPath(request.path) }
      GetACLResponse(request.path, acl.asScala, stat)
    }
  }

  /**
   * Get a node's child nodes with the given GetChildrenRequest
   *
   * The list of children returned is not sorted and no guarantee is provided
   * as to its natural or lexical order.
   *
   * @param request the GetChildrenRequest to use
   * @return Future[GetChildrenResponse]
   */
  def getChildren(request: GetChildrenRequest)(implicit executor: ExecutionContext): Future[GetChildrenResponse] = {
    Future {
      val builder = underlying.getChildren
      val (statOpt, stat) = {
        val s = new Stat()
        (builder.storingStatIn(s), s)
      }
      val (watchOpt, watcher) = request.watch match {
        case false => (statOpt, None)
        case true =>
          val w = Some(new Watcher())
          (statOpt.usingWatcher(w.get), w)
      }
      val children = blocking { watchOpt.forPath(request.path) }
      GetChildrenResponse(request.path, children.asScala, stat, watcher)
    }
  }

  /**
   * Get a node's data with the given GetDataRequest
   *
   * @param request the GetDataRequest to use
   * @return Future[GetDataResponse]
   */
  def getData[T](request: GetDataRequest)(implicit executor: ExecutionContext): Future[GetDataResponse] = {
    Future {
      val builder = underlying.getData
      val decompressedOpt = request.decompressed match {
        case false => builder
        case true => builder.decompressed
      }
      val (statOpt, stat) = {
        val s = new Stat()
        (decompressedOpt.storingStatIn(s), s)
      }
      val (watchOpt, watcher) = request.watch match {
        case false => (statOpt, None)
        case true =>
          val w = Some(new Watcher())
          (statOpt.usingWatcher(w.get), w)
      }
      val data = blocking { watchOpt.forPath(request.path) }
      GetDataResponse(request.path, Option(data), stat, watcher)
    }
  }

  /**
   * Set a node's ACL with the given SetACLRequest
   *
   * @param request the SetACLRequest to use
   * @return Future[SetACLResponse]
   */
  def setAcl(request: SetACLRequest)(implicit executor: ExecutionContext): Future[SetACLResponse] = {
    Future {
      val builder = underlying.setACL()
      val versionOpt = request.version match {
        case None => builder
        case Some(v) => builder.withVersion(v)
      }
      val stat = blocking { versionOpt.withACL(request.acl.asJava).forPath(request.path) }
      SetACLResponse(request.path, stat)
    }
  }

  /**
   * Set a node's data with the given SetDataRequest
   *
   * @param request the SetDataRequest to use
   * @return Future[SetDataResponse]
   */
  def setData(request: SetDataRequest)(implicit executor: ExecutionContext): Future[SetDataResponse] = {
    Future {
      val builder = underlying.setData()
      val compressedOpt = request.compressed match {
        case false => builder
        case true => builder.compressed()
      }
      val versionOpt = request.version match {
        case None => compressedOpt
        case Some(v) => compressedOpt.withVersion(v)
      }
      val stat = request.data match {
        case None => blocking { versionOpt.forPath(request.path, null) } // scalastyle:ignore
        case Some(d) => blocking { versionOpt.forPath(request.path, d) }
      }
      SetDataResponse(request.path, stat)
    }
  }

  /**
   * Get a node's stat object with the given StatRequest
   *
   * @param request the StatRequest to use
   * @return Future[StatResponse]
   */
  def stat(request: StatRequest)(implicit executor: ExecutionContext): Future[StatResponse] = {
    exists(ExistsRequest(request.path)).map { response =>
      response.stat.map { s =>
        StatResponse(request.path, stat = s)
      }.getOrElse(throw new NoNodeException(request.path))
    }
  }

  /**
   * Sets a watch on a node
   *
   * @param request the WatchRequest to use
   * @return Future[WatchResponse]
   */
  def watch(request: WatchRequest)(implicit executor: ExecutionContext): Future[WatchResponse] = {
    exists(ExistsRequest(request.path, watch = true)).map { response =>
      response.watch.map { w =>
        WatchResponse(request.path, watch = w)
      }.getOrElse(throw new NoNodeException(request.path))
    }
  }

  /**
   * Flushes channel between process and leader
   *
   * @param request the SyncRequest to use
   * @return Future[SyncResponse]
   */
  def sync(request: SyncRequest)(implicit executor: ExecutionContext): Future[SyncResponse] = {
    Future {
      blocking { underlying.sync().forPath(request.path) }
      SyncResponse(request.path)
    }
  }

  /**
   * Executes multiple ZooKeeper operations as a single TransactionRequest
   *
   * On success, a list of OpResponse is returned
   * On failure, none of the ops are completed, and the Future is failed with the exception that caused the failure
   *
   * @param ops a Sequence of OpRequests
   * @return `Future[Seq[OpResponse]]`
   */
  def transaction(ops: Seq[OpRequest])(implicit executor: ExecutionContext): Future[Seq[OpResponse]] = {
    Future {
      val request = TransactionRequest(ops)
      val transaction = Transactions.toCuratorTransaction(underlying.inTransaction(), request)
      val results = blocking { transaction.commit() }
      val response = Transactions.fromCuratorTransactionResults(results.asScala.toSeq)
      response.ops
    }
  }

  /**
   * Clear internal references to watchers that may inhibit garbage collection.
   * Call this method on watchers you are no longer interested in.
   *
   * @param watch the watcher reference to clear
   */
  def clearWatch(watch: Watcher)(implicit executor: ExecutionContext): Unit = {
    watch.close()
    underlying.clearWatcherReferences(watch)
  }
}

object SCuratorClient {

  /**
   * Creates a SCuratorClient by wrapping the passed CuratorFramework.
   *
   * If autoStart is true. The client will be started if it has not been started already.
   *
   * @param underlying the CuratorFramework to wrap
   * @param autoStart true if client should be started automatically
   * @return
   */
  def apply(underlying: CuratorFramework, autoStart: Boolean = false): SCuratorClient = {
    val client = new SCuratorClient(underlying)
    if (client.state == CuratorFrameworkState.LATENT && autoStart) {
      client.start()
    }
    client
  }

  object Implicits {
    import scala.language.implicitConversions
    implicit def curatorToSCurator(underlying: CuratorFramework): SCuratorClient = {
      new SCuratorClient(underlying)
    }
  }
}

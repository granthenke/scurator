package org.scurator.components

import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.data.ACL

sealed trait Request

/**
  * OpRequest is used to compose a Transaction request.
  * A Transaction request can be composed by OpRequest only.
  */
sealed trait OpRequest extends Request

case class CheckRequest(path: String, version: Option[Int] = None) extends OpRequest

case class CreateRequest(path: String, data: Option[Array[Byte]] = None, acl: Option[Seq[ACL]] = None,
  mode: CreateMode = CreateMode.PERSISTENT, compressed: Boolean = false) extends OpRequest

case class DeleteRequest(path: String, version: Option[Int] = None) extends OpRequest

case class SetDataRequest(path: String, data: Option[Array[Byte]], version: Option[Int] = None, compressed: Boolean = false) extends OpRequest

case class TransactionRequest(ops: Seq[OpRequest]) extends Request

case class ExistsRequest(path: String, watch: Boolean = false) extends Request

case class StatRequest(path: String) extends Request

case class WatchRequest(path: String) extends Request

case class GetACLRequest(path: String) extends Request

case class GetChildrenRequest(path: String, watch: Boolean = false) extends Request

case class GetDataRequest(path: String, watch: Boolean = false, decompressed: Boolean = false) extends Request

case class SetACLRequest(path: String, acl: Seq[ACL], version: Option[Int] = None) extends Request

case class SyncRequest(path: String) extends Request

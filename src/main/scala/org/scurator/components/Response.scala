package org.scurator.components

import org.apache.zookeeper.data.{ACL, Stat}

sealed trait Response

/**
  * OpResponse is used to compose a Transaction response.
  * A Transaction response can be composed by OpResult only.
  */
sealed trait OpResponse extends Response

case class CreateResponse(path: String) extends OpResponse

case class DeleteResponse(path: String) extends OpResponse

case class EmptyResponse() extends Response with OpResponse

case class SetDataResponse(path: String, stat: Stat) extends Response with OpResponse

case class TransactionResponse(ops: Seq[OpResponse]) extends Response

case class ExistsResponse(path: String, exists: Boolean, stat: Option[Stat], watch: Option[Watcher]) extends Response

case class StatResponse(path: String, stat: Stat) extends Response

case class WatchResponse(path: String, watch: Watcher) extends Response

case class GetACLResponse(path: String, acl: Seq[ACL], stat: Stat) extends Response

case class GetChildrenResponse(path: String, children: Seq[String], stat: Stat, watch: Option[Watcher]) extends Response

case class GetDataResponse(path: String, data: Option[Array[Byte]], stat: Stat, watch: Option[Watcher]) extends Response

case class SetACLResponse(path: String, stat: Stat) extends Response

case class SyncResponse(path: String) extends Response

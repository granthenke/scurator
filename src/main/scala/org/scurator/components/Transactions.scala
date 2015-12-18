package org.scurator.components

import org.apache.curator.framework.api.transaction._

import scala.collection.JavaConverters._

object Transactions {

  def toCuratorTransaction(transaction: CuratorTransaction, transactionRequest: TransactionRequest): CuratorTransactionFinal = {
    transactionRequest.ops.foldLeft(transaction) {
      case (trans, op) =>
        op match {
          case check: CheckRequest => handleCheckRequest(trans, check).and()
          case create: CreateRequest => handleCreateRequest(trans, create).and()
          case delete: DeleteRequest => handleDeleteRequest(trans, delete).and()
          case setData: SetDataRequest => handleSetDataRequest(trans, setData).and()
        }
    }.asInstanceOf[CuratorTransactionFinal]
    // We should always be able to based on org.apache.curator.framework.imps.CuratorTransactionImpl
  }

  def fromCuratorTransactionResults(results: Seq[CuratorTransactionResult]): TransactionResponse = {
    val ops = {
      results.map { result =>
        result.getType match {
          case OperationType.CHECK => handleCheckResponse(result)
          case OperationType.CREATE => handleCreateResponse(result)
          case OperationType.DELETE => handleDeleteResponse(result)
          case OperationType.SET_DATA => handleSetDataResponse(result)
        }
      }
    }
    TransactionResponse(ops)
  }

  private def handleCheckRequest(transaction: CuratorTransaction, request: CheckRequest): CuratorTransactionBridge = {
    val builder = transaction.check()
    val versionOpt = request.version match {
      case None => builder
      case Some(v) => builder.withVersion(v)
    }
    versionOpt.forPath(request.path)
  }

  private def handleCheckResponse(result: CuratorTransactionResult): EmptyResponse = EmptyResponse()

  private def handleCreateRequest(transaction: CuratorTransaction, request: CreateRequest): CuratorTransactionBridge = {
    val builder = transaction.create()
    // Can't support compressed option due to type bug in Curator TODO: Waiting for CURATOR-278
    // val compressedOpt = request.compressed match {
    //   case false => builder
    //   case true => builder.compressed
    // }
    if (request.compressed) throw new UnsupportedOperationException("compression is not supported in transactions. See Jira CURATOR-278")
    val modeOpt = builder.withMode(request.mode)
    val aclOpt = request.acl match {
      case None => modeOpt
      case Some(a) => modeOpt.withACL(a.asJava)
    }
    request.data match {
      case None => aclOpt.forPath(request.path, null) // scalastyle:ignore
      case Some(d) => aclOpt.forPath(request.path, d)
    }
  }

  private def handleCreateResponse(result: CuratorTransactionResult): CreateResponse = new CreateResponse(result.getForPath)

  private def handleDeleteRequest(transaction: CuratorTransaction, request: DeleteRequest): CuratorTransactionBridge = {
    val builder = transaction.delete()
    val versionOpt = request.version match {
      case None => builder
      case Some(v) => builder.withVersion(v)
    }
    versionOpt.forPath(request.path)
  }

  private def handleDeleteResponse(result: CuratorTransactionResult): DeleteResponse = new DeleteResponse(result.getForPath)

  private def handleSetDataRequest(transaction: CuratorTransaction, request: SetDataRequest): CuratorTransactionBridge = {
    val builder = transaction.setData()
    // Can't support compressed option due to type bug in Curator TODO: Waiting for CURATOR-278
    // val compressedOpt = request.compressed match {
    //   case false => builder
    //   case true => builder.compressed()
    // }
    if (request.compressed) throw new UnsupportedOperationException("compression is not supported in transactions. See Jira CURATOR-278")
    val versionOpt = request.version match {
      case None => builder
      case Some(v) => builder.withVersion(v)
    }
    request.data match {
      case None => versionOpt.forPath(request.path, null) // scalastyle:ignore
      case Some(d) => versionOpt.forPath(request.path, d)
    }
  }

  private def handleSetDataResponse(result: CuratorTransactionResult): SetDataResponse = {
    new SetDataResponse(result.getForPath, result.getResultStat)
  }
}

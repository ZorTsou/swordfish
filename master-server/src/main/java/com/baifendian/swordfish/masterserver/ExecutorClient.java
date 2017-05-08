/*
 * Copyright (C) 2017 Baifendian Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baifendian.swordfish.masterserver;

import com.baifendian.swordfish.common.job.exception.ExecException;
import com.baifendian.swordfish.masterserver.utils.ResultHelper;
import com.baifendian.swordfish.rpc.RetInfo;
import com.baifendian.swordfish.rpc.WorkerService;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutorClient {

  private static Logger logger = LoggerFactory.getLogger(ExecutorClient.class);

  // executor host
  private String host;

  // executor 端口信息
  private int port;

  // 超时设置
  private int timeout = 3000;

  // rpc 失败的重试次数
  private static final int RPC_RETRIES = 3;

  private TTransport tTransport;

  private WorkerService.Client client;

  // 重试次数
  private int retries;

  public ExecutorClient(String host, int port, int retries) {
    this.host = host;
    this.port = port;
    this.retries = retries;
  }

  public ExecutorClient(String host, int port) {
    this(host, port, ExecutorClient.RPC_RETRIES);
  }

  public ExecutorClient(ExecutorServerInfo executorServerInfo) {
    this.host = executorServerInfo.getHost();
    this.port = executorServerInfo.getPort();
    this.retries = RPC_RETRIES;
  }

  private void connect() {
    tTransport = new TSocket(host, port, timeout);

    try {
      TProtocol protocol = new TBinaryProtocol(tTransport);
      client = new WorkerService.Client(protocol);
      tTransport.open();
    } catch (TTransportException e) {
      logger.error("Catch an exception", e);
    }
  }

  private void close() {
    if (tTransport != null) {
      tTransport.close();
    }
  }

  /**
   * 调度执行
   *
   * @param execId
   * @param scheduleDate
   * @return
   */
  public boolean scheduleExecFlow(int execId, long scheduleDate) {
    connect();

    try {
      client.scheduleExecFlow(execId, scheduleDate);
    } catch (TException e) {
      logger.error("report info error", e);
      return false;
    } finally {
      close();
    }

    return true;
  }

  /**
   * 执行即席查询
   *
   * @param id
   * @throws TException
   */
  public void execAdHoc(int id) throws TException {
    connect();

    try {
      client.execAdHoc(id);
    } finally {
      close();
    }
  }

  /**
   * 执行工作流
   *
   * @param execId
   * @return
   * @throws TException
   */
  public boolean execFlow(int execId) throws TException {
    boolean result = false;

    for (int i = 0; i < retries; i++) {
      result = execFlowOne(execId);
      if (result) {
        break;
      }
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        logger.error("Catch an exception", e);
      }
    }

    return result;
  }

  /**
   * 执行一次
   *
   * @param execId
   * @return
   * @throws TException
   */
  public boolean execFlowOne(int execId) throws TException {
    connect();

    try {
      RetInfo retInfo = client.execFlow(execId);
      if (retInfo.getStatus() != ResultHelper.SUCCESS.getStatus()) {
        throw new ExecException(retInfo.getMsg());
      }
    } catch (TException e) {
      logger.error("exec flow error", e);
      throw e;
    } finally {
      close();
    }

    return true;
  }

  /**
   * 取消执行
   *
   * @param execId
   * @return
   * @throws TException
   */
  public RetInfo cancelExecFlow(int execId) throws TException {
    connect();

    try {
      return client.cancelExecFlow(execId);
    } finally {
      close();
    }
  }
}

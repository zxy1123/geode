/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.geode.internal.cache.tier.sockets;

import org.apache.geode.internal.cache.InternalCache;
import org.apache.geode.internal.cache.tier.Acceptor;
import org.apache.geode.internal.cache.tier.CachedRegionHelper;
import org.apache.geode.internal.cache.tier.sockets.sasl.SaslAuthenticator;
import org.apache.geode.internal.cache.tier.sockets.sasl.SaslCallbackHandler;
import org.apache.geode.internal.cache.tier.sockets.sasl.SaslMessenger;
import org.apache.geode.internal.security.SecurityService;

import javax.security.sasl.Sasl;
import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;
import java.io.*;
import java.net.Socket;
import java.nio.channels.AcceptPendingException;
import java.util.Collections;

/**
 * Holds the socket and protocol handler for the new client protocol.
 */
public class GenericProtocolServerConnection extends ServerConnection {
  // The new protocol lives in a separate module and gets loaded when this class is instantiated.
  private final ClientProtocolMessageHandler messageHandler;
  private boolean isAutenticated = false;

  /**
   * Creates a new <code>GenericProtocolServerConnection</code> that processes messages received
   * from an edge client over a given <code>Socket</code>.
   */
  public GenericProtocolServerConnection(Socket s, InternalCache c, CachedRegionHelper helper,
      CacheServerStats stats, int hsTimeout, int socketBufferSize, String communicationModeStr,
      byte communicationMode, Acceptor acceptor, ClientProtocolMessageHandler newClientProtocol,
      SecurityService securityService) {
    super(s, c, helper, stats, hsTimeout, socketBufferSize, communicationModeStr, communicationMode,
        acceptor, securityService);
    this.messageHandler = newClientProtocol;
  }

  @Override
  protected void doOneMessage() {
    try {
      Socket socket = this.getSocket();
      InputStream inputStream = socket.getInputStream();
      OutputStream outputStream = socket.getOutputStream();
      DataInputStream dataInputStream = new DataInputStream(inputStream);
      DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
      authenticateClient(dataInputStream, dataOutputStream);
      messageHandler.receiveMessage(inputStream, outputStream, this.getCache());
    } catch (IOException e) {
      logger.warn(e);
      this.setFlagProcessMessagesAsFalse(); // TODO: better shutdown.
    }
  }

  private void authenticateClient(DataInputStream inputStream, DataOutputStream outputStream)
      throws IOException {
      SaslServer saslServer = Sasl.createSaslServer("PLAIN", "geode", "localhost", Collections.emptyMap(), new SaslCallbackHandler());
      SaslAuthenticator saslAuthenticator = new SaslAuthenticator(saslServer, new SaslMessenger(inputStream, outputStream));
//      if(!isAutenticated) {
      if (saslAuthenticator.authenticateClient()) {
        outputStream.writeByte(Acceptor.SUCCESSFUL_SERVER_TO_CLIENT);
      } else {
        outputStream.writeByte(Acceptor.UNSUCCESSFUL_SERVER_TO_CLIENT);
      }
      isAutenticated = true;
//      }
    System.out.println("Done authenticating");
  }

  @Override
  protected boolean doHandShake(byte epType, int qSize) {
//    return (new SaslAuthenticator(theSocket, securityService)).authenticateClient();
    return true;
  }

  @Override
  public boolean isClientServerConnection() {
    return true;
  }
}

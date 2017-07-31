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

package org.apache.geode.internal.cache.tier.sockets.sasl;

import javax.security.sasl.SaslException;
import javax.security.sasl.SaslServer;

import org.apache.logging.log4j.Logger;

import org.apache.geode.internal.logging.LogService;
import org.apache.geode.internal.security.SecurityService;

import java.io.IOException;

/**
 * SaslAuthenticator performs simple authentication using SASL
 */
public class SaslAuthenticator {
  protected static final Logger logger = LogService.getLogger();

  private final SaslServer saslServer;
  private final SaslMessenger saslMessenger;

  public SaslAuthenticator(SaslServer saslServer, SaslMessenger saslMessenger) {
    this.saslServer = saslServer;
    this.saslMessenger = saslMessenger;
  }

  public boolean authenticateClient() {
    try {
      byte[] challenge = saslServer.evaluateResponse(new byte[0]);

      while(!saslServer.isComplete()) {
        saslMessenger.sendMessage(challenge);
        byte[] response = saslMessenger.readMessage();
        challenge = saslServer.evaluateResponse(response);
      }
      System.out.println(">>>>>>> Completed authentication");

      return true;
    } catch (IOException e) {
      logger.warn("client authentication failed", e);

      return false;
    }
  }
}

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

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.AuthorizeCallback;

/**
 * SASL invokes a callback handler during authentication
 */
public class SaslCallbackHandler implements CallbackHandler {

  @Override
  public void handle(Callback[] callbacks)
      throws IOException, UnsupportedCallbackException {
    for (Callback callback: callbacks) {
      System.out.println("ClientCallbackHandler processing callback " + callback);

      if (callback instanceof TextOutputCallback) {
        // display the message according to the specified type
        TextOutputCallback toc = (TextOutputCallback) callback;
        switch (toc.getMessageType()) {
          case TextOutputCallback.INFORMATION:
            System.out.println(toc.getMessage());
            break;
          case TextOutputCallback.ERROR:
            System.out.println("ERROR: " + toc.getMessage());
            break;
          case TextOutputCallback.WARNING:
            System.out.println("WARNING: " + toc.getMessage());
            break;
          default:
            throw new IOException("Unsupported message type: " +
                toc.getMessageType());
        }

      } else if (callback instanceof NameCallback) {
        NameCallback nc = (NameCallback) callback;
        System.out.println("name from name callback: " +nc.getDefaultName());
      } else if (callback instanceof PasswordCallback) {
        PasswordCallback pc = (PasswordCallback) callback;
        System.out.println("client is setting password");
        pc.setPassword("secretsecret".toCharArray());
      } else if (callback instanceof AuthorizeCallback) {

        AuthorizeCallback ac = (AuthorizeCallback) callback;
        ac.setAuthorized(true);
      } else {
        throw new UnsupportedCallbackException
            (callback, "Unrecognized Callback");
      }
    }
  }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.tomcat;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import org.apache.tomcat.websocket.WsSession;

// Disabled now bug has been fixed
//@ServerEndpoint(value = "/bug59659")
public class Bug59659 {
    private static final AtomicInteger count  = new AtomicInteger (0);

	@OnOpen
    public void wsOpen(@SuppressWarnings("unused") Session session) {
        count.incrementAndGet();
    }

    @OnError
    public void wsError(Session session, Throwable t) {
        System.out.println("WS Error ");
        t.printStackTrace();
        (new Exception()).printStackTrace();
        try {
            WsSession s = (WsSession) session;
            s.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }

    @OnClose
    public void wsClosed(Session session) {
        count.decrementAndGet();
        try {
            session.close();
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
}

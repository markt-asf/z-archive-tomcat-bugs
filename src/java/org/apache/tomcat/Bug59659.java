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

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.tomcat.websocket.WsSession;

@ServerEndpoint(value = "/bug59659")
public class Bug59659 {
    private static final AtomicInteger count  = new AtomicInteger (0);

	@OnOpen
    public void wsOpen(@SuppressWarnings("unused") Session session){
        int c = count.incrementAndGet();
        System.out.println("WS Opened " + c);
    }

    @OnError
    public void wsError(Session session, @SuppressWarnings("unused") Throwable t){
        System.out.println("WS Error ");
        try {
            WsSession s = (WsSession) session;
            s.close();
        } catch (IOException e) {}
    }

    @OnClose
    public void wsClosed(Session session){
        int c = count.decrementAndGet();
        System.out.println("WS Closed " + c);
        try {
            session.close();
        } catch (IOException e) {}
    }
}

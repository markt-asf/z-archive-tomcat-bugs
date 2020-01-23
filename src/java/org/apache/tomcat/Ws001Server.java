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

import java.util.Random;
import java.util.concurrent.CopyOnWriteArraySet;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/ws001")
public class Ws001Server {

    private Session session;
    private boolean keepingrunning;
    private static CopyOnWriteArraySet<Ws001Server> webSocketSet = new CopyOnWriteArraySet<>();


    @OnClose
    public void onClose() {
        System.out.println("Close Connection ...");
        keepingrunning = true;
        webSocketSet.remove(this);
    }


    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Open Connection ...");
        keepingrunning = true;
        this.session = session;
        webSocketSet.add(this);
    }


    @OnMessage
    public void onMessage(String message) {
        System.out.println("Message from the client: " + message);
        try {
            Random r = new Random();
            // keep sending data
            // BUG IS HERE
            // The application needs to spawn a separate thread for this. Doing
            // it in on the container thread means no other container threads
            // can do anything on this connection.
            while (keepingrunning) {
                int daf = r.nextInt();
                this.session.getBasicRemote().sendText(Integer.toString(daf));
                Thread.sleep(20);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("end");
    }


    @OnError
    public void onError(Throwable e) {
        keepingrunning = true;
        e.printStackTrace();
    }
}

package org.apache.tomcat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
 * Test results
 * Single runs. Rounded to nearest 10ms.
 *
 * OS               JRE     JRE version     Selector    8k (ms)     1k (ms)
 * markt's MacBook
 * MacOS 10.14.6    Oracle  1.7.0_80-b15    KQueue (D)  310         430
 * MacOS 10.14.6    Oracle  1.7.0_80-b15    Poll        320         420
 * MacOS 10.14.6    Adopt   1.8.0_212-b03   KQueue (D)  300         400
 * MacOS 10.14.6    Adopt   1.8.0_212-b03   Poll        320         420
 * MacOS 10.14.6    Oracle  9.0.4+11        KQueue (D)  320         470
 * MacOS 10.14.6    Oracle  10.2.3+13       KQueue (D)  320         490
 * MacOS 10.14.6    Adopt   11.0.3+7        KQueue (D)  320         450
 * MacOS 10.14.6    Adopt   11.0.3+7        Poll        340         460
 * MacOS 10.14.6    Adopt   13.0.1+9        KQueue (D)  340         430
 * MacOS 10.14.6    Adopt   13.0.1+9        Poll        330         460
 *
 * markt's desktop                                               (forced to 2304 bytes)
 * Ubuntu 18.04.3   OpenJDK 13.0.1+9        EPoll (D)   420      56,130
 * Ubuntu 18.04.3   OpenJDK 13.0.1+9        Poll        450      56,230
 * Ubuntu 18.04.3   Adopt   11.0.4+11       EPoll (D)   470      56,140
 * Ubuntu 18.04.3   Adopt   11.0.4+11       Poll        450      56,160
 * Ubuntu 18.04.3   Adopt   1.8.0-222-b10   EPoll (D)   530      56,150
 * Ubuntu 18.04.3   Adopt   1.8.0-222-b10   Poll        420      56,100
 * Ubuntu 18.04.3   Sun     1.6.0_45-b06    EPoll (D)   400      56,080
 * Ubuntu 18.04.3   Sun     1.6.0_45-b06    Poll        410      56,100
 */
public class Bug63916NioPoller {


    public static void main(String[] args) throws Exception {

        int sendBufferSize = -1;
        if (args != null && args.length > 0) {
            sendBufferSize = Integer.parseInt(args[0]);
        }
        Server server = new Server(sendBufferSize);
        Thread serverThread = new Thread(server);
        serverThread.start();
        serverThread.join();
    }

    public static class Server implements Runnable {

        private final int sendBufferSize;

        public Server(int sendBufferSize) {
            this.sendBufferSize = sendBufferSize;
        }

        @Override
        public void run() {
            try {
                // Need to set the SelectorProvider before the Poller is created

                // Available on Linux (8, 11, 13), MacOS (7, 8, 11, 13)
                //System.setProperty("java.nio.channels.spi.SelectorProvider", "sun.nio.ch.PollSelectorProvider");

                // Default on Linux (6, 8, 11, 13)
                //System.setProperty("java.nio.channels.spi.SelectorProvider", "sun.nio.ch.EPollSelectorProvider");

                // Default on MacOS (7, 8, 9, 10, 11, 13)
                //System.setProperty("java.nio.channels.spi.SelectorProvider", "sun.nio.ch.KQueueSelectorProvider");

                // Start the Poller
                Poller poller = new Poller();
                Thread pollerThread = new Thread(poller);
                pollerThread.start();

                // Open a server socket
                ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 8080);
                serverSocketChannel.socket().bind(addr);

                // Wait for the first (and only) connection
                SocketChannel socketChannel = serverSocketChannel.accept();
                // Make it non-blocking
                socketChannel.configureBlocking(false);
                System.out.println("Java Vendor:          " + System.getProperty("java.vendor"));
                System.out.println("Java Runtime Name:    " + System.getProperty("java.runtime.name"));
                System.out.println("Java VM name:         " + System.getProperty("java.vm.name"));
                System.out.println("Java Runtime Version: " + System.getProperty("java.runtime.version"));
                System.out.println("Selector provider: " + SelectorProvider.provider());
                System.out.println("Default send buffer size is [" + socketChannel.socket().getSendBufferSize() + "]");
                if (sendBufferSize != -1) {
                    socketChannel.socket().setSendBufferSize(sendBufferSize);
                }
                System.out.println("Used send buffer size is [" + socketChannel.socket().getSendBufferSize() + "]");

                Connection connection = new Connection(socketChannel, poller);

                long startTime = System.currentTimeMillis();

                // Register the newly opened connection with the Poller
                poller.register(connection);

                pollerThread.join();

                long endTime = System.currentTimeMillis();

                System.out.println("Writing 10MB took [" + (endTime - startTime) + "] milliseconds");

                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static class Poller implements Runnable {

        private final Selector selector = Selector.open();

        private volatile boolean running = true;
        private Connection pendingRegistration;
        private Connection writeInterest;

        public Poller() throws IOException{
        }

        @Override
        public void run() {

            ExecutorService executor = Executors.newSingleThreadExecutor();

            try {
                while (running) {
                    // Registration events
                    synchronized (this) {
                        if (pendingRegistration != null) {
                            pendingRegistration.getSocketChannel().register(selector, SelectionKey.OP_WRITE, pendingRegistration);
                            pendingRegistration = null;
                        }
                    }

                    // Write interest events
                    synchronized (this) {
                        if (writeInterest != null) {
                            // Tell the selector we want to know when we can write again
                            SelectionKey selectionKey = writeInterest.getSocketChannel().keyFor(selector);
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                            writeInterest = null;
                        }
                    }

                    // Wait for socket(s) to report being ready for requested events
                    //long selectStart = System.currentTimeMillis();
                    int keyCount = selector.select();
                    if (keyCount == 0) {
                        continue;
                    }
                    //long selectEnd = System.currentTimeMillis();
                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    while (selectionKeys.hasNext()) {
                        SelectionKey selectionKey = selectionKeys.next();
                        selectionKeys.remove();

                        if (selectionKey.isWritable()) {
                            //System.out.println("select() took [" + (selectEnd - selectStart) + "] milliseconds");
                            selectionKey.interestOps(0);
                            Connection connection = (Connection) selectionKey.attachment();
                            executor.execute(connection);
                        } else {
                            throw new IOException("Key not writeable");
                        }
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            } finally {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                executor.shutdown();
            }
        }

        public void register(Connection connection) {
            synchronized (this) {
                pendingRegistration = connection;
            }
            selector.wakeup();
        }

        public void writeInterest(Connection connection) {
            synchronized (this) {
                writeInterest = connection;
            }
            selector.wakeup();
        }
        public Selector getSelector() {
            return selector;
        }

        public void stop() {
            running = false;
            selector.wakeup();
        }
    }


    public static class Connection implements Runnable {

        private final ByteBuffer data = ByteBuffer.allocate(8192);

        private final SocketChannel socketChannel;
        private final Poller poller;

        private volatile int total = 0;

        public Connection(SocketChannel socketChannel, Poller poller) {
            this.socketChannel = socketChannel;
            this.poller = poller;

            // Data
            while (data.hasRemaining()) {
                data.put((byte) 'A');
            }
            data.flip();
        }

        @Override
        public void run() {
            try {
                int thisWrite;
                do {
                    if (!data.hasRemaining()) {
                        data.clear();
                    }
                    thisWrite = socketChannel.write(data);

                    if (thisWrite < 0 || total >= 10 * 1024 * 1024) {
                        socketChannel.close();
                        poller.stop();
                        return;
                    }

                    total += thisWrite;
                    //System.out.println("This write [" + thisWrite + "], total [" + total + "]");
                } while (thisWrite > 0);

                poller.writeInterest(this);
            } catch (IOException e) {
                e.printStackTrace(System.out);
            } catch (BufferOverflowException e) {
                e.printStackTrace(System.out);
            }
        }

        public SocketChannel getSocketChannel() {
            return socketChannel;
        }
    }
}

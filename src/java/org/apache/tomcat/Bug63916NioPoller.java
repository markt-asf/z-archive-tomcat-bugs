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
                System.out.println("Selector provider: " + SelectorProvider.provider());
                System.out.println("Default send buffer size is [" + socketChannel.socket().getSendBufferSize() + "]");
                if (sendBufferSize != -1) {
                    socketChannel.socket().setSendBufferSize(sendBufferSize);
                }

                Connection connection = new Connection(socketChannel, poller);

                long startTime = System.currentTimeMillis();

                // Register the newly opened connection with the Poller
                poller.register(connection);

                pollerThread.join();

                long endTime = System.currentTimeMillis();

                System.out.println("Writing 10MB took [" + (endTime - startTime) + "] milliseconds");
            } catch (IOException | InterruptedException e) {
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
                        poller.stop();
                        return;
                    }

                    total += thisWrite;
                    //System.out.println("This write [" + thisWrite + "], total [" + total + "]");
                } while (thisWrite > 0);

                poller.writeInterest(this);
            } catch (IOException | BufferOverflowException e) {
                e.printStackTrace(System.out);
            }
        }

        public SocketChannel getSocketChannel() {
            return socketChannel;
        }
    }
}

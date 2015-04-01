package org.cucumbergrid.junit.runtime.node;

import org.cucumbergrid.junit.runner.CucumberGridNode;

import java.net.ConnectException;
import java.net.SocketOptions;
import java.util.Iterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class CucumberGridClient implements Runnable {

    private Selector selector;
    private SocketChannel channel;
    private ByteBuffer readSizeBuffer = ByteBuffer.allocate(4);
    private CucumberGridClientHandler handler;
    private String hubAddress;
    private int port;
    private int selectTimeout;
    private int connectTimeout;
    private int maxRetries;
    private int retries;

    public CucumberGridClient(CucumberGridNode config) {
        this(config.hub(), config.port(), config.selectTimeout(), config.connectTimeout(), config.maxRetries());
    }

    public CucumberGridClient(String hubAddress, int port, int selectTimeout, int connectTimeout, int maxRetries) {
        this.hubAddress = hubAddress;
        this.port = port;
        this.selectTimeout = selectTimeout;
        this.connectTimeout = connectTimeout;
        this.maxRetries = maxRetries;
    }

    public void init() {
        try {
            tryConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void tryConnect() throws IOException {
        tryConnect(0);
    }

    private void tryConnect(int delay) throws IOException {
        if (retries++ > maxRetries) {
            System.out.println("Max retries achieved");
            return;
        }
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        System.out.println("Trying to connect to " + hubAddress + ":" + port);
        selector = Selector.open();
        channel = SocketChannel.open();
        channel.configureBlocking(false);

        channel.register(selector, SelectionKey.OP_CONNECT);
        channel.connect(new InetSocketAddress(hubAddress, port));
    }

    public void setHandler(CucumberGridClientHandler handler) {
        this.handler = handler;
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        if (channel.isConnectionPending()) {
            System.out.println("Connection pending...");
            try {
                channel.finishConnect();
            } catch (ConnectException e) {
                // ignore
                channel.close();
                tryConnect(connectTimeout);
                return;
            }
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Connected");
    }



    public void process() {
        try {
            selector.select(selectTimeout);

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) continue;

                if (key.isConnectable()) {
                    handleConnect(key);
                }

                if (key.isValid() && key.isReadable()) {
                    handleRead(key);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (channel.isOpen()) {
            process();
        }
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();


        readSizeBuffer.clear();
        int read = channel.read(readSizeBuffer);
        if (read > 0) {
            readSizeBuffer.flip();
            int msgSize = readSizeBuffer.getInt();
            ByteBuffer buffer = ByteBuffer.allocate(msgSize);
            do {
                read = channel.read(buffer);
            } while (read == 0);
            if (read != msgSize) {
                System.out.println("Read different buffer size: " + read + " " + msgSize);
                return;
            }
            buffer.flip();
            byte[] data = new byte[msgSize];
            buffer.get(data);

            onDataReceived(key, data);
        }
        if (read < 0) {
            // nothing to read
            // close channel
            //msg = key.attachment()+" left the chat.\n";
            System.out.println("nothing to read");
            channel.close();
        }
    }

    private void onDataReceived(SelectionKey key, byte[] data) {
        if (handler != null) {
            handler.onDataReceived(key, data);
        }
    }

    public void send(byte[] data) throws IOException {
        send(ByteBuffer.wrap(data));
    }

    public void send(ByteBuffer buffer) throws IOException {
        //TODO: find a better way to do this and optmize memory
        ByteBuffer size = ByteBuffer.allocate(4);
        size.putInt(buffer.limit());
        size.flip();
        channel.write(size);
        channel.write(buffer);
    }

    public boolean isConnectionPending() {
        return channel.isConnectionPending();
    }

    public boolean isConnected() {
        return channel.isConnected();
    }

    public void shutdown() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

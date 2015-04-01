package org.cucumbergrid.junit.runtime.hub;

import org.cucumbergrid.junit.runner.CucumberGridHub;

import java.util.Iterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class CucumberGridServer implements Runnable {

    public final static String ADDRESS = "127.0.0.1";

    private int port;
    private int selectTimeout = 1000;
    private ServerSocketChannel serverChannel;
    private Selector selector;
    private ByteBuffer readSizeBuffer = ByteBuffer.allocate(4);
    private CucumberGridServerHandler handler;

    public CucumberGridServer(int port) {
        this.port = port;
    }

    public CucumberGridServer(int port, int selectTimeout) {
        this.port = port;
        this.selectTimeout = selectTimeout;
    }

    public CucumberGridServer(CucumberGridHub config) {
        this(config.port(), config.selectTimeout());
    }

    public void setHandler(CucumberGridServerHandler handler) {
        this.handler = handler;
    }

    public void init() {
        System.out.println("initializing server");

        if (selector != null) return;
        if (serverChannel != null) return;

        try {
            selector = Selector.open();
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(ADDRESS, port));

            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (serverChannel.isOpen()) {
            process();
        }
    }

    public void process() {
        try {
            selector.select(selectTimeout);

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                // remove the key so that we don't process this operation again.
                keys.remove();

                // key could be invalid if for example, the client closed the connection.
                if (!key.isValid()) {
                    System.out.println("invalid");
                    continue;
                }

                if (key.isAcceptable()) handleAccept(key);
                if (key.isReadable()) handleRead(key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
        String address = sc.socket().getInetAddress().toString() + ":" + sc.socket().getPort();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_READ, address);
        System.out.println("New connection from: " + address);
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

    public void shutdown() {
        if (selector != null) {
            try {
                selector.close();
                serverChannel.socket().close();
                serverChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(SelectionKey key, byte[] data) throws IOException {
        send((SocketChannel) key.channel(), ByteBuffer.wrap(data));
    }

    public void send(SocketChannel channel, byte[] data) throws IOException {
        send(channel, ByteBuffer.wrap(data));
    }

    public void send(SocketChannel channel, ByteBuffer buffer) throws IOException {
        //TODO: find a better way to do this and optmize memory
        ByteBuffer size = ByteBuffer.allocate(4);
        size.putInt(buffer.limit());
        size.flip();
        channel.write(size);
        channel.write(buffer);
    }

    public void sendToOthers(SocketChannel channel, byte[] data) throws IOException {
        sendToOthers(channel, ByteBuffer.wrap(data));
    }

    public void sendToOthers(SocketChannel channel, ByteBuffer buffer) throws IOException {
        SelectionKey myKey = channel.keyFor(selector);

        for (SelectionKey key : selector.keys()) {
            if (myKey == key) continue;

            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel other = (SocketChannel) key.channel();
                send(other, buffer);
                buffer.rewind();
            }
        }
    }

    public void broadcast(byte[] data) throws IOException {
        broadcast(ByteBuffer.wrap(data));
    }

    public void broadcast(ByteBuffer buffer) throws IOException {
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel channel = (SocketChannel) key.channel();
                send(channel, buffer);
                buffer.rewind();
            }
        }
    }

    public static void main(String[] args) throws IOException{
        final CucumberGridServer server = new CucumberGridServer(8511);
        server.setHandler(new CucumberGridServerHandler() {
            @Override
            public void onDataReceived(SelectionKey key, byte[] data) {
                String msg = new String(data);
                System.out.println("Data received from " + key.attachment() + ": " + msg);
                if (msg.startsWith("broadcast")) {
                    msg = msg.replace("broadcast ", "");
                    try {
                        server.broadcast(msg.getBytes());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        server.init();
        new Thread(server).start();
    }
}

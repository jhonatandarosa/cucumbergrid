package org.cucumbergrid.junit.runtime.node;

import java.util.Iterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author Jhonatan da Rosa <br>
 *         Dígitro - 26/03/15 <br>
 *         <a href="mailto:jhonatan.rosa@digitro.com.br">jhonatan.rosa@digitro.com.br</a>
 */
public class CucumberGridClient implements Runnable {

    private Selector selector;
    private SocketChannel channel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(256);
    private CucumberGridClientHandler handler;
    private String hubAddress;
    private int port;

    public CucumberGridClient(String hubAddress, int port) {
        this.hubAddress = hubAddress;
        this.port = port;
    }

    public void init() {
        try {
            selector = Selector.open();
            channel = SocketChannel.open();
            channel.configureBlocking(false);

            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(new InetSocketAddress(hubAddress, port));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setHandler(CucumberGridClientHandler handler) {
        this.handler = handler;
    }

    private void handleConnect(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        if (channel.isConnectionPending()) {
            channel.finishConnect();
        }
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        System.out.println("Connected");
    }

    public void process() {
        try {
            selector.select(1000);

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) continue;

                if (key.isConnectable()) {
                    System.out.println("I am connected to the server");
                    handleConnect(key);
                }

                if (key.isReadable()) {
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        readBuffer.clear();
        int read = 0;
        while ((read = channel.read(readBuffer)) > 0) {
            readBuffer.flip();
            byte[] bytes = new byte[readBuffer.limit()];
            readBuffer.get(bytes);
            baos.write(bytes);
            readBuffer.clear();
        }
        if (read < 0) {
            // nothing to read
            // close channel
            //msg = key.attachment()+" left the chat.\n";
            channel.close();
        } else {
            // msg = key.attachment()+": "+sb.toString();
            byte[] data = baos.toByteArray();

            onDataReceived(key, data);
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
        channel.write(buffer);
    }

    public boolean isConnectionPending() {
        return channel.isConnectionPending();
    }
}

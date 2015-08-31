package org.cucumbergrid.junit.utils;

import java.io.*;

public final class IOUtils {

    private static final int BUFFER_SIZE = 4096;

    private IOUtils() {}

    public static <T extends Serializable> byte[] serialize(T object) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(object);
                oos.flush();
                return baos.toByteArray();
            }
        }
    }

    public static <T extends Serializable> T deserialize(byte[] data) throws IOException {
        try(ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            try(ObjectInputStream ois = new ObjectInputStream(bais)) {
                return (T)ois.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException("Could not decode data", e);
            }
        }
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int read = -1;
        while ( (read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        out.flush();
    }

    public static String readFully(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            copy(inputStream, baos);
            return new String(baos.toByteArray());
        }
    }
}

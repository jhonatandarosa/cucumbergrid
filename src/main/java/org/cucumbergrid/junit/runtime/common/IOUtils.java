package org.cucumbergrid.junit.runtime.common;

import java.io.*;

public class IOUtils {

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
}

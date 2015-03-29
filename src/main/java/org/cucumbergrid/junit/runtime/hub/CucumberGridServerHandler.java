package org.cucumbergrid.junit.runtime.hub;

import java.nio.channels.SelectionKey;

public interface CucumberGridServerHandler {
    void onDataReceived(SelectionKey key, byte[] data);
}

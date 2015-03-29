package org.cucumbergrid.junit.runtime.node;

import java.nio.channels.SelectionKey;

public interface CucumberGridClientHandler {
    void onDataReceived(SelectionKey key, byte[] data);

}

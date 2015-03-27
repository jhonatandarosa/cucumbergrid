package org.cucumbergrid.junit.runtime.node;

import java.nio.channels.SelectionKey;

/**
 * @author Jhonatan da Rosa <br>
 *         Dígitro - 26/03/15 <br>
 *         <a href="mailto:jhonatan.rosa@digitro.com.br">jhonatan.rosa@digitro.com.br</a>
 */
public interface CucumberGridClientHandler {
    void onDataReceived(SelectionKey key, byte[] data);
}

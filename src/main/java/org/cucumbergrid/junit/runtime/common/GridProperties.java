package org.cucumbergrid.junit.runtime.common;

/**
 * @author Jhonatan da Rosa <br>
 *         Dígitro - 27/08/15 <br>
 *         <a href="mailto:jhonatan.rosa@digitro.com.br">jhonatan.rosa@digitro.com.br</a>
 */
public final class GridProperties {

    private GridProperties() {
    }

    public static String getGridId() {
        return System.getProperty("cucumber.grid.id");
    }
}

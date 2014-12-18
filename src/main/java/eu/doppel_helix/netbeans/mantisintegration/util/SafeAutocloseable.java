
package eu.doppel_helix.netbeans.mantisintegration.util;

public interface SafeAutocloseable extends AutoCloseable {

    @Override
    public void close();
    
}

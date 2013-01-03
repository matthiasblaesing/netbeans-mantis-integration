
package eu.doppel_helix.netbeans.mantisintegration.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class VersionTest {
    
    public VersionTest() {
    }

    @Test
    public void testCorrectExpansion() {
        Version v = new Version("1.2");
        assertEquals((int) 1, (int) v.getVersionParts().get(0));
        assertEquals((int) 2, (int) v.getVersionParts().get(1));
    }
    
    @Test
    public void testCorrectComparison() {
        Version v1 = new Version("1.20");
        Version v2 = new Version("1.3");
        List<Version> versions = new ArrayList<Version>();
        versions.add(v1);
        versions.add(v2);
        Collections.sort(versions);
        assertEquals(v2, versions.get(0));
        assertEquals(v1, versions.get(1));
    }
    
    @Test
    public void testBrokenVersion() {
        Version v1 = new Version("1.20-SNAPSHOT");
        assertEquals((int) 1, (int) v1.getVersionParts().get(0));
        assertEquals((int) 20, (int) v1.getVersionParts().get(1));
        assertEquals(v1.getVersionParts().size(), 2);
    }
}


package eu.doppel_helix.netbeans.mantisintegration.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Version implements Comparable<Version> {
    private final static Logger logger = Logger.getLogger(Version.class.getName());
    List<Integer> levels = new ArrayList<>();
    String versionString;

    public Version(String versionString) {
        this.versionString = versionString;
        String[] versionParts = versionString.split("\\D+");
        for(String versionPart: versionParts) {
            try {
                Integer part = Integer.valueOf(versionPart);
                levels.add(part);
            } catch (NumberFormatException ex) {
                logger.log(Level.INFO, "Failed to parse part of version string: {1} ({0})",
                        new Object[] {versionString, versionPart});
            }
        }
    }

    public String getVersionString() {
        return versionString;
    }

    public List<Integer> getVersionParts() {
        return Collections.unmodifiableList(levels);
    }

    @Override
    public int compareTo(Version o) {
        for(int i = 0; i < Math.max(levels.size(), o.levels.size()); i++) {
            Integer i1 = levels.size() > i ? levels.get(i) : 0;
            Integer i2 = o.levels.size() > i ? o.levels.get(i) : 0;
            if(i1 == null) {
                i1 = 0;
            }
            if(i2 == null) {
                i2 = 0;
            }
            if( ! i1.equals(i2) ) {
                return i1.compareTo(i2);
            }
        }
        return 0;
    }

    @Override
    public String toString() {
        return levels.toString() + " (" + versionString + ")";
    }
}

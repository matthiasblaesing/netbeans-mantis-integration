
package eu.doppel_helix.netbeans.mantisintegration.repository;

import eu.doppel_helix.netbeans.mantisintegration.issue.MantisIssue;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author matthias
 */
// @todo Implement a sane implementation (on-disk!)
public class IssueCache {
    private final int CAPACITY = 200;

    private final Map<BigInteger,MantisIssue> cache = new LinkedHashMap<BigInteger, MantisIssue>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<BigInteger, MantisIssue> eldest) {
            return size() > CAPACITY;
        }

    };

    public synchronized MantisIssue getIssue(BigInteger id) {
        return cache.get(id);
    }

    public synchronized void setIssueData(BigInteger id, MantisIssue mi) {
        cache.put(id, mi);
    }
}

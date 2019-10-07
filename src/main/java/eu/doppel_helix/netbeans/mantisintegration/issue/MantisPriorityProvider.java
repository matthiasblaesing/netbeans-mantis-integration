
package eu.doppel_helix.netbeans.mantisintegration.issue;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.bugtracking.spi.IssuePriorityInfo;
import org.netbeans.modules.bugtracking.spi.IssuePriorityProvider;
import org.openide.util.ImageUtilities;

public class MantisPriorityProvider implements IssuePriorityProvider<MantisIssue>{
    private final static Map<String,IssuePriorityInfo> issuePriorityMap;
    private final static IssuePriorityInfo[] issuePriorities;
    private final static Image fallback = ImageUtilities.loadImage("eu/doppel_helix/netbeans/mantisintegration/issue/priority_0.gif", false);
    static {
        issuePriorities = new IssuePriorityInfo[] {
            new IssuePriorityInfo("10", "none", ImageUtilities.loadImage("eu/doppel_helix/netbeans/mantisintegration/issue/priority_0.gif", false)),
            new IssuePriorityInfo("20", "low", ImageUtilities.loadImage("eu/doppel_helix/netbeans/mantisintegration/issue/priority_low_1.gif", false)),
            new IssuePriorityInfo("30", "normal", ImageUtilities.loadImage("eu/doppel_helix/netbeans/mantisintegration/issue/priority_normal.gif", false)),
            new IssuePriorityInfo("40", "high", ImageUtilities.loadImage("eu/doppel_helix/netbeans/mantisintegration/issue/priority_1.gif", false)),
            new IssuePriorityInfo("50", "urgent", ImageUtilities.loadImage("eu/doppel_helix/netbeans/mantisintegration/issue/priority_2.gif", false)),
            new IssuePriorityInfo("60", "immediate", ImageUtilities.loadImage("eu/doppel_helix/netbeans/mantisintegration/issue/priority_3.gif", false)),
        };
        issuePriorityMap = new HashMap<>();
        for(IssuePriorityInfo ipi: issuePriorities) {
            issuePriorityMap.put(ipi.getID(), ipi);
        }
    }

    public Image getImageById(String id) {
        try {
            return issuePriorityMap.get(id).getIcon();
        } catch (NullPointerException ex) {
            return fallback;
        }
    }

    @Override
    public String getPriorityID(MantisIssue i) {
        return i.getPriority().getId().toString();
    }

    @Override
    public IssuePriorityInfo[] getPriorityInfos() {
        return issuePriorities;
    }
}

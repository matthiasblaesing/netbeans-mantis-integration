
package eu.doppel_helix.netbeans.mantisintegration.repository;

import biz.futureware.mantisconnect.ProjectVersionData;
import java.util.Comparator;

public class ProjectVersionDataComparator implements Comparator<ProjectVersionData>{

    @Override
    public int compare(ProjectVersionData o1, ProjectVersionData o2) {
        String obsolete1 = (o1.getObsolete() != null && o1.getObsolete()) ? "1" : "0";
        String obsolete2 = (o2.getObsolete() != null && o2.getObsolete()) ? "1" : "0";
        String release1 = (o1.getReleased()!= null && o1.getReleased()) ? "1" : "0";
        String release2 = (o2.getReleased() != null && o2.getReleased()) ? "1" : "0";
        String name1 = o1.getName() != null ? o1.getName() : "";
        String name2 = o2.getName() != null ? o2.getName() : "";
        String referenz1 = obsolete1 + release1 + name1;
        String referenz2 = obsolete2 + release2 + name2;
        return referenz1.compareToIgnoreCase(referenz2);
    }

}

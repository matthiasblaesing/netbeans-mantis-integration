
package eu.doppel_helix.netbeans.mantisintegration.data;

import biz.futureware.mantisconnect.ProjectData;
import java.util.ArrayList;
import java.util.List;

public class FlattenedProjectData {
    private biz.futureware.mantisconnect.ProjectData projectData;
    private int level;

    public FlattenedProjectData(biz.futureware.mantisconnect.ProjectData projectData, int level) {
        this.projectData = projectData;
        this.level = level;
    }

    public biz.futureware.mantisconnect.ProjectData getProjectData() {
        return projectData;
    }

    public void setProjectData(biz.futureware.mantisconnect.ProjectData projectData) {
        this.projectData = projectData;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "NestedProjectData{" + "projectData=" + projectData + ", level=" + level + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.projectData != null ? this.projectData.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlattenedProjectData other = (FlattenedProjectData) obj;
        if (this.projectData != other.projectData && (this.projectData == null || !this.projectData.equals(other.projectData))) {
            return false;
        }
        return true;
    }
    
    /** 
     * Flatten the supplied ProjectData and Subprojects
     */
    public static List<FlattenedProjectData> buildList(ProjectData pd) {
        List<FlattenedProjectData> result = new ArrayList<FlattenedProjectData>();
        buildList(pd, 0, result);
        return result;
    }
    
    private static void buildList(ProjectData pd, int level, List<FlattenedProjectData> result) {
        result.add(new FlattenedProjectData(pd, level));
        for(ProjectData pdi: pd.getSubprojects()) {
            buildList(pdi, level + 1, result);
        }
    }
}

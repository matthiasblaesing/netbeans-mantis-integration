
package eu.doppel_helix.netbeans.mantisintegration.repository;

import biz.futureware.mantisconnect.ObjectRef;
import eu.doppel_helix.netbeans.mantisintegration.data.Version;

class ConnectionCheckResult {
    private Version version = null;
    private ObjectRef[] statusList = new ObjectRef[0];
    private ObjectRef[] resolutionList = new ObjectRef[0];

    public ConnectionCheckResult() {
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public ObjectRef[] getStatusList() {
        return statusList;
    }

    public void setStatusList(ObjectRef[] statusList) {
        this.statusList = statusList;
    }

    public ObjectRef[] getResolutionList() {
        return resolutionList;
    }

    public void setResolutionList(ObjectRef[] resolutionList) {
        this.resolutionList = resolutionList;
    }
    
}

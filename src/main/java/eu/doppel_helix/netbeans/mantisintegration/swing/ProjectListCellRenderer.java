
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.ProjectData;
import eu.doppel_helix.netbeans.mantisintegration.data.FlattenedProjectData;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class ProjectListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if(value instanceof ProjectData) {
            value = ((ProjectData)value).getName();
        } else if ( value instanceof FlattenedProjectData) {
            FlattenedProjectData npd = (FlattenedProjectData) value;
            StringBuilder sb = new StringBuilder();
            for(int i = 0; i < npd.getLevel(); i++) {
                sb.append("\u00BB ");
            }
            sb.append(npd.getProjectData().getName());
            value = sb.toString();
        } else if (value == null) {
            value = " ";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
    
}

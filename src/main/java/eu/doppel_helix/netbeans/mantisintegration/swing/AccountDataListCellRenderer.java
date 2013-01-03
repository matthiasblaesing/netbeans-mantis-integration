
package eu.doppel_helix.netbeans.mantisintegration.swing;

import biz.futureware.mantisconnect.AccountData;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class AccountDataListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        if(value instanceof AccountData) {
            AccountData ac = (AccountData) value;
            value = ac.getName();
            if(ac.getReal_name() != null) {
                value += " - " + ac.getReal_name();
            }
        } else if (value == null) {
            value = " ";
        }
        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
    
}

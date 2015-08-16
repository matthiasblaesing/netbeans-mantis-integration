
package eu.doppel_helix.netbeans.mantisintegration.swing;

import java.awt.Color;
import javax.swing.UIManager;

public class UIUtils {
    // The color accessors are copied here for compatibility 
    public static Color getTaskNewColor () {
        Color c = UIManager.getColor("nb.bugtracking.new.color"); //NOI18N
        if (c == null) {
            c = new Color(0, 180, 0);
        }
        return c;
    }

    public static Color getTaskModifiedColor () {
        Color c = UIManager.getColor("nb.bugtracking.modified.color"); //NOI18N
        if (c == null) {
            c = new Color(0, 0, 255);
        }
        return c;
    }

    public static Color getTaskConflictColor () {
        Color c = UIManager.getColor("nb.bugtracking.conflict.color"); //NOI18N
        if (c == null) {
            c = new Color(255, 0, 0);
        }
        return c;
    }

    public static Color getTaskObsoleteColor () {
        Color c = UIManager.getColor("nb.bugtracking.obsolete.color"); //NOI18N
        if (c == null) {
            c = new Color(153, 153, 153);
        }
        return c;
    }
}

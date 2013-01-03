
package eu.doppel_helix.netbeans.mantisintegration.swing;

import java.awt.Dimension;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

public class DirectionalEditorPane extends JTextPane {
    boolean trackViewportWidth = false;
    boolean trackViewportHeight = false;

    public DirectionalEditorPane() {
    }

    public DirectionalEditorPane(StyledDocument doc) {
        super(doc);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return super.getPreferredScrollableViewportSize();
    }

    public void setScrollableTracksViewportWidth(boolean trackViewportWidth) {
        boolean oldValue = trackViewportWidth;
        this.trackViewportWidth = trackViewportWidth;
        firePropertyChange("scrollableTracksViewportWidth", oldValue, trackViewportWidth);
    }

    public void setScrollableTracksViewportHeight(boolean trackViewportHeight) {
        boolean oldValue = trackViewportHeight;
        this.trackViewportHeight = trackViewportHeight;
        firePropertyChange("scrollableTracksViewportWidth", oldValue, trackViewportHeight);
    }
    
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return trackViewportWidth;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return trackViewportHeight;
    }
}
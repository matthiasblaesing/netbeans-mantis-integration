
package eu.doppel_helix.netbeans.mantisintegration.swing;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class DispatchingListener implements MouseListener {
    private final Component delegate;

    public DispatchingListener(Component delegate) {
        this.delegate = delegate;
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
        e.setSource(delegate);
        delegate.dispatchEvent(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        e.setSource(delegate);
        delegate.dispatchEvent(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        e.setSource(delegate);
        delegate.dispatchEvent(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        e.setSource(delegate);
        delegate.dispatchEvent(e);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.setSource(delegate);
        delegate.dispatchEvent(e);
    }
    
}

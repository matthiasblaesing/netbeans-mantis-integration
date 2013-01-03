
package eu.doppel_helix.netbeans.mantisintegration;

import javax.imageio.ImageIO;
import org.openide.modules.ModuleInstall;

public class Installer extends ModuleInstall {

    @Override
    public void restored() {
        // Make sure the ICO reader added to the classpath by this module
        // is correctly recognized
        ImageIO.scanForPlugins();
    }
}

package org.kaleta.lolstats.frontend;

import java.io.File;
import java.util.logging.Logger;

/**
 * Created by Stanislav Kaleta on 11.02.2016.
 *
 * Performs initialization of this app. Includes data and resources checks, app. wide constants and default logger.
 */
public class Initializer {
    public static final String NAME = "LolStats";
    public static final String VERSION = "2.0";
    public static final String DATA_SOURCE = new File(Initializer.class.getProtectionDomain()
            .getCodeSource().getLocation().getPath())/**/.getParentFile().getPath()
            + "/" + NAME + "-" + VERSION + "-DATA/";
    public static final Logger LOG = Logger.getLogger("Logger");


    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    new AppFrame().setVisible(true);

                    //new ShowLpGraph(new AppFrame()).actionPerformed(null);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }



}
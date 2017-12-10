package cardTools;

import applet.MainApplet;

/**
 *
 * @author Petr Svenda
 */
public class RunConfig {
    int targetReaderIndex = 0;
    public int numRepeats = 1;
    public Class appletToSimulate;
    boolean bReuploadApplet = false;
    byte[] installData = null;
    
    public enum CARD_TYPE {
        PHYSICAL, JCOPSIM, JCARDSIMLOCAL, JCARDSIMREMOTE
    }
    public CARD_TYPE testCardType = CARD_TYPE.PHYSICAL;
    
    public static RunConfig getDefaultConfig() {
        RunConfig runCfg = new RunConfig();
        runCfg.targetReaderIndex = 0;
        runCfg.testCardType = CARD_TYPE.PHYSICAL;
        runCfg.appletToSimulate = MainApplet.class;
        
        return runCfg;
    }
}

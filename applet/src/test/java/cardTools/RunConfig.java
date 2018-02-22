package cardTools;

/**
 * Applet run configuration.
 *
 * @author Petr Svenda, Dusan Klinec
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
        runCfg.appletToSimulate = null;
        
        return runCfg;
    }

    public int getTargetReaderIndex() {
        return targetReaderIndex;
    }

    public int getNumRepeats() {
        return numRepeats;
    }

    public Class getAppletToSimulate() {
        return appletToSimulate;
    }

    public boolean isbReuploadApplet() {
        return bReuploadApplet;
    }

    public byte[] getInstallData() {
        return installData;
    }

    public CARD_TYPE getTestCardType() {
        return testCardType;
    }

    public RunConfig setTargetReaderIndex(int targetReaderIndex) {
        this.targetReaderIndex = targetReaderIndex;
        return this;
    }

    public RunConfig setNumRepeats(int numRepeats) {
        this.numRepeats = numRepeats;
        return this;
    }

    public RunConfig setAppletToSimulate(Class appletToSimulate) {
        this.appletToSimulate = appletToSimulate;
        return this;
    }

    public RunConfig setbReuploadApplet(boolean bReuploadApplet) {
        this.bReuploadApplet = bReuploadApplet;
        return this;
    }

    public RunConfig setInstallData(byte[] installData) {
        this.installData = installData;
        return this;
    }

    public RunConfig setTestCardType(CARD_TYPE testCardType) {
        this.testCardType = testCardType;
        return this;
    }
}

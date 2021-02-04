package cardTools;

import com.licel.jcardsim.smartcardio.CardSimulator;
import javacard.framework.Applet;

/**
 * Applet run configuration.
 *
 * @author Petr Svenda, Dusan Klinec
 */
public class RunConfig {
    int targetReaderIndex = 0;
    public int numRepeats = 1;
    public Class<? extends Applet> appletToSimulate;
    boolean bReuploadApplet = false;
    byte[] installData = null;
    byte[] aid = null;
    CardSimulator simulator = null;
    String remoteAddress;
    boolean remoteDisconnectPrevious = false;

    public enum CARD_TYPE {
        PHYSICAL, JCOPSIM, JCARDSIMLOCAL, JCARDSIMREMOTE, PHYSICAL_JAVAX, REMOTE
    }

    public CARD_TYPE testCardType = CARD_TYPE.PHYSICAL;
    public CARD_TYPE remoteCardType = CARD_TYPE.PHYSICAL;

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

    public RunConfig setAppletToSimulate(Class<? extends Applet> appletToSimulate) {
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

    public byte[] getAid() {
        return aid;
    }

    public RunConfig setAid(byte[] aid) {
        this.aid = aid;
        return this;
    }

    public CardSimulator getSimulator() {
        return simulator;
    }

    public RunConfig setSimulator(CardSimulator simulator) {
        this.simulator = simulator;
        return this;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public RunConfig setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    public CARD_TYPE getRemoteCardType() {
        return remoteCardType;
    }

    public RunConfig setRemoteCardType(CARD_TYPE remoteCardType) {
        this.remoteCardType = remoteCardType;
        return this;
    }

    public boolean isRemoteDisconnectPrevious() {
        return remoteDisconnectPrevious;
    }

    public RunConfig setRemoteDisconnectPrevious(boolean remoteDisconnectPrevious) {
        this.remoteDisconnectPrevious = remoteDisconnectPrevious;
        return this;
    }
}

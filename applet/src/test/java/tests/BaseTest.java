package tests;

import applet.MainApplet;
import cardTools.CardManager;
import cardTools.RunConfig;
import cardTools.Util;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;

/**
 * Base Test class.
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author Petr Svenda, Dusan Klinec (ph4r05)
 */
public class BaseTest {
    private static String APPLET_AID = "0102030405060708090102";
    private static byte APPLET_AID_BYTE[] = Util.hexStringToByteArray(APPLET_AID);

    protected RunConfig.CARD_TYPE cardType = RunConfig.CARD_TYPE.JCARDSIMLOCAL;

    protected boolean simulateStateful = false;
    protected CardManager statefulCard = null;

    public BaseTest() {

    }

    /**
     * Creates card manager and connects to the card.
     *
     * @return
     * @throws Exception
     */
    public CardManager connect() throws Exception {
        return connect(null);
    }

    public CardManager connect(byte[] installData) throws Exception {
        if (simulateStateful && statefulCard != null){
            return statefulCard;
        } else if (simulateStateful){
            statefulCard = connectRaw(installData);
            return statefulCard;
        }

        return connectRaw(installData);
    }

    public CardManager connectRaw(byte[] installData) throws Exception {
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();
        System.setProperty("com.licel.jcardsim.object_deletion_supported", "1");
        System.setProperty("com.licel.jcardsim.sign.dsasigner.computedhash", "1");

        // Running on physical card
        if (cardType == RunConfig.CARD_TYPE.PHYSICAL) {
            runCfg.setTestCardType(RunConfig.CARD_TYPE.PHYSICAL);
        } else {
            // Running in the simulator
            runCfg.setAppletToSimulate(MainApplet.class)
                    .setTestCardType(RunConfig.CARD_TYPE.JCARDSIMLOCAL)
                    .setbReuploadApplet(true)
                    .setInstallData(installData);
        }

        if (!cardMngr.Connect(runCfg)) {
            throw new RuntimeException("Connection failed");
        }

        return cardMngr;
    }

    /**
     * Convenience method for connecting and sending
     * @param cmd
     * @return
     */
    public ResponseAPDU connectAndSend(CommandAPDU cmd) throws Exception {
        return connect().transmit(cmd);
    }

    /**
     * Convenience method for building APDU command
     * @param data
     * @return
     */
    public static CommandAPDU buildApdu(String data){
        return new CommandAPDU(Util.hexStringToByteArray(data));
    }

    /**
     * Convenience method for building APDU command
     * @param data
     * @return
     */
    public static CommandAPDU buildApdu(byte[] data){
        return new CommandAPDU(data);
    }

    /**
     * Convenience method for building APDU command
     * @param data
     * @return
     */
    public static CommandAPDU buildApdu(CommandAPDU data){
        return data;
    }

    /**
     * Sending command to the card.
     * Enables to send init commands before the main one.
     *
     * @param cardMngr
     * @param command
     * @param initCommands
     * @return
     * @throws CardException
     */
    public ResponseAPDU sendCommandWithInitSequence(CardManager cardMngr, String command, ArrayList<String> initCommands) throws CardException {
        if (initCommands != null) {
            for (String cmd : initCommands) {
                cardMngr.getChannel().transmit(buildApdu(cmd));
            }
        }

        final ResponseAPDU resp = cardMngr.getChannel().transmit(buildApdu(command));
        return resp;
    }

    public RunConfig.CARD_TYPE getCardType() {
        return cardType;
    }

    public BaseTest setCardType(RunConfig.CARD_TYPE cardType) {
        this.cardType = cardType;
        return this;
    }

    public boolean isSimulateStateful() {
        return simulateStateful;
    }

    public BaseTest setSimulateStateful(boolean simulateStateful) {
        this.simulateStateful = simulateStateful;
        return this;
    }

    public boolean isPhysical() {
        return cardType == RunConfig.CARD_TYPE.PHYSICAL;
    }

    public boolean isStateful(){
        return isPhysical() || simulateStateful;
    }

    public boolean canReinstall(){
        return !isPhysical() && !simulateStateful;
    }
}

package cardTools;

import applet.MainApplet;

import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;

/**
 * Test class.
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author Petr Svenda, Dusan Klinec (ph4r05)
 */
public class SimpleAPDU {
    private static String APPLET_AID = "482871d58ab7465e5e05";
    private static byte APPLET_AID_BYTE[] = Util.hexStringToByteArray(APPLET_AID);

    private static final String STR_APDU_DUMMY = "00C00000080000000000000000";

    /**
     * Main entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            demoSingleCommand();
        } catch (Exception ex) {
            System.out.println("Exception : " + ex);
        }
    }

    public static void demoSingleCommand() throws Exception {
        final CardManager cardMngr = new CardManager(true, APPLET_AID_BYTE);
        final RunConfig runCfg = RunConfig.getDefaultConfig();

        // Running on physical card
        //runCfg.testCardType = RunConfig.CARD_TYPE.PHYSICAL;

        // Running in the simulator
        runCfg.appletToSimulate = MainApplet.class;
        runCfg.testCardType = RunConfig.CARD_TYPE.JCARDSIMLOCAL;
        runCfg.bReuploadApplet = true;
        runCfg.installData = new byte[8];

        System.out.print("Connecting to card...");
        if (!cardMngr.Connect(runCfg)) {
            return;
        }
        System.out.println(" Done.");

        sendCommandWithInitSequence(cardMngr, STR_APDU_DUMMY, null);
    }

    public static void sendCommandWithInitSequence(CardManager cardMngr, String command, ArrayList<String>  initCommands) throws CardException {
        if (initCommands != null) {
            for (String cmd : initCommands) {
                cardMngr.m_channel.transmit(new CommandAPDU(Util.hexStringToByteArray(cmd)));
            }
        }
        ResponseAPDU resp = cardMngr.m_channel.transmit(new CommandAPDU(Util.hexStringToByteArray(command)));
    }

}

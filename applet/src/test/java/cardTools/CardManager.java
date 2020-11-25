package cardTools;

import apdu4j.TerminalManager;
import com.licel.jcardsim.io.CAD;
import com.licel.jcardsim.io.JavaxSmartCardInterface;
import javacard.framework.AID;

import javax.smartcardio.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Petr Svenda
 */
public class CardManager {
    private final static Logger LOG = LoggerFactory.getLogger(CardManager.class);
    protected boolean bDebug = false;
    protected byte[] appletId = null;
    protected Long lastTransmitTime = (long) 0;
    protected CommandAPDU lastCommand = null;
    protected CardChannel channel = null;
    protected boolean autoSelect = true;
    protected ResponseAPDU selectResponse = null;

    /**
     * Add LC=0 byte to the APDU.
     */
    protected boolean fixLc = true;

    public CardManager(boolean bDebug, byte[] appletAID) {
        this.bDebug = bDebug;
        this.appletId = appletAID;
    }

    /**
     * Card connect
     * @param runCfg run configuration
     * @return true if connected
     * @throws Exception exceptions from underlying connects
     */
    public boolean connect(RunConfig runCfg) throws Exception {
        boolean bConnected = false;
        switch (runCfg.testCardType) {
            case PHYSICAL: {
                channel = connectPhysicalCard(runCfg.targetReaderIndex);
                break;
            }
            case PHYSICAL_JAVAX: {
                channel = connectPhysicalCardJavax(runCfg.targetReaderIndex);
                break;
            }
            case JCOPSIM: {
                channel = connectJCOPSimulator(runCfg.targetReaderIndex);
                break;
            }
            case JCARDSIMLOCAL: {
                channel = connectJCardSimLocalSimulator(runCfg.appletToSimulate, runCfg.installData);
                break;
            }
            case JCARDSIMREMOTE: {
                channel = null; // Not implemented yet
                break;
            }
            default:
                channel = null;
                bConnected = false;

        }
        if (channel != null) {
            bConnected = true;
        }
        return bConnected;
    }

    public void disconnect(boolean bReset) throws CardException {
        channel.getCard().disconnect(bReset); // Disconnect from the card
    }

    public CardChannel connectPhysicalCardJavax(int targetReaderIndex) throws Exception {
        // JCOP Simulators
        LOG.debug("Looking for physical cards... ");
        return connectTerminalAndSelect(findCardTerminalSmartcardIO(targetReaderIndex));
    }

    public CardChannel connectPhysicalCard(int targetReaderIndex) throws Exception {
        // JCOP Simulators
        LOG.debug("Looking for physical cards... ");
        return connectTerminalAndSelect(findCardTerminal(targetReaderIndex));
    }

    public CardChannel connectJCOPSimulator(int targetReaderIndex) throws Exception {
        // JCOP Simulators
        LOG.debug("Looking for JCOP simulators...");
        int[] ports = new int[]{8050};
        return connectToCardByTerminalFactory(TerminalFactory.getInstance("JcopEmulator", ports), targetReaderIndex);
    }

    public CardTerminal findCardTerminal(int targetReaderIndex) throws CardException {
        TerminalFactory tf = TerminalManager.getTerminalFactory();
        String reader = System.getenv("GP_READER");
        if (reader != null) {
            Optional<CardTerminal> t = TerminalManager.getInstance(tf.terminals()).dwim(reader, System.getenv("GP_READER_IGNORE"), Collections.emptyList());
            if (!t.isPresent()) {
                throw new RuntimeException("Reader could not be found");
            }
            return t.get();
        }

        return findTerminalIdx(tf.terminals().list(), targetReaderIndex);
    }

    public CardTerminal findCardTerminalSmartcardIO(int targetReaderIndex) throws CardException {
        TerminalFactory tf = TerminalFactory.getDefault();
        return findTerminalIdx(tf.terminals().list(), targetReaderIndex);
    }

    public CardTerminal findTerminalIdx(List<CardTerminal> terminals, int targetReaderIndex) throws CardException {
        int currIdx = -1;
        TerminalFactory tf = TerminalFactory.getDefault();
        for (CardTerminal t : terminals) {
            currIdx += 1;
            if (currIdx != targetReaderIndex){
                continue;
            }
            if (t.isCardPresent()) {
                return t;
            }
        }
        throw new RuntimeException("No card terminal found");
    }

    public CardChannel connectJCardSimLocalSimulator(Class appletClass, byte[] installData) throws Exception {
        System.setProperty("com.licel.jcardsim.terminal.type", "2");
        CAD cad = new CAD(System.getProperties());
        JavaxSmartCardInterface simulator = (JavaxSmartCardInterface) cad.getCardInterface();
        if (installData == null) {
            installData = new byte[0];
        }
        AID appletAID = new AID(appletId, (short) 0, (byte) appletId.length);

        AID appletAIDRes = simulator.installApplet(appletAID, appletClass, installData, (short) 0, (byte) installData.length);
        simulator.selectApplet(appletAID);

        return new SimulatedCardChannelLocal(simulator);
    }

    public CardChannel connectTerminalAndSelect(CardTerminal terminal) throws CardException {
        CardChannel ch = connectTerminal(terminal);

        // Select applet (mpcapplet)
        LOG.debug("Smartcard: Selecting applet...");
        selectResponse = selectApplet();

        return ch;
    }

    public CardChannel connectTerminal(CardTerminal terminal) throws CardException {
        LOG.debug("Connecting...");
        Card card = terminal.connect("*"); // Connect with the card
        if (card == null){
            return null;
        }
        LOG.debug("Terminal connected");

        LOG.debug("Establishing channel...");
        channel = card.getBasicChannel();
        LOG.debug("Channel established");

        return card.getBasicChannel();
    }

    public ResponseAPDU selectApplet() throws CardException {
        CommandAPDU cmd = new CommandAPDU(0x00, 0xa4, 0x04, 0x00, appletId);
        return transmit(cmd);
    }

    public CardChannel connectToCardByTerminalFactory(TerminalFactory factory, int targetReaderIndex) throws CardException {
        List<CardTerminal> terminals = new ArrayList<>();

        boolean card_found = false;
        CardTerminal terminal = null;
        Card card = null;
        try {
            for (CardTerminal t : factory.terminals().list()) {
                terminals.add(t);
                if (t.isCardPresent()) {
                    card_found = true;
                }
            }
        } catch (Exception e) {
            LOG.error("Terminal listing failed.", e);
        }

        if (!card_found) {
            LOG.warn("Failed to find physical card.");
            return null;
        }

        LOG.debug("Cards found: " + terminals);
        terminal = terminals.get(targetReaderIndex); // Prioritize physical card over simulations
        return connectTerminalAndSelect(terminal);
    }

    public ResponseAPDU transmit(CommandAPDU cmd)
        throws CardException {

        if (isFixLc()){
            cmd = fixApduLc(cmd);
        }

        lastCommand = cmd;
        if (bDebug) {
            log(cmd);
        }

        long elapsed = -System.currentTimeMillis();
        ResponseAPDU response = channel.transmit(cmd);
        elapsed += System.currentTimeMillis();
        lastTransmitTime = elapsed;

        if (bDebug) {
            log(response, lastTransmitTime);
        }

        return response;
    }

    public void log(CommandAPDU cmd) {
        LOG.debug(String.format("--> %s (%d B)", Util.toHex(cmd.getBytes()),
            cmd.getBytes().length));
    }

    public void log(ResponseAPDU response, long time) {
        String swStr = String.format("%02X", response.getSW());
        byte[] data = response.getData();
        if (data.length > 0) {
            LOG.debug(String.format("<-- %s %s (%d) [%d ms]", Util.toHex(data), swStr,
                data.length, time));
        } else {
            LOG.debug(String.format("<-- %s [%d ms]", swStr, time));
        }
    }

    public CommandAPDU fixApduLc(CommandAPDU cmd){
        if (cmd.getNc() != 0){
            return cmd;
        }

        byte[] apdu = new byte[] {
            (byte)cmd.getCLA(),
            (byte)cmd.getINS(),
            (byte)cmd.getP1(),
            (byte)cmd.getP2(),
            (byte)0
        };
        return new CommandAPDU(apdu);
    }

    public void log(ResponseAPDU response) {
        log(response, 0);
    }

    public Card waitForCard(CardTerminals terminals)
        throws CardException {
        while (true) {
            for (CardTerminal ct : terminals
                .list(CardTerminals.State.CARD_INSERTION)) {

                return ct.connect("*");
            }
            terminals.waitForChange();
        }
    }

    public boolean isbDebug() {
        return bDebug;
    }

    public byte[] getAppletId() {
        return appletId;
    }

    public Long getLastTransmitTime() {
        return lastTransmitTime;
    }

    public CommandAPDU getLastCommand() {
        return lastCommand;
    }

    public CardChannel getChannel() {
        return channel;
    }

    public CardManager setbDebug(boolean bDebug) {
        this.bDebug = bDebug;
        return this;
    }

    public CardManager setAppletId(byte[] appletId) {
        this.appletId = appletId;
        return this;
    }

    public CardManager setLastTransmitTime(Long lastTransmitTime) {
        this.lastTransmitTime = lastTransmitTime;
        return this;
    }

    public CardManager setLastCommand(CommandAPDU lastCommand) {
        this.lastCommand = lastCommand;
        return this;
    }

    public CardManager setChannel(CardChannel channel) {
        this.channel = channel;
        return this;
    }

    public boolean isFixLc() {
        return fixLc;
    }

    public CardManager setFixLc(boolean fixLc) {
        this.fixLc = fixLc;
        return this;
    }

    public boolean isAutoSelect() {
        return autoSelect;
    }

    public void setAutoSelect(boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    public ResponseAPDU getSelectResponse() {
        return selectResponse;
    }
}

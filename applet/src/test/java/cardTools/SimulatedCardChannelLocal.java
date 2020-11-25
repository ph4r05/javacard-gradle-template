package cardTools;

import com.licel.jcardsim.io.JavaxSmartCardInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;
import java.nio.ByteBuffer;

/**
 *
 * @author Petr Svenda
 */
public class SimulatedCardChannelLocal extends CardChannel {
    private final static Logger LOG = LoggerFactory.getLogger(SimulatedCardChannelLocal.class);

    JavaxSmartCardInterface m_simulator;
    SimulatedCard m_card;

    SimulatedCardChannelLocal (JavaxSmartCardInterface simulator) {
        m_simulator = simulator;
        m_card = new SimulatedCard();
    }

    @Override
    public Card getCard() {
        return m_card;
    }

    @Override
    public int getChannelNumber() {
        return 0;
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU apdu) throws CardException {
        ResponseAPDU responseAPDU = null;

        try {
            log(apdu);
            responseAPDU = this.m_simulator.transmitCommand(apdu);
            log(responseAPDU);
            // TODO: Add delay corresponding to real cards
            //int delay = OperationTimes.getCardOperationDelay(apdu);
            //Thread.sleep(delay);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return responseAPDU;
    }

    @Override
    public int transmit(ByteBuffer bb, ByteBuffer bb1) throws CardException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws CardException {
        m_simulator.reset();
    }


    private static void log(CommandAPDU cmd) {
        LOG.debug(String.format("--> [%s] (%s B)", Util.toHex(cmd.getBytes()), cmd.getBytes().length));
    }

    private static void log(ResponseAPDU response, long time) {
        String swStr = String.format("%02X", response.getSW());
        byte[] data = response.getData();
        if (data.length > 0) {
            LOG.debug(String.format("<-- %s %s (%d B)", Util.toHex(data), swStr,
                data.length));
        } else {
            LOG.debug(String.format("<-- %s", swStr));
        }
        if (time > 0) {
            LOG.debug(String.format("Elapsed time %d ms", time));
        }
    }

    private static void log(ResponseAPDU response) {
        log(response, 0);
    }
}

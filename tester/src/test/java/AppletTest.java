import applet.MainApplet;
import com.licel.jcardsim.base.Simulator;
import javacard.framework.AID;
import org.junit.Assert;
import org.testng.annotations.*;

import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.*;

/**
 * Example test class for the applet
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author xsvenda, Dusan Klinec (ph4r05)
 */
public class AppletTest {

    private Simulator simulator;
    private byte[] appletAIDBytes;
    private AID appletAID;
    
    public AppletTest() {
        this.simulator = new Simulator();
        this.appletAIDBytes = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        this.appletAID = new AID(this.appletAIDBytes, (short) 0, (byte) this.appletAIDBytes.length);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
        this.simulator.installApplet(appletAID, MainApplet.class);
        simulator.selectApplet(appletAID);
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        simulator.reset();
    }

    // Example test
    @Test
    public void hello() throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        // 4. Gut public key
        BigInteger exp = new BigInteger(new ResponseAPDU(simulator.transmitCommand((new CommandAPDU(0x00, 0x02, 0x00, 0x00)).getBytes())).getData());
        BigInteger mod = new BigInteger(new ResponseAPDU(simulator.transmitCommand((new CommandAPDU(0x00, 0x03, 0x00, 0x00)).getBytes())).getData());

        RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PublicKey pub = kf.generatePublic(spec);

        // 5. Sign random data
        SecureRandom random = new SecureRandom();
        byte[] challenge = new byte[64];
        random.nextBytes(challenge);
        byte[] signature = new ResponseAPDU(simulator.transmitCommand((new CommandAPDU(0x00, 0x04, 0x00, 0x00, challenge)).getBytes())).getData();

        // 6. Validate
        Signature verifier = Signature.getInstance("SHA1withRSA");
        verifier.initVerify(pub);
        verifier.update(challenge);
        Assert.assertTrue(verifier.verify(signature));
    }
}

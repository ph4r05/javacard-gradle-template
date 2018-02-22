package tests;

import org.junit.Assert;
import org.testng.annotations.*;

import javax.smartcardio.ResponseAPDU;

/**
 * Example test class for the applet
 * Note: If simulator cannot be started try adding "-noverify" JVM parameter
 *
 * @author xsvenda, Dusan Klinec (ph4r05)
 */
public class AppletTest {
    
    public AppletTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
    }

    // Example test
    @Test
    public void hello() throws Exception {
        final ResponseAPDU responseAPDU = SimpleAPDU.demoSingleCommand();
        Assert.assertNotNull(responseAPDU);
        Assert.assertEquals(0x9000, responseAPDU.getSW());
        Assert.assertNotNull(responseAPDU.getBytes());
    }
}

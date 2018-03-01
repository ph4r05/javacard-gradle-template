package applet;

import javacard.framework.*;
import javacard.security.*;

public class MainApplet extends Applet implements ISO7816 {
	private static final short MAX_LENGTH = 256;
	private static final byte[] hello = {'p','k','i','t','c','h'};

	// Keys
	private KeyPair kp;
	private Signature signature;

	// Signature scratchpad
	private byte[] scratchpad;

	protected MainApplet() {
		scratchpad = new byte[256];
		kp = new KeyPair(KeyPair.ALG_RSA, KeyBuilder.LENGTH_RSA_1024);
		kp.genKeyPair();
		signature = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
		signature.init(kp.getPrivate(), Signature.MODE_SIGN);
		register();
	}

	/**
	 * Installs this applet.
	 * @param bArray the array containing installation parameters
	 * @param bOffset the starting offset in bArray
	 * @param bLength the length in bytes of the parameter data in bArray
	 */
	public static void install(byte[] bArray, short bOffset, byte bLength){
		new MainApplet();
	}

	/**
	 * Processes an incoming APDU. Will always respond with the helloFidesmo string,
	 * regardless of what is received.
	 * @see APDU
	 * @param apdu the incoming APDU
	 * @exception ISOException with the response bytes per ISO 7816-4
	 */
	public void process(APDU apdu) {
		byte buffer[] = apdu.getBuffer();

		if (this.selectingApplet()) {
			Util.arrayCopyNonAtomic(hello, (short) 0, buffer, (short) 0, (short) hello.length);
			apdu.setOutgoingAndSend((short) 0, (short)hello.length);
			return;
		}

		switch (buffer[ISO7816.OFFSET_INS]) {
			case 0x01:
				sendHello(apdu);
				return;
			case 0x02:
				sendPubKeyExp(apdu);
				return;
			case 0x03:
				sendPubKeyMod(apdu);
				return;
			case 0x04:
				signData(apdu);
				return;
			case 0x05:
				echo(apdu);
				return;
			default:
				ISOException.throwIt (ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}

	private void signData(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		//short dataLen = apdu.setIncomingAndReceive();

		short signLen = signature.sign(buffer, apdu.getOffsetCdata(), apdu.getIncomingLength(), scratchpad, (short) 0);

		Util.arrayCopyNonAtomic(scratchpad, (short) 0, buffer, (short) 0, signLen);
		apdu.setOutgoingAndSend((short) 0, signLen);
	}

	private void sendHello(APDU apdu) {
		byte buffer[] = apdu.getBuffer();
		Util.arrayCopyNonAtomic(hello, (short)0, buffer, (short)0, (short)hello.length);
		apdu.setOutgoingAndSend((short)0, (short)hello.length);
	}

	private void sendPubKeyExp(APDU apdu) {
		byte buffer[] = apdu.getBuffer();
		RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
		short len = pub.getExponent(buffer, (short) 0);
		apdu.setOutgoingAndSend((short)0, len);
	}

	private void sendPubKeyMod(APDU apdu) {
		byte buffer[] = apdu.getBuffer();
		RSAPublicKey pub = (RSAPublicKey) kp.getPublic();
		short len = pub.getModulus(buffer, (short) 0);
		apdu.setOutgoingAndSend((short)0, len);
	}

	private void echo(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short len = apdu.getIncomingLength();
		Util.arrayFillNonAtomic(buffer, (short) 0, (short) 1, (byte) len);
		apdu.setOutgoingAndSend((short) 0, (short)1);
	}
}

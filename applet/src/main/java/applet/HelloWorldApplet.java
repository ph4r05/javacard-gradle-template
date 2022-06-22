package applet;

import javacard.framework.*;

public class HelloWorldApplet extends Applet
{

	private static final byte[] helloWorld = {'H', 'e', 'l', 'l', 'o', ' ', 'W', 'o', 'r', 'l', 'd', '!'};

	public static void install(byte[] bArray, short bOffset, byte bLength)
	{
		new HelloWorldApplet();
	}

	public HelloWorldApplet()
	{
		register();
	}

	public void process(APDU apdu)
	{
		sendHelloWorld(apdu);
	}

	// part of https://github.com/devrandom/javacard-helloworld/blob/master/src/main/java/org/gitian/javacard/HelloWorldApplet.java#L38
	private void sendHelloWorld(APDU apdu) {
		byte[] buffer = apdu.getBuffer();
		short length = (short) helloWorld.length;
		Util.arrayCopyNonAtomic(helloWorld, (short) 0, buffer, (short) 0, length);
		apdu.setOutgoingAndSend((short) 0, length);
	}
}

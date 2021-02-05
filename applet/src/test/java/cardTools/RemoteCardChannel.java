package cardTools;

import okhttp3.*;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Objects;

public class RemoteCardChannel extends CardChannel {
  private final static Logger LOG = LoggerFactory.getLogger(RemoteCardChannel.class);
  public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
  private final OkHttpClient client = new OkHttpClient();
  protected RemoteCard card;
  protected RunConfig cfg;
  protected boolean connected = false;

  public RemoteCardChannel(RunConfig runConfig) {
    card = new RemoteCard();
    cfg = runConfig;
  }

  @Override
  public Card getCard() {
    return card;
  }

  @Override
  public int getChannelNumber() {
    return 0;
  }

  @Override
  public ResponseAPDU transmit(CommandAPDU apdu) throws CardException {
    ResponseAPDU responseAPDU = null;

    try {
      connectIfNeeded();

      log(apdu);
      final JSONObject resp = cardApdu(apdu.getBytes());
      final byte[] apduData = Hex.decodeHex(resp.getString("response"));
      responseAPDU = new ResponseAPDU(apduData);
      log(responseAPDU);

    } catch (Exception ex) {
      LOG.warn("Transmit failed", ex);
      throw new CardException("Transmit failed - exception", ex);
    }

    return responseAPDU;
  }

  @Override
  public int transmit(ByteBuffer bb, ByteBuffer bb1) throws CardException {
    LOG.error("Accessing unimplemented transmit variant");
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void close() throws CardException {
    try {
      cardDisconnect(true);
    } catch (IOException e) {
      throw new CardException("Disconnect failed - exception", e);
    } finally {
      connected = false;
    }
  }

  protected void connectIfNeeded() throws IOException {
    if (connected){
      return;
    }

    if (cfg.remoteDisconnectPrevious) {
      LOG.debug("Disconnecting previous session");
      cardDisconnect(true);
      cardConnect();

    } else {
      LOG.debug("Trying to reuse existing card session");
      final boolean conn = cardIsConnected();
      if (!conn) {
        LOG.debug("Card session reuse not successful, connecting...");
        cardConnect();
      }
    }

    // if (cfg.aid != null) {
    //   cardSelect(cfg.aid);
    // }

    connected = true;
  }

  protected JSONObject addTarget(JSONObject req){
    req.put("target", cfg.remoteCardType == CardType.JCARDSIMLOCAL ? "sim" : "card");
    req.put("idx", cfg.targetReaderIndex);
    return req;
  }

  protected boolean cardIsConnected() throws IOException {
    final JSONObject req = addTarget(new JSONObject().put("action", "is_connected"));
    LOG.debug("Calling card is_connected: " + req.toString());

    JSONObject resp = sendJson(req);
    checkResult(resp);
    return resp.getBoolean("connected");
  }

  protected JSONObject cardConnect() throws IOException {
    final JSONObject req = addTarget(new JSONObject().put("action", "connect"));

    LOG.debug("Calling card connect: " + req.toString());
    JSONObject resp = sendJson(req);
    checkResult(resp);
    return resp;
  }

  protected JSONObject cardDisconnect(Boolean reset) throws IOException {
    LOG.debug("Calling card disconnect");
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "disconnect").put("reset", reset)));
    return resp;
  }

  protected JSONObject cardReset() throws IOException {
    LOG.debug("Calling card reset");
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "reset")));
    return resp;
  }

  protected JSONObject cardSelect(byte[] aid) throws IOException {
    LOG.debug("Calling AID select with AID: " + Hex.encodeHexString(aid));
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "select").put("aid", Hex.encodeHexString(aid))));
    checkResult(resp);
    return resp;
  }

  protected JSONObject cardApdu(byte[] apdu) throws IOException {
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "send").put("apdu", Hex.encodeHexString(apdu))));
    checkResult(resp);
    return resp;
  }

  protected JSONObject cardAtr() throws IOException {
    LOG.debug("Calling getAtr");
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "atr")));
    checkResult(resp);
    return resp;
  }

  protected String cardProtocol() throws IOException {
    LOG.debug("Calling cardProtocol");
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "protocol")));
    checkResult(resp);
    return resp.getString("protocol");
  }

  public void checkResult(JSONObject res) {
    int result = res.getInt("result");
    if (result != 0){
      connected = false;
      LOG.warn("RemoteCard returned invalid code: " + result);
      throw new RuntimeException("RemoteCard server returned invalid code: " + result);
    }
  }

  public JSONObject sendJson(JSONObject req) throws IOException {
    final Request request = new Request.Builder()
        .url(cfg.remoteAddress + "/v1/card")
        .header("User-Agent", "OkHttp")
        .addHeader("Accept", "application/json; q=0.5")
        .post(RequestBody.create(req.toString(), MEDIA_TYPE_JSON))
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
      final String respString = Objects.requireNonNull(response.body()).string();
      final JSONObject jso = new JSONObject(respString);
      if (!jso.has("result")){
        throw new IOException("Response has no result field");
      }
      return jso;
    }
  }

  private static void log(CommandAPDU cmd) {
    Util.log(LOG, cmd);
  }

  private static void log(ResponseAPDU response) {
    Util.log(LOG, response);
  }

  class RemoteCard extends Card {
    private final Logger LOG = LoggerFactory.getLogger(RemoteCardChannel.class);
    @Override
    public ATR getATR() {
      try {
        connectIfNeeded();
        final JSONObject resp = cardAtr();
        return new ATR(Hex.decodeHex(resp.getString("atr")));
      } catch (Exception e) {
        LOG.error("ATR failed", e);
      }
      return null;
    }

    @Override
    public String getProtocol() {
      try {
        connectIfNeeded();
        return cardProtocol();
      } catch (Exception e) {
        LOG.error("ATR failed", e);
      }
      return null;
    }

    @Override
    public CardChannel getBasicChannel() {
      return RemoteCardChannel.this;
    }

    @Override
    public CardChannel openLogicalChannel() throws CardException {
      return RemoteCardChannel.this;
    }

    @Override
    public void beginExclusive() throws CardException {
      LOG.info("Asked to beginExclusive(), do nothing");
    }

    @Override
    public void endExclusive() throws CardException {
      LOG.info("Asked to endExclusive(), do nothing");
    }

    @Override
    public byte[] transmitControlCommand(int controlCode, byte[] command) throws CardException {
      LOG.error("Accessing unsupported transmitControlCommand");
      throw new CardException("Not supported");
    }

    @Override
    public void disconnect(boolean reset) throws CardException {
      close();
    }
  }

}

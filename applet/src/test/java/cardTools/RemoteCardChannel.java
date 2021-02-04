package cardTools;

import apdu4j.HexUtils;
import okhttp3.*;
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
  protected SimulatedCard m_card;
  protected RunConfig cfg;
  protected boolean connected = false;

  public RemoteCardChannel(RunConfig runConfig) {
    m_card = new SimulatedCard();
    cfg = runConfig;
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
      connectIfNeeded();

      log(apdu);
      final JSONObject resp = cardApdu(apdu.getBytes());
      final byte[] apduData = HexUtils.hex2bin(resp.getString("response"));
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
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void close() throws CardException {
    try {
      cardDisconnect();
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
      cardDisconnect();
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
    req.put("target", cfg.remoteCardType == RunConfig.CARD_TYPE.JCARDSIMLOCAL ? "sim" : "card");
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

  protected JSONObject cardDisconnect() throws IOException {
    LOG.debug("Calling card disconnect");
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "disconnect")));
    return resp;
  }

  protected JSONObject cardReset() throws IOException {
    LOG.debug("Calling card reset");
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "reset")));
    return resp;
  }

  protected JSONObject cardSelect(byte[] aid) throws IOException {
    LOG.debug("Calling AID select with AID: " + HexUtils.bin2hex(aid));
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "select").put("aid", HexUtils.bin2hex(aid))));
    checkResult(resp);
    return resp;
  }

  protected JSONObject cardApdu(byte[] apdu) throws IOException {
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "send").put("apdu", HexUtils.bin2hex(apdu))));
    checkResult(resp);
    return resp;
  }

  protected JSONObject cardAtr() throws IOException {
    LOG.debug("Calling getAtr");
    JSONObject resp = sendJson(addTarget(new JSONObject().put("action", "atr")));
    checkResult(resp);
    return resp;
  }

  public void checkResult(JSONObject res) {
    int result = res.getInt("result");
    if (result != 0){
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

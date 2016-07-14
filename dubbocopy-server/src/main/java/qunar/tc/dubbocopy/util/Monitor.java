package qunar.tc.dubbocopy.util;

import qunar.metrics.Counter;
import qunar.metrics.Meter;
import qunar.metrics.Metrics;

/**
 * @author song.xue created on 15/4/25
 * @version 1.0.0
 */
public class Monitor {

    public final static Meter serverChannelRead = Metrics.meter("tc.dubbocopy.server.serverChannelRead").get();
    public final static Meter requestDecoded = Metrics.meter("tc.dubbocopy.server.reqestDecoded").get();
    public final static Meter notDubboRequest = Metrics.meter("tc.dubbocopy.server.notDubboRequest").tag("cause", "notRequest").get();
    public final static Meter notDubboMagic = Metrics.meter("tc.dubbocopy.server.notDubboRequest").tag("cause", "notDubboMagic").get();
    public final static Meter requestSent = Metrics.meter("tc.dubbocopy.server.requestSent").get();
    public final static Meter requestSuccess = Metrics.meter("tc.dubbocopy.server.requestResult").tag("result", "success").get();
    public final static Meter requestFail = Metrics.meter("tc.dubbocopy.server.requestResult").tag("result", "fail").get();
    public final static Meter connectSuccess = Metrics.meter("tc.dubbocopy.server.connectResult").tag("result", "success").get();
    public final static Meter connectFail = Metrics.meter("tc.dubbocopy.server.connecttResult").tag("result", "fail").get();
    public final static Meter createNewConnection = Metrics.meter("tc.dubbocopy.server.connectUse").tag("type", "new").get();
    public final static Meter useOldConnection = Metrics.meter("tc.dubbocopy.server.connectUse").tag("type", "cached").get();
    public final static String REQUEST_NO_ROUTER = "tc.dubbocopy.server.requestNoRouter";
    public final static Meter routerChanged = Metrics.meter("tc.dubbocopy.server.routerChanged").get();
    public final static Counter sendBufferOverflow = Metrics.counter("tc.dubbocopy.server.sendBufferOverflow").get();

}

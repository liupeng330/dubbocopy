package qunar.tc.dubbocopy.conn;

import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.handler.DiscardHandler;
import qunar.tc.dubbocopy.handler.IdleCloseHandler;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by zhaohui.yu
 * 15/8/10
 */
public class HttpConnectionPool implements ConnectionPool {

    @Value("${dubbocopy.client.connectTimeout}")
    private int connectionTimeoutMs;

    @Value("${dubbocopy.client.idleTimeoutSeconds}")
    private int idleTimeoutSeconds;

    @Autowired
    private IdleCloseHandler idleCloseHandler;

    private final DiscardHandler discardHandler;

    private final ConcurrentMap<String, Connection> used;

    public HttpConnectionPool() {
        this.used = Maps.newConcurrentMap();
        this.discardHandler = new DiscardHandler(this);
    }

    public Connection getConnection(Target target) {
        NettyConnection connection = new NettyConnection(target, connectionTimeoutMs, idleTimeoutSeconds, discardHandler, idleCloseHandler);
        used.putIfAbsent(connection.key(), connection);
        return connection;
    }

    public void returnConnection(Connection connection) {
        Connection usedConn = used.remove(connection.key());
        if (usedConn == null) return;
        usedConn.close();
    }

    public void removeConnection(Connection connection) {
        returnConnection(connection);
    }
}

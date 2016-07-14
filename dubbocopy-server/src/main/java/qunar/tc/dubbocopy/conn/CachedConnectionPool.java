package qunar.tc.dubbocopy.conn;

import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import qunar.metrics.Gauge;
import qunar.metrics.Metrics;
import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.handler.DiscardHandler;
import qunar.tc.dubbocopy.handler.IdleCloseHandler;

import java.util.concurrent.ExecutionException;

/**
 * @author kelly.li
 * @date 2015-06-30
 */
public class CachedConnectionPool implements ConnectionPool, InitializingBean, DisposableBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(CachedConnectionPool.class);

    private LoadingCache<Target, Connection> POOL;

    @Value("${dubbocopy.client.connectTimeout}")
    private int connectionTimeoutMs;

    @Value("${dubbocopy.client.idleTimeoutSeconds}")
    private int idleTimeoutSeconds;

    @Autowired
    private IdleCloseHandler idleCloseHandler;

    private final DiscardHandler discardHandler;

    public CachedConnectionPool() {
        this.discardHandler = new DiscardHandler(this);
    }

    public void afterPropertiesSet() {
        POOL = CacheBuilder.newBuilder().removalListener(new RemovalListener<Target, Connection>() {
            public void onRemoval(RemovalNotification<Target, Connection> removedItem) {
                LOGGER.info("Remove connection {} from cache", removedItem.getKey());
                Connection conn = removedItem.getValue();
                if (conn != null) {
                    conn.close();
                }
            }
        }).build(new CacheLoader<Target, Connection>() {
            @Override
            public Connection load(Target target) {
                LOGGER.info("Build connection {} to cache", target);
                return new NettyConnection(target, connectionTimeoutMs, idleTimeoutSeconds, discardHandler, idleCloseHandler);
            }
        });

        Metrics.gauge("target.client.connections").call(new Gauge() {
            public double getValue() {
                return POOL.size();
            }
        });

    }

    public Connection getConnection(Target target) {
        Connection connection = null;
        try {
            connection = POOL.get(target);
        } catch (ExecutionException e) {
            String errorMessage = "Get client connection failed";
            LOGGER.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
        return connection;
    }

    public void returnConnection(Connection connection) {

    }

    public void removeConnection(Connection connection) {
        POOL.invalidate(connection.target());
    }

    public void destroy() {
        POOL.invalidateAll();
    }

}

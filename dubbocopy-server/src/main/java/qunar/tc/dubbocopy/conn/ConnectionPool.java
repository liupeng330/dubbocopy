package qunar.tc.dubbocopy.conn;

import qunar.tc.dubbocopy.api.model.Target;

/**
 * @author kelly.li
 * @date 2015-06-30
 */
public interface ConnectionPool {

    Connection getConnection(Target target);

    void returnConnection(Connection connection);

    void removeConnection(Connection connection);
}

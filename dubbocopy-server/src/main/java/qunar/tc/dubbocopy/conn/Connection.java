package qunar.tc.dubbocopy.conn;

import qunar.tc.dubbocopy.api.model.Target;
import qunar.tc.dubbocopy.request.RawByteBufRequest;

/**
 * @author kelly.li
 * @date 2015-06-30
 */
public interface Connection {

    public void send(final RawByteBufRequest request);

    void close();

    String key();

    Target target();
}

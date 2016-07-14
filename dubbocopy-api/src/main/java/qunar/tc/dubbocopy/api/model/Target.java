package qunar.tc.dubbocopy.api.model;

import java.io.Serializable;

/**
 * @author song.xue created on 15/4/22
 * @version 1.0.0
 */
public class Target implements Serializable {
    private final String host;
    private final int port;

    public Target(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Target target = (Target) o;

        if (port != target.port) return false;
        if (host != null ? !host.equals(target.host) : target.host != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = host != null ? host.hashCode() : 0;
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "Target{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}

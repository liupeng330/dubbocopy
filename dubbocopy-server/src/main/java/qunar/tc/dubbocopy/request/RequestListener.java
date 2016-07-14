package qunar.tc.dubbocopy.request;

/**
 * @author kelly.li
 * @date 2015-06-30
 */
public interface RequestListener {

	void onConnected();

	void onConnectFailed(Throwable e);

	void onUnWritable();

	void onWriteSuccess();

	void onWriteFailed(Throwable e);

}

package qunar.tc.dubbocopy.util;

/**
 * 
 * @author kelly.li
 * @date 2015-07-08
 */
public class KeyGenerator {

	public static String generateKey(String service, String method) {
		return service + "_" + method;
	}

}

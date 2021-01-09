package pri.liyang.util;

import java.util.HashSet;
import java.util.Set;

public class UriUtils {

    private static final Set<String> uriBlackList = new HashSet<>();

    static {
        uriBlackList.add("/favicon.ico");
        uriBlackList.add("/");
    }

    public static boolean isInBlackList(String uri) {
        return uriBlackList.contains(uri);
    }

    public static String parseUri(String uri) {
        int index = uri.indexOf("?");
        if (index > 0) {
            uri = uri.substring(0, index);
        }
        uri = uri.substring(1);
        return uri.replaceAll("/", ".");
    }

}

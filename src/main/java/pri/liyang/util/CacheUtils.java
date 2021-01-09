package pri.liyang.util;

import pri.liyang.inter.BaseController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheUtils {

    private static final Map<String, BaseController> controllerCache = new ConcurrentHashMap<>();

    public static BaseController getControllerInstance(String classpath) throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        if (!controllerCache.containsKey(classpath)) {
            Class<?> controllerClass = Class.forName(classpath);
            BaseController controller = (BaseController)controllerClass.newInstance();
            controllerCache.put(classpath, controller);
        }
        return controllerCache.get(classpath);
    }

}

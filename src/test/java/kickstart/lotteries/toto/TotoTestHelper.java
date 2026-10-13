package kickstart.lotteries.toto;

import java.lang.reflect.Field;

class TotoTestHelper {
    static void injectField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject field '" + fieldName + "' into " + target, e);
        }
    }
}


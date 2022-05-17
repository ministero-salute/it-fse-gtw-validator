package it.finanze.sanita.fse2.ms.gtw.validator.singleton;

public final class ResetSingleton {

	public static void setPrivateField(Class clazz, Object inst, Object value, String... fields) throws Exception {
		for(String field : fields) {
			java.lang.reflect.Field f = clazz.getDeclaredField(field);
			f.setAccessible(true);
			f.set(inst, value);
			f.setAccessible(false);
		}
	}
}

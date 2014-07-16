package di.utility;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ReflectUtil {

	private ReflectUtil() {
	}

	public static Set<Field> getFields(Class<?> classToInspect, Class<? extends Annotation> annotation) {
		Set<Field> foundFields = new HashSet<Field>();

		Collection<Field> allFields = getFields(classToInspect);
		for (Field field : allFields) {
			if (field.isAnnotationPresent(annotation)) {
				foundFields.add(field);
			}
		}

		return foundFields;
	}

	public static Set<Field> getFields(Object object, Class<? extends Annotation> annotation) {
		return getFields(object.getClass(), annotation);
	}

	public static Collection<Field> getFields(Class<?> klass) {
		Class<?> clazz = klass;
		Map<String, Field> fields = new HashMap<String, Field>();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (!fields.containsKey(field.getName())) {
					fields.put(field.getName(), field);
				}
			}
			clazz = clazz.getSuperclass();
		}

		return fields.values();
	}
	
}

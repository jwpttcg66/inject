package di.convert;

import java.util.HashMap;
import java.util.Map;

import di.convert.impl.BooleanConvert;
import di.convert.impl.IntegerConvert;
import di.convert.impl.LongConvert;
import di.convert.impl.StringConvert;

public class ConvertObject {
	private static Map<Class<?>, Convert<?>> CONVERT_TYPE_MAPS = new HashMap<>();

	static {
		CONVERT_TYPE_MAPS.put(boolean.class, new BooleanConvert());
		CONVERT_TYPE_MAPS.put(Boolean.class, new BooleanConvert());

		CONVERT_TYPE_MAPS.put(int.class, new IntegerConvert());
		CONVERT_TYPE_MAPS.put(Integer.class, new IntegerConvert());

		CONVERT_TYPE_MAPS.put(long.class, new LongConvert());
		CONVERT_TYPE_MAPS.put(Long.class, new LongConvert());
		
		CONVERT_TYPE_MAPS.put(String.class, new StringConvert());
	}

	@SuppressWarnings("unchecked")
	public static <T> T execute(String value, Class<T> type) {
		if (type == null) {
			throw new RuntimeException("Can't convert to null type");
		}

		if (value == null) {
			return null;
		}

		Convert<T> convert = (Convert<T>) CONVERT_TYPE_MAPS.get(type);
		if (convert == null) {
			throw new RuntimeException(String.format("not support the config inject type:%s.", type));
		}
		return convert.parse(value);
	}

}

package di;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.annotation.Inject;
import di.annotation.PreLoad;
import di.utility.ReflectUtil;
import di.utility.ScanClassUtils;

public class InjectContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(InjectContext.class);
	private static String[] packages = { "com", "test" };

	/** 注入类字段集合 	key:类,value:字段集合 */
	private static final Map<Class<?>, Set<Field>> clazzFieldMaps = new ConcurrentHashMap<>();
	/** 实例类集合 key:类,value:实例 类*/
	private static final Map<Class<?>, Object> instanceMaps = new ConcurrentHashMap<>();
	
	private InjectContext() {
	}
	
	/**
	 * 初始化
	 * <pre>
	 * >加载所有注入配置
	 * >获取所有带有@Inject注解的类
	 * >循环处理每个类的依赖关系，并注入@Config属性 与@Inject引用
	 * </pre>
	 * @param fileName
	 */
	public static void initialize(String fileName) {
		BeanConfig.initialize(fileName);
		
		TreeMap<Integer, List<Class<?>>> sortScanList = getScanClazzMaps();
		
		List<Object> instanceList = new ArrayList<>();
		for (List<Class<?>> clazzList : sortScanList.values()) {
			for (Class<?> clazz : clazzList) {
				if (instanceMaps.containsKey(clazz)) {
					throw new RuntimeException(String.format("有重复的注入名%s.", clazz));
				}

				try {
					Object instance = clazz.newInstance();
					instanceMaps.put(clazz, instance);
					instanceList.add(instance);
				} catch (Exception ex) {
					throw new RuntimeException(ex);
				}
			}
		}

		// 配置注入、引用注入
		for (Object instance : instanceList) {
			injectInstance(instance);
		}

		// 执行初始化方法
		for (Object instance : instanceList) {
			injectPreload(instance);
		}

		LOGGER.info("注入初始化完成!");
	}


	/**
	 * 获取bean
	 * @param beanClazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> clazz) {
		for (Entry<Class<?>, Object> entry : instanceMaps.entrySet()) {
			if (compareClass(clazz, entry.getKey())) {
				return (T) entry.getValue();
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getBean(String className) {
		for (Entry<Class<?>, Object> entry : instanceMaps.entrySet()) {
			if (entry.getKey().getSimpleName().equals(className)) {
				return (T) entry.getValue();
			}
			
			Inject inject = entry.getKey().getAnnotation(Inject.class);
			if (inject != null && inject.value().equals(className)) {
				return (T) entry.getValue();
			}
		}
		return null;
	}
 	
	/**
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> getBeanOfType(Class<T> clazz) {
		List<T> list = new ArrayList<>();
		for (Entry<Class<?>, Object> entry : instanceMaps.entrySet()) {
			if (compareClass(clazz, entry.getKey())) {
				list.add((T) entry.getValue());
			}
		}
		return list;
	}

	/**
	 * 替换正在运行的bean
	 * @param clazz	需要替换的类
	 */
	@SuppressWarnings("unchecked")
	public static <T> T replace(Class<T> clazz) {
		if (clazz == null) {
			return null;
		}

		try {
			Class<?> oldClazz = clazz;
			Class<?>[] interfacesClazz = clazz.getInterfaces();
			for (Class<?> interClazz : interfacesClazz) {
				for (Entry<Class<?>, Object> entry : instanceMaps.entrySet()) {
					if (compareClass(interClazz, entry.getKey())) {
						oldClazz = entry.getKey();
						break;
					}
				}
			}
			
			synchronized (instanceMaps) {
				clazzFieldMaps.remove(oldClazz);
				instanceMaps.remove(oldClazz);
				
				Object instance = clazz.newInstance();
				injectInstance(instance);
				injectPreload(instance);

				// 替换后，原来的类有没有消失？
				instanceMaps.put(clazz, instance);

				// 循环所有注入类，重新绑定一次Field注入。。。。
				for (Entry<Class<?>, Set<Field>> entry : clazzFieldMaps.entrySet()) {
					for (Field f : entry.getValue()) {
						// 优化，判断该field为 当前需要替换的Class<?>
						if (compareClass(clazz, f.getType())) {
							Object tmpInstance = getBean(entry.getKey());
							injectField(tmpInstance, f);
						}
					}
				}
				
				return (T) instance;				
			}
		} catch (Exception ex) {
		}
		return null;
	}
	
	
	// static method---------------------------------------------------


	private static boolean compareClass(Class<?> clazz1, Class<?> clazz2) {
		if (clazz1.getName().equals(clazz2.getName())) {
			return true;
		}
		
		if (clazz1.isInterface() || Modifier.isAbstract(clazz1.getModifiers())) {
			if (clazz1.isAssignableFrom(clazz2)) {
				return true;
			}
			return false;
		}
		
		if (clazz2.isInterface() || Modifier.isAbstract(clazz2.getModifiers())) {
			if (clazz2.isAssignableFrom(clazz1)) {
				return true;
			}
			return false;
		}

		Inject inject1 = clazz1.getAnnotation(Inject.class);
		Inject inject2 = clazz2.getAnnotation(Inject.class);
		
		if (inject1.value().length() > 0 && inject2.value().length() > 0 && inject1.value() == inject2.value()) {
			return true;
		}
		return false;
	}

	private static String[] getScanPackages() {
		String cfgPackages = BeanConfig.getValue("inject.scan.packages");
		String[] packageArray;
		if (cfgPackages != null && !cfgPackages.isEmpty()) {
			packageArray = cfgPackages.split(",");
		} else {
			packageArray = packages;
		}
		return packageArray;
	}
	
	/**
	 * 实例注入
	 * @param instance
	 */
	private static void injectInstance(Object instance) {
		BeanConfig.injectConfig(instance); // 配置注入
		Collection<Field> fields = getInjectFields(instance.getClass());
		for (Field field : fields) {
			injectField(instance, field);
		}
	}
	
	/**
	 * 字段注入
	 * @param instance	实倒类
	 * @param field		需要注入的字段
	 */
	private static void injectField(Object instance, Field field) {
		try {
			Class<?> typeClazz = field.getType();
			Inject inject = field.getAnnotation(Inject.class);
			Object fieldInstance = inject.value().equals("") ? getBean(typeClazz) : getBean(inject.value());
			if (fieldInstance == null) {
				LOGGER.warn("{} -- filed:{} inject value is null.", instance.getClass(), field.getName());
			}

			field.setAccessible(true);
			field.set(instance, fieldInstance);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
		
	private static Set<Field> getInjectFields(Class<?> clazz) {
		Set<Field> fields = clazzFieldMaps.get(clazz);
		if (fields == null) {
			fields = ReflectUtil.getFields(clazz, Inject.class);
			clazzFieldMaps.put(clazz, fields);
		}
		return fields;
	}
	
	private static TreeMap<Integer, List<Class<?>>> getScanClazzMaps() {
		List<Class<Object>> scanList = ScanClassUtils.scan(getScanPackages(), Inject.class, null);

		TreeMap<Integer, List<Class<?>>> preloadMaps = new TreeMap<>(new Comparator<Integer>() {
			@Override
			public int compare(Integer key0, Integer key1) {
				return key0.compareTo(key1);
			}
		});

		for (Class<Object> clazz : scanList) {
			Inject inject = clazz.getAnnotation(Inject.class);
			List<Class<?>> instanceList = preloadMaps.get(inject.order());
			if (instanceList == null) {
				instanceList = new ArrayList<>();
				preloadMaps.put(inject.order(), instanceList);
			}
			instanceList.add(clazz);
		}
		return preloadMaps;
	}
	
	private static void injectPreload(Object instance) {
		try {
			for (Class<?> clazz = instance.getClass(); clazz != Object.class; clazz = clazz.getSuperclass()) {
				Method[] methodArray = clazz.getDeclaredMethods();
				for (Method method : methodArray) {
					if (method.isAnnotationPresent(PreLoad.class)) {
						method.setAccessible(true);
						method.invoke(instance);
					}
				}
			}
			
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
}

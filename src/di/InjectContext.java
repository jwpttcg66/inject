package di;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.annotation.Inject;
import di.annotation.PostConstruct;
import di.utility.ReflectUtil;
import di.utility.ScanClassUtils;

public class InjectContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(InjectContext.class);
	private static String[] packages = { "com", "test" };

	/** 注入类字段集合 	key:类,value:字段集合 */
	private static final Map<Class<?>, Set<Field>> clazzFieldMaps = new ConcurrentHashMap<>();
	/** 实例类集合 key:类,value:实例 类*/
	private static final Map<Class<?>, Object> instanceMaps = new ConcurrentHashMap<>();
	
	private static BeanConfig BEAN_CONFIG = new BeanConfig();
	
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
		BEAN_CONFIG.initialize(fileName);

		try {
			List<Class<?>> scanList = ScanClassUtils.scan(getScanPackages(), Inject.class);
			for (Class<?> clazz : scanList) {
				Object instance = clazz.newInstance();
				
				if (instanceMaps.containsKey(clazz)) {
					throw new RuntimeException(String.format("有重复的注入名%s.", clazz));
				}
				instanceMaps.put(clazz, instance);
			}

			// 配置注入、引用注入
			for (Object instance : instanceMaps.values()) {
				injectInstance(instance);
			}

			// 执行初始化方法
			for (Object instance : instanceMaps.values()) {
				postConstruct(instance);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
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
			T value = (T) entry.getValue();
			if (clazz.isInterface() && clazz.isAssignableFrom(value.getClass())) {
				return value;
			} else {
				if (entry.getKey().equals(clazz)) {
					return value;
				}
			}
		}

		return null;
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
					if (interClazz.isAssignableFrom(entry.getValue().getClass())) {
						oldClazz = entry.getValue().getClass();
						break;
					}
				}
			}
			
			synchronized (instanceMaps) {
				clazzFieldMaps.remove(oldClazz);
				instanceMaps.remove(oldClazz);
				
				Object instance = clazz.newInstance();
				injectInstance(instance);
				postConstruct(instance);

				// 替换后，原来的类有没有消失？
				instanceMaps.put(clazz, instance);

				// 循环所有注入类，重新绑定一次Field注入。。。。
				for (Entry<Class<?>, Set<Field>> entry : clazzFieldMaps.entrySet()) {
					for (Field f : entry.getValue()) {
						// 优化，判断该field为 当前需要替换的Class<?>
						Class<?> fieldClass = f.getType();

						if (fieldClass.isInterface()) {
							if(fieldClass.isAssignableFrom(clazz)) {
								Object tmpInstance = getBean(entry.getKey());
								injectField(tmpInstance, f);								
							}
						} else if(fieldClass.equals(clazz)) {
							Object tmpInstance = getBean(entry.getKey());
							injectField(tmpInstance, f);	
						}
					}
				}
				
				if (LOGGER.isDebugEnabled()) {
					for (Class<?> cls : instanceMaps.keySet()) {
						LOGGER.debug("inject instance:{}", cls);
					}
				}
				
				
				return (T) instance;				
			}
		} catch (Exception ex) {
		}
		return null;
	}
	
	
	// static method---------------------------------------------------
	
//	private static String getName(Class<?> clazz) {
//		Inject inject = clazz.getAnnotation(Inject.class);
//		if (inject == null || inject.value().equals("")) {
//			return clazz.getName();
//		}
//		return inject.value();
//	}

	private static String[] getScanPackages() {
		String cfgPackages = BEAN_CONFIG.getValue("inject.scan.packages");
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
		BEAN_CONFIG.injectConfig(instance); // 配置注入
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
			Object fieldInstance = getBean(typeClazz);
			set2Field(instance, field, fieldInstance);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private static void set2Field(Object instance, Field field, Object value) {
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
	
	private static void postConstruct(Object instance) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (Method method : instance.getClass().getMethods()) {
			if (method.isAnnotationPresent(PostConstruct.class)) {
				method.invoke(instance);
			}
		}
	}
	
}

package di.utility;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import net.sf.corn.cps.CPScanner;
import net.sf.corn.cps.ClassFilter;
import di.annotation.Inject;

public class ScanClassUtils {


	/**
	 * 获取有带@Inject注解的类集合
	 * @param packages
	 * @param annotation
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<Class<T>> scan(String[] packages, Class<? extends Annotation> annotation, Class<?> interfaceClass) {
		ClassFilter clazzFilter = new ClassFilter();
		for (String packageName : packages) {
			clazzFilter.packageName(packageName);
		}

		if (annotation != null) {
			clazzFilter.annotation(Inject.class);
			clazzFilter.joinAnnotationsWithAnd();
		}
		
		if (interfaceClass != null) {
			clazzFilter.interfaceClass(interfaceClass);
			clazzFilter.joinInterfacesWithAnd();
		}

		List<Class<?>> scanList = CPScanner.scanClasses(clazzFilter);
		List<Class<T>> scanTList = new ArrayList<>();
		for (Class<?> clazz : scanList) {
			scanTList.add((Class<T>) clazz);
		}
		return scanTList;
	}
	
	public static <T> List<Class<T>> scan(String... packages) {
		return scan(packages, null, null);
	}
	
	public static <T> List<Class<T>> scan(String packageName, Class<?> interfaceClass) {
		return scan(new String[] { packageName }, null, interfaceClass);
	}
	
}

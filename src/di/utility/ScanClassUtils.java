package di.utility;

import java.io.File;
import java.io.FileFilter;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ScanClassUtils {
	private static final String CLASS_PATH = ScanClassUtils.class.getClassLoader().getResource("").getPath();
	private static final String FILE_EXT = ".class";
	private static final String BLANK = "";
	private static final String DOT = ".";

	private static FileFilter FILE_FILTER = new FileFilter() {
		@Override
		public boolean accept(File pathname) {
			return pathname.getName().endsWith(FILE_EXT) || pathname.isDirectory();
		}
	};

	/**
	 * 获取有带@Inject注解的类集合
	 * @param packages
	 * @param annotation
	 * @return
	 */
	public static List<Class<?>> scan(String[] packages, Class<? extends Annotation> annotation) {
		List<Class<?>> classList = new ArrayList<Class<?>>();
		for (String name : packages) {
			File classFileDirectory = getScanClassFileDirectory(obtainFilePath(name));
			scanFiles(classFileDirectory, classList, annotation);
		}
		return classList;
	}

	private static void scanFiles(File file, List<Class<?>> classList, Class<? extends Annotation> annotation) {
		if (file.isDirectory()) {
			for (File f : file.listFiles(FILE_FILTER)) {
				scanFiles(f, classList, annotation);
			}
		} else {
			try {
				final Class<?> clazz = getClass(file);
				if (clazz != null && clazz.isAnnotationPresent(annotation)) {
					classList.add(clazz);
				}
			} catch (ClassNotFoundException ex) {
			}
		}
	}

	private static Class<?> getClass(File file) throws ClassNotFoundException {
		final String packagePath = file.getAbsolutePath().substring(file.getAbsolutePath().indexOf(CLASS_PATH) + CLASS_PATH.length());
		Class<?> clazz = Class.forName(getClassFullName(packagePath));
		return clazz;
	}

	private static String getClassFullName(String packagePath) {
		return packagePath.replace(File.separator, DOT).replace(FILE_EXT, BLANK);
	}

	private static File getScanClassFileDirectory(String filePath) {
		return new File(filePath);
	}

	private static String obtainFilePath(String packageName) {
		final String oppositeFilePath = packageName.replace(DOT, File.separator);
		return CLASS_PATH + oppositeFilePath;
	}
}

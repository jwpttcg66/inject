package di.utility;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtils.class);

	private static JavaClassLoader CLASS_LOADER = new JavaClassLoader();

	public static Class<?> loader(File loadFile) {
		File binFile = new File("bin");
		if (!binFile.exists()) {
			binFile.mkdir();
		}

		Class<?> clazz = null;
		try {
			String packageName = getPackageName(loadFile);
			String codeContent = FileUtils.readFileToString(loadFile);
			clazz = CLASS_LOADER.javaCode2Clazz(packageName, codeContent);
		} catch (IllegalAccessException | ClassNotFoundException | IOException e) {
			LOGGER.warn("{}", e);
		}

		return clazz;
	}
	
	private static String getPackageName(File file) {
		String fullPath = file.getPath();
		int beginIndex = fullPath.indexOf("\\", 1); // 去除script文件夹
		int endIndex = fullPath.lastIndexOf("."); // 去除.后缀名
		return fullPath.substring(beginIndex + 1, endIndex).replace("\\", ".");
	}
	
}

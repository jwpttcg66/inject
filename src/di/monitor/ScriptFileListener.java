package di.monitor;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.InjectContext;
import di.utility.ClassUtils;

public class ScriptFileListener extends FileAlterationListenerAdaptor {
	private static final Logger LOGGER = LoggerFactory.getLogger(ScriptFileListener.class);

	@Override
	public void onFileChange(File file) {
		Class<?> clazz = ClassUtils.loader(file);
		InjectContext.replace(clazz);
		LOGGER.info("onFileChange. file name:{}, filePath:{}", file.getName(), file.getPath());
	}

	@Override
	public void onFileCreate(File arg0) {
		LOGGER.info("onFileCreate");
	}

	@Override
	public void onFileDelete(File arg0) {
		LOGGER.info("onFileDelete");
	}

	@Override
	public void onStart(FileAlterationObserver arg0) {
	}

	@Override
	public void onStop(FileAlterationObserver arg0) {
	}

}

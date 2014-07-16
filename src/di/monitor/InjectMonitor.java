package di.monitor;

import java.io.File;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.annotation.Config;
import di.annotation.Inject;
import di.annotation.PostConstruct;

@Inject
public class InjectMonitor {
	private static final Logger LOGGER = LoggerFactory.getLogger(InjectMonitor.class);

	private FileAlterationMonitor monitor;

	@Config("inject.monitor.interval")
	private int interval;

	/** 注入监控路径 */
	@Config("inject.monitor.path")
	private String path;

	@PostConstruct
	public void init() {
		try {
			monitor = new FileAlterationMonitor(this.interval);
			File file = new File(this.path);
			FileAlterationObserver observer = new FileAlterationObserver(file);
			monitor.addObserver(observer);
			observer.addListener(new ScriptFileListener());
			monitor.start();
		} catch (Exception e) {
			LOGGER.warn("{}", e);
		}
		LOGGER.info("inject script monitor start is complete!");
	}

}

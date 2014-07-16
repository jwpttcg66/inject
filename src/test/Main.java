package test;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.InjectContext;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		LOGGER.info("begin");
		
		InjectContext.initialize("bean.properties");

		Timer timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				TestConfigInterface testConfig = InjectContext.getBean(TestConfigInterface.class);
				testConfig.print();
			}
		}, 100, 2000);
		
	}

}

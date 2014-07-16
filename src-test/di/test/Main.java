package di.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.InjectContext;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		LOGGER.info("begin");
		
		InjectContext.initialize("bean.properties");

		TestConfigImpl testConfig = (TestConfigImpl) InjectContext.getBean(TestConfigInterface.class);
		testConfig.print();

		InjectContext.replace(TestConfigImpl1.class);
		TestConfigImpl1 test1 = (TestConfigImpl1) InjectContext.getBean(TestConfigInterface.class);
		test1.print();		
		System.out.println("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
		test1.getInterface().print();
	}

}

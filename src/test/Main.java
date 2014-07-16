package test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.InjectContext;

public class Main {
	private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {
		LOGGER.info("begin");
		
		//Inject初始化
		InjectContext.initialize("bean.properties");
		
	}

}

package test.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import di.annotation.Config;
import di.annotation.PreLoad;

//@Inject
public class TestConfigImpl implements TestConfigInterface {
	private static final Logger LOGGER = LoggerFactory.getLogger(TestConfigImpl.class);

	@Config("string.test")
	public String stringValue;
	@Config("int.test")
	public int intValue;
	@Config("long.test")
	public long longValue;
	@Config("boolean.test")
	public boolean boolValue;
	
	@PreLoad
	public void init() {
		LOGGER.info("执行了@PostConstruct");
	}
	
	public TestConfigImpl() {
		LOGGER.info("执行了构造函数");
	}
	
	@Override
	public void print() {
		LOGGER.info("------------------------------------------------------------------");
		LOGGER.info("this class Name:{}", TestConfigImpl.class);
		LOGGER.info("string:{} int:{} long:{} boolean:{}", this.stringValue, this.intValue, this.longValue, this.boolValue);
		LOGGER.info("------------------------------------------------------------------");
	}
}

package test;

import di.annotation.Config;
import di.annotation.Inject;
import di.annotation.PostConstruct;

@Inject
public class TestConfigImpl implements TestConfigInterface {

	@Config("string.test")
	public String stringValue;
	@Config("int.test")
	public int intValue;
	@Config("long.test")
	public long longValue;
	@Config("boolean.test")
	public boolean boolValue;
	
	@Inject
	public TestSubClazz testSubClazz;
	
	@PostConstruct
	public void init() {
		System.out.println("执行了@PostConstruct");
	}
	
	public TestConfigImpl() {
		System.out.println("执行了构造函数");
	}
	
	@Override
	public void print() {
		System.out.println("------------------------------------------------------------------");
		System.out.println(String.format("this class Name:%s", TestConfigImpl.class));
		System.out.println(String.format("string:%s int:%s long:%s boolean:%s", this.stringValue, this.intValue, this.longValue, this.boolValue));
		System.out.println(String.format("inject TestSubClazz %s", this.testSubClazz.stringName));
		System.out.println("------------------------------------------------------------------");
	}

	@Override
	public TestConfigInterface getInterface() {
		return testSubClazz.testConfigImpl;
	}
}

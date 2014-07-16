package di.test;

import di.annotation.Config;
import di.annotation.Inject;
import di.annotation.PostConstruct;

@Inject
public class TestConfigImpl implements TestConfigInterface {

	@Config("string.test")
	public String stringValue;
	@Config("int.test")
	public int intValue;
	@Config("int.test")
	public Integer integerValue;
	@Config("long.test")
	public long longValue;
	@Config("boolean.test")
	public boolean booleanValue;
	
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
		System.out.println(this.stringValue);
		System.out.println(this.intValue);
		System.out.println(this.integerValue);
		System.out.println(this.longValue);
		System.out.println(this.booleanValue);
		System.out.println("=============================");
		System.out.println(this.testSubClazz.stringName); 
	}

	@Override
	public TestConfigInterface getInterface() {
		return testSubClazz.testConfigImpl;
	}
}

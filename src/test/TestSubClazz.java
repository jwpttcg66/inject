package test;

import di.annotation.Config;
import di.annotation.Inject;

@Inject
public class TestSubClazz {

	@Config("subtest.aaa")
	public String stringName;
	
	@Inject
	public TestConfigInterface testConfigImpl;
}

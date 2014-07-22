package test;

import java.util.Timer;
import java.util.TimerTask;

import test.config.TestConfigInterface;
import di.annotation.Inject;
import di.annotation.PreLoad;

//@Inject
public class TimerPrint {

	/** 注入这个接口.主要测试这个接口的实现类是否可以动态替换 */
	@Inject
	TestConfigInterface testConfig;
	
	@PreLoad
	public void init() {
		//每 2000ms输出print()，用于确认是否替换掉接口的实现类
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				testConfig.print();
			}
		}, 100, 2000);
	}
	
}

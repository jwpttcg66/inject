package di.convert.impl;

import di.convert.Convert;

public class IntegerConvert implements Convert<Integer> {

	@Override
	public Integer parse(String value) {
		return Integer.valueOf(value);
	}

}

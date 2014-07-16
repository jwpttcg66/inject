package di.convert.impl;

import di.convert.Convert;

public class StringConvert implements Convert<String> {

	@Override
	public String parse(String value) {
		return value;
	}

}

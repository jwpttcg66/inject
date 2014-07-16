package di.convert.impl;

import di.convert.Convert;

public class BooleanConvert implements Convert<Boolean> {

	@Override
	public Boolean parse(String value) {
		return Boolean.valueOf(value);
	}

}

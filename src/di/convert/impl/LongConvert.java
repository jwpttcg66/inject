package di.convert.impl;

import di.convert.Convert;

public class LongConvert implements Convert<Long> {

	@Override
	public Long parse(String value) {
		return Long.valueOf(value);
	}

}

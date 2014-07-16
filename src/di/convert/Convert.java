package di.convert;

public interface Convert<T> {
	
	T parse(String value);
}

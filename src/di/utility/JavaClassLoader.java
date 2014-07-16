package di.utility;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaClassLoader extends ClassLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaClassLoader.class);
	
	private Class<?> loadScriptClass(String name) throws ClassNotFoundException {
		try {
			byte[] bs = loadByteCode(name);
			return super.defineClass(name, bs, 0, bs.length);
		} catch (Exception e) {
			LOGGER.error("", e);
		}
		return null;
	}

	private byte[] loadByteCode(String name) throws IOException {
		int iRead = 0;
		String classFileName = "bin/" + name.replace('.', '/') + ".class";

		FileInputStream in = null;
		ByteArrayOutputStream buffer = null;
		try {
			in = new FileInputStream(classFileName);
			buffer = new ByteArrayOutputStream();
			while ((iRead = in.read()) != -1) {
				buffer.write(iRead);
			}
			return buffer.toByteArray();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {
				LOGGER.error("", e);
			}
			try {
				if (buffer != null) {
					buffer.close();
				}
			} catch (Exception e) {
				LOGGER.error("", e);
			}
		}
	}
	
	public Class<?> javaCode2Clazz(String name, String textCode) throws IllegalAccessException, IOException, ClassNotFoundException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

		JavaFileObject jfile = new JavaSourceFromString(name, textCode);
		List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
		jfiles.add(jfile);
		List<String> options = new ArrayList<String>();

		options.add("-encoding");
		options.add("UTF-8");
//		options.add("-classpath");
//		options.add(this.classpath);
		options.add("-d");
		options.add("bin");

		JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, jfiles);
		boolean success = task.call();
		fileManager.close();

		if (success) {
			try {
				Class<?> c = Class.forName(name);
				if (c != null) {
					return c;
				} else {
					return loadScriptClass(name);
				}
			} catch (Exception e) {
			}
		} else {
			String error = "";
			for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
				error = error + printDiagnostic(diagnostic);
			}
			LOGGER.error(error);
		}
		return null;
	}

	private String printDiagnostic(Diagnostic<?> diagnostic) {
		StringBuffer res = new StringBuffer();
		res.append("Code:[" + diagnostic.getCode() + "]\n");
		res.append("Kind:[" + diagnostic.getKind() + "]\n");
		res.append("Position:[" + diagnostic.getPosition() + "]\n");
		res.append("Start Position:[" + diagnostic.getStartPosition() + "]\n");
		res.append("End Position:[" + diagnostic.getEndPosition() + "]\n");
		res.append("Source:[" + diagnostic.getSource() + "]\n");
		res.append("Message:[" + diagnostic.getMessage(null) + "]\n");
		res.append("LineNumber:[" + diagnostic.getLineNumber() + "]\n");
		res.append("ColumnNumber:[" + diagnostic.getColumnNumber() + "]\n");
		return res.toString();
	}
	
	public class JavaSourceFromString extends SimpleJavaFileObject {
		private String code;

		public JavaSourceFromString(String name, String code) {
			super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
			this.code = code;
		}

		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) {
			return code;
		}
	}
}

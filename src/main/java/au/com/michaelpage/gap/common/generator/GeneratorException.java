package au.com.michaelpage.gap.common.generator;

public class GeneratorException extends Exception {

	private static final long serialVersionUID = -5485233371729147519L;

	public GeneratorException() {
		super();
	}

	public GeneratorException(String message) {
		super(message);
	}

	public GeneratorException(Throwable cause) {
		super(cause);
	}

	public GeneratorException(String message, Throwable cause) {
		super(message, cause);
	}

}

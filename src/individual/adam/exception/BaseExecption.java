package individual.adam.exception;

public class BaseExecption extends Exception {
	private String errorTyp;
	private String errorMsg;
	public static final String SYSTEM_ERROR = "SystemError";
	public static final String USER_DEFINED_ERROR = "UserDefinedError";
	public BaseExecption(String errorTyp, String errorMsg) {
		super();
		this.errorTyp = errorTyp;
		this.errorMsg = errorMsg;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public String getErrorTyp() {
		return errorTyp;
	}
	public void setErrorTyp(String errorTyp) {
		this.errorTyp = errorTyp;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
}

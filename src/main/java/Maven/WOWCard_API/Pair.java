package Maven.WOWCard_API;

public class Pair {
	boolean isPass;
	String code;
	public boolean isPass() {
		return isPass;
	}

	public void setPass(boolean isPass) {
		this.isPass = isPass;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	
	
	Pair(boolean isPass, String code){
		this.isPass = isPass;
		this.code = code;
	}

}

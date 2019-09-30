package au.com.michaelpage.gap.common.model;

public class JobBoard {
	
	private String name;
	
	private boolean nonNextGen;

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNonNextGen() {
		return this.nonNextGen;
	}

	public void setNonNextGen(boolean nonNextGen) {
		this.nonNextGen = nonNextGen;
	}
}

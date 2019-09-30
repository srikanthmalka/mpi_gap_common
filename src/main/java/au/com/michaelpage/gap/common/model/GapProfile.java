package au.com.michaelpage.gap.common.model;

public class GapProfile {
	private String id;
	private GapProfileType type;
	private Double exchangeRate;
	
	public GapProfile() {}

	public GapProfile(String id, GapProfileType type, Double exchangeRate) {
		super();
		this.id = id;
		this.type = type;
		this.exchangeRate = exchangeRate;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public GapProfileType getType() {
		return type;
	}

	public void setType(GapProfileType type) {
		this.type = type;
	}

	public Double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(Double exchangeRate) {
		this.exchangeRate = exchangeRate;
	};
	
}

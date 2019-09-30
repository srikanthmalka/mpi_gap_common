package au.com.michaelpage.gap.common.model;

public class Host {
	private String host;
	private String countryCode;
	private String brand;

	public Host(String host, String countryCode, String brand) {
		super();
		this.host = host;
		this.countryCode = countryCode;
		this.brand = brand;
	}

	public Host() {}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	
	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	};
	
}

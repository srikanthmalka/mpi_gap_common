package au.com.michaelpage.gap.common;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import au.com.bytecode.opencsv.CSVReader;
import au.com.michaelpage.gap.common.model.JobBoard;
import au.com.michaelpage.gap.common.util.DatabaseManager;
import au.com.michaelpage.gap.common.util.Util;

public enum Settings {
	
	INSTANCE;
	
	private Logger logger = LoggerFactory.getLogger(Settings.class);
	
	private Map<String, Object> settings;
	
	private String hostName;
	
	private String rpmId;
	
	private String localProfileId;
	
	private String regionalProfileId;
	
	private String countryCode;
	
	private String brand;
	
	private Double exchangeRate;
	
	private Date extractDate;
	
	private List<JobBoard> jobBoards;
	
	private String currencyCode;
	
	private Settings() {
		hostName = System.getProperty("gts.hostName");
		load(hostName);

		String jobBoardsStr = System.getProperty("gts.jobBoards");
		if (jobBoardsStr != null) {
			logOption("jobBoards", jobBoardsStr);
			this.jobBoards = loadJobBoards(jobBoardsStr);
		}
	}
	
	public String getHostName() {
		return hostName;
	}
	
	public String getRpmId() {
		return rpmId;
	}

	public String getLocalProfileId() {
		return localProfileId;
	}

	public String getRegionalProfileId() {
		return regionalProfileId;
	}
	
	public String getCountryCode() {
		return countryCode;
	}

	public String getBrand() {
		return brand;
	}

	public Double getExchangeRate() {
		return exchangeRate;
	}

	public void setExchangeRate(Double exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	
	public Date getExtractDate() {
		if (extractDate == null) {
			extractDate = DatabaseManager.INSTANCE.getExtractDate();
		}
		
		return extractDate;
	}


	public List<JobBoard> getJobBoards() {
		return this.jobBoards;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	@SuppressWarnings("unchecked")
	public String getOptionValue(String option) {
		String[] path = option.split(":");
		
		Map<String, Object> map = settings;
		for (int i = 0; i < path.length; i++) {
			Object obj = map.get(path[i]); 
			if (obj instanceof String && i == path.length-1) {
				return (String)obj; 
			} else {
				map = (Map<String, Object>)obj; 
				if (map == null) {
					break;
				}
			}
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public List<Map<String, Object>> getOptionArray(String option) {
		String[] path = option.split(":");

		Map<String, Object> map = settings;
		for (int i = 0; i < path.length; i++) {
			if (map != null) {
				Object obj = map.get(path[i]); 
				if (obj instanceof List && i == path.length-1) {
					return (List<Map<String, Object>>)obj; 
				} else {
					map = (Map<String, Object>)obj; 
				}
			}
		}

		return null;
	}


	private void load(String hostName) {
		String jsonText = Util.readTextFile("c:\\GTS\\Settings\\" + hostName + "\\settings.json");
		settings = new Gson().fromJson(jsonText, new TypeToken<HashMap<String, Object>>() {}.getType());		
		validateSettings();
	}
	
	private void validateSettings() {
		rpmId = validateOption("rpmId");
		currencyCode = validateOption("currencyCode");
		localProfileId = validateOption("localProfileId");
		regionalProfileId = validateOption("regionalProfileId");
		countryCode = Util.getCountryCodeFromHostName(hostName);
		brand = Util.getBrandFromHostName(hostName);
		
		if (Util.isEmpty(countryCode) || Util.isEmpty(brand)) {
			throw new RuntimeException("Unable to determine countryCode or brand from hostname [hostName: " + hostName);
		}
		
		if (rpmId == null || localProfileId == null || regionalProfileId == null || currencyCode == null) {
			throw new RuntimeException("All required options have to be provided.");
		}
	}
	
	private String validateOption(String option) {
		String value = getOptionValue(option);
		if (value == null) {
			logger.error("Initialising option {}: cannot be empty, please check configuration in settings.json file.", option);
		} else {
			logOption(option, value);
		}
		return value;
	}
	
	private void logOption(String option, String value) {
		logger.info("Initialising option {} [{}]", option, value);
	}

	private List<JobBoard> loadJobBoards(String jobBoardsStr) {
		List<JobBoard> result = new ArrayList<JobBoard>();

		CSVReader reader = null;
		try {
			reader = new CSVReader(new StringReader(jobBoardsStr.replaceAll("\\|\\|", "\n")));
			String[] nextLine;
			while ((nextLine = reader.readNext()) != null) {
				if ((nextLine != null) && (nextLine.length != 0)) {
					JobBoard jobBoard = new JobBoard();
					jobBoard.setName(nextLine[0].trim());
					String nonNextGen = nextLine[1].trim();
					jobBoard.setNonNextGen((nextLine[1] != null) && ((nonNextGen.equals("1"))
							|| (nonNextGen.equalsIgnoreCase("true")) || (nonNextGen.equalsIgnoreCase("yes"))));
					result.add(jobBoard);
				}
			}
			return result;
		} catch (Throwable t) {
			throw new RuntimeException(t);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}

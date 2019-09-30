package au.com.michaelpage.gap.common.google;

import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import au.com.michaelpage.gap.common.util.Util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;


public class GoogleHitsUploader {
	
	private static final Logger logger = LoggerFactory.getLogger(GoogleHitsUploader.class);
	
	private static final String ENDPOINT_URL = "https://ssl.google-analytics.com/collect";
	private static final String ENDPOINT_DEBUG_URL = "https://ssl.google-analytics.com/debug/collect";
	private static final String USER_AGENT = "GTSHitsUploader/1.0";
	
	public void upload(String fileName) {
		GoogleUploaderDao googleUploaderDao = new GoogleUploaderDao();
		Map<Integer, String> hits = googleUploaderDao.findOutstandingHits(fileName);
		
		int total = hits.size();
		int failed = 0;
		
		logger.info("Started uploading hits file {}. Number of hits: {}", fileName, total);
		
		try {
			for (Map.Entry<Integer, String> entry : hits.entrySet()) {
		    	boolean valid = validateAndUploadHit(entry.getValue());
		    	if (!valid) {
		    		failed++;
		    		googleUploaderDao.updateHitUploadStatus(entry.getKey(), false);
		    	} else {
		    		googleUploaderDao.updateHitUploadStatus(entry.getKey(), true);
		    	}
			}

			if (failed == 0) {
				googleUploaderDao.updateFileUploadStatus(fileName, true);
			}
		
		} catch (Exception e) {
			logger.error("An error occurred during uploading of hits file {}. {}", fileName, e.getMessage());
			logger.debug("ERROR: {}", e.getMessage(), e);
		}
		
		if (failed > 0) {
			logger.error("There are {} failed hits, please check log file for details", failed);
		}
		
		logger.info("Finished uploading hits file {} [total: {}; failed: {}]", fileName, total, failed);
	}
	

	private boolean validateAndUploadHit(String data) {
		try {
			// Only revenue hits will be validated
			boolean valid = validateHit(data);
			if (valid) {
				uploadHit(data);
				return true;
			}
		} catch (Exception e) {
			logger.debug("Error occurred while uploading hit {}. Message: {}", data, e.getMessage(), e);
		}
		
		return false;
	}
	
	private String generateHitData(String[] header, String[] data) {
		StringBuilder s = new StringBuilder();
		
		for (int i = 0; i < header.length; i++) {
			if (Util.isEmpty(header[i])) {
				continue;
			}
			
			try {
				s.append(header[i]).append("=").append(URLEncoder.encode(data[i].trim(), "UTF-8")).append("&");
			} catch (UnsupportedEncodingException e) {
				// UTF-8 is valid format and this exception should never be thrown
				e.printStackTrace(); // just in case
			}
		}
		
		s.deleteCharAt(s.length()-1); // remove the last "&"
		
		return s.toString();
	}
	
	private boolean validateHit(String data) {
		if (!data.contains("pa=purchase")) {
			return true;
		}
		
		JsonElement json = null;
		try {
			URL url = new URL(ENDPOINT_DEBUG_URL);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	 
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
	 
			con.setDoOutput(true);
			DataOutputStream os = null;
			try {
				os = new DataOutputStream(con.getOutputStream());
				os.writeBytes(data);
			} catch (Exception e) {
				logger.debug("ERROR: " + e.getMessage(), e);
				throw new RuntimeException(e);
			} finally {
				if (os != null) {
					os.flush();
					os.close();
				}
			}
	 
			int responseCode = con.getResponseCode();
			
			if (responseCode == 200) {
				json = new JsonParser().parse(new InputStreamReader(con.getInputStream()));
				boolean valid = json.getAsJsonObject().get("hitParsingResult").getAsJsonArray().get(0).
						getAsJsonObject().get("valid").getAsBoolean();
				boolean parserMessageExists = json.getAsJsonObject().get("hitParsingResult").getAsJsonArray().get(0).
						getAsJsonObject().get("parserMessage").getAsJsonArray().size() != 0;
				
				if (parserMessageExists) {
					if (valid) {
						logger.debug("WARN: Validation successful but warnings exist for data string '{}': " + json.getAsString(), data);
					} else {
						logger.debug("ERROR: Validation failed for data string '{}': " + json.getAsString(), data);
					}
				}
			} else {
				logger.debug("ERROR: Invalid response code for data string '{}'", data);
				return false;
			}
			
			return true;
			 
		} catch (Exception e) {
			logger.debug("ERROR: An exception occured during validation for data string '{}': " + 
					(json == null ? null : json.toString()), data, e);
		}
		
		return false;
		
	}	

	private void uploadHit(String data) {
		try {
			URL url = new URL(ENDPOINT_URL);
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
	 
			con.setRequestMethod("POST");
			con.setRequestProperty("User-Agent", USER_AGENT);
			con.setDoOutput(true);
			
			DataOutputStream os = null;
			try {
				os = new DataOutputStream(con.getOutputStream());
				os.writeBytes(data);
				os.flush();
			} finally {
				if (os != null) {
					os.close();
				}
			}
			
			con.getResponseCode();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
	
	public void uploadWithoutDatabase(String fileName) {
		logger.info("Started uploading hits file {}", fileName);
		
		int total = 0;
		int failed = 0;
		
		List<String> failedList = new ArrayList<String>();
		
		CSVReader reader = null;
		try {		            
		    reader = new CSVReader(new FileReader(fileName));
		    String[] header = reader.readNext();
		    String[] data;
		    while ((data = reader.readNext()) != null) {
		    	total++;
		    	String hitData = generateHitData(header, data);
		    	boolean valid = validateAndUploadHit(hitData);
		    	if (!valid) {
		    		failedList.add(hitData);
		    		failed++;
		    		logger.error("FAILED: Record #" + (total + 1));
		    	}
		    	if (((total + 1) % 500) == 0)
		    	logger.debug("Record #" + (total + 1));
		    }
		} catch (Exception e) {
			logger.error("An error occurred during uploading of hits file {}. {}", fileName, e.getMessage());
			logger.debug("ERROR: {}", e.getMessage(), e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		
		if (failed > 0) {
			logger.error("There are {} failed hits, trying to re-upload", failed);
			
				try {
					for (String hitData : failedList) {
				    	
						boolean valid = validateAndUploadHit(hitData);
				    	if (!valid) {
				    		failed++;
				    	}
					}
				} catch (Exception e) {
					logger.error("An error occurred during uploading of hits file {}. {}", fileName, e.getMessage());
					logger.debug("ERROR: {}", e.getMessage(), e);
				}
			
		}
		
		logger.info("Finished uploading hits file {} [total: {}; failed: {}]", fileName, total, failed);
	}
	
	
	public static void main(String[] args) throws Exception {
		GoogleHitsUploader googleHitsUploader = new GoogleHitsUploader();
		googleHitsUploader.uploadWithoutDatabase("c:\\GTS\\Output\\RPM\\pagepersonnel.fr\\FEE_PAID_TEMP_PAGEVIEW_PPFR.csv");
	}
	
}

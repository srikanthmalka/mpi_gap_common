package au.com.michaelpage.gap.common.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class CurrencyExchangeUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(CurrencyExchangeUtil.class);
	
	private static final String APP_ID = "9edda2b0ca9c4ceeb30c95177ecadb67";
	
	public static void main(String[] args) throws Exception {
		//System.out.println(convertToGBP("HKD", "300"));
		//System.out.println(convertToGBP("AUD", "300"));
		//System.out.println(getRateToGBP("AUD", new SimpleDateFormat("yyyy-MM-dd").parse("2018-05-01")));
		//System.out.println(getRateToGBP("AUD", new SimpleDateFormat("yyyy-MM-dd").parse("2018-08-01")));
	}
	
	public static String convertToGBP(String from, String amount, Date date) {
		return getRateTo(amount, from, "GBP", date);
	}

	public static String getRateTo(String amount, String from, String to, Date date) {
		String result = null;
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
		String request = "https://openexchangerates.org/api/historical/" + dateStr + ".json?app_id=" + APP_ID + "&base=" + from + "&symbols=" + to;
		logger.debug("Sending request..." + request);
		int count = 0;
		int maxTries = 3;
		while (true) {
			try {
				String pageContent = getPageContent(request);
				Map<String, Object> exchangeInfo = new Gson().fromJson(pageContent, 
						new TypeToken<HashMap<String, Object>>() {}.getType());
				
				Map<String, Double> rates = (Map<String, Double>)exchangeInfo.get("rates");
				
				Double rate = rates.get(to);
				
				DecimalFormat df = new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
				df.setMaximumFractionDigits(340); // 340 = DecimalFormat.DOUBLE_FRACTION_DIGITS

				logger.info("Exchange rate of {} to {} on {} is {}.", from, to, dateStr, df.format(rate));
				
				result = df.format(rate * new Double(amount));
				
				if (result != null) break;
			} catch (Exception e) {
				logger.error("Error occurred while converting amount {} from {} to {} on {}  try-count {}", amount, from, to, dateStr, count + 1, e);
			}
			if (++count == maxTries) break;
		}
		logger.debug("Exchange rate of {} {} = {} {} on {}", amount, from, result, to, dateStr);
		return result;
	}
	
	public static String getRateToGBP(String from, Date date) {
		return getRateTo("1", from, "GBP", date);
	}
	
	private static String getPageContent(String url) throws Exception {
		
		URL obj = new URL(url);
		HttpsURLConnection conn = (HttpsURLConnection) obj.openConnection();

		// default is GET
		conn.setRequestMethod("GET");

		conn.setUseCaches(false);

		// act like a browser
		conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.81 Safari/537.36");
		conn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		int responseCode = conn.getResponseCode();
		logger.debug("Sending 'GET' request to URL : <{}>", url);
		logger.debug("Response Code : <{}>", responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				conn.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		return response.toString();

	}
}

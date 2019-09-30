package au.com.michaelpage.gap.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {
	
	private static final Logger logger = LoggerFactory.getLogger(Util.class);
	
	public static String getTimestamp() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
		return sdf.format(new Date());
	}

	public static boolean isEmpty(String s) {
		return s == null || s.length() == 0;
	} 
	
	public static String readTextFile(String fileName) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));
		    StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		        sb.append(line);
		        sb.append(System.getProperty("line.separator"));
		        line = br.readLine();
		    }
		    return sb.toString();
		} catch (Exception e) { 
			throw new RuntimeException(e);
		} finally {
		    if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		}	
	}

	public static void writeTextFile(String fileName, String content) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(fileName));
			bw.write(content);
		} catch (Exception e) { 
			throw new RuntimeException(e);
		} finally {
		    if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		    }
		}	
	}
	
	public static void delete(String fileName) {
		
		try {
			File file = new File(fileName);
			
			if (!file.exists()) {
				return;
			}

	    	if (file.isDirectory()) {
	    		 
	    		//directory is empty, then delete it
	    		if (file.list().length==0) {
	 
	    		   if (file.delete()) {
	    			   logger.debug("Directory is deleted : {}", file.getAbsolutePath());
	    		   } else {
	    			   logger.error("Can't delete directory : {}", file.getAbsolutePath());
	    		   }
	    		   
	    		} else {
	 
	    		   //list all the directory contents
	        	   String files[] = file.list();
	 
	        	   for (String temp : files) {
	        	      //construct the file structure
	        	      File fileDelete = new File(file, temp);
	 
	        	      //recursive delete
	        	     delete(fileDelete.getAbsolutePath());
	        	   }
	 
	        	   //check the directory again, if empty then delete it
	        	   if (file.list().length == 0) {
	        		   if (file.delete()) {
	        			   logger.debug("Directory is deleted : {}", file.getAbsolutePath());
	        		   } else {
	        			   logger.error("Can't delete directory : {}", file.getAbsolutePath());
	        		   }
	        	     
	        	   }
	    		}
	 
	    	} else {
	    		//if file, then delete it
	    		if (file.delete()) {
	    			logger.debug("File is deleted : {}", file.getAbsolutePath());
	    		} else {
	    			logger.error("Can't delete file : {}", file.getAbsolutePath());
	    		}
	    	}
    	
		} catch (Exception e) {
			throw new RuntimeException("Unable to delete " + fileName + ". Message: " + e.getMessage(), e);
		}
    }
	
	public static void unzip(String zipFile, String destinationFolder) {
		byte[] buffer = new byte[1024];
		
		try {
			logger.info("Unzipping file {} to folder {}", zipFile, destinationFolder);
			
			File destination = new File(destinationFolder);
			if (!destination.exists()) {
				destination.mkdirs();
			}
			
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry ze = zis.getNextEntry();
	    	
			while (ze != null) {
				String fileName = ze.getName();
				File newFile = new File(destinationFolder + File.separator + fileName);
				logger.info("Unzip: " + newFile.getAbsoluteFile());
				
				new File(newFile.getParent()).mkdirs();
	  
				FileOutputStream fos = new FileOutputStream(newFile);             
	  
				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}
	  
				fos.close();   
				ze = zis.getNextEntry();
	     	}
	  
			zis.closeEntry();
			zis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean moveFile(String fileName, String destinationFolder) {
		try {
			File file = new File(fileName);
			new File(destinationFolder).mkdirs();
			return file.renameTo(new File(destinationFolder + file.getName()));
		} catch (Exception e) {
			logger.debug("Can't move file {} to {}. Message: {}", fileName, destinationFolder, e.getMessage(), e);
			throw new RuntimeException("Can't archive file " + fileName);
		}
	}

	public static boolean renameFile(String fileName, String destinationFileName) {
		try {
			File file = new File(fileName);
			File destinationFile = new File(destinationFileName);
			File parent = destinationFile.getParentFile();
			parent.mkdirs();
			return file.renameTo(destinationFile);
		} catch (Exception e) {
			logger.debug("Can't rename file {} to {}. Message: {}", fileName, destinationFileName, e.getMessage(), e);
			throw new RuntimeException("Can't rename file " + fileName + ". Check log files for more detail.");
		}
	}
	
	public static String extractFileName(String fullFileName) {
		int pos1 = fullFileName.lastIndexOf("\\");
		int pos2 = fullFileName.lastIndexOf(".");
		return fullFileName.substring(pos1 + 1, pos2);
	}

	public static String extractFilePath(String fullFileName) {
		int pos = fullFileName.lastIndexOf("\\");
		return fullFileName.substring(0, pos);
	}
	
	public static int getRandom(int n) {
		Random rand = new Random();
		return rand.nextInt(n) + 1;		
	}
	
	public static String objectToJson(Object obj) {
		Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().serializeNulls().create();
		return gson.toJson(obj);
	}
	
	public static String getBrandFromHostName(String hostName) {
		if (hostName.startsWith("michaelpage")) {
			return "MP";
		} else if (hostName.startsWith("pagepersonnel")) {
			return "PP";
		} else {
			throw new RuntimeException("Incorrect host name " + hostName + ". It should start with michaelpage or pagepersonnel.");
		}
	}

	public static String getCountryCodeFromHostName(String hostName) {
		return hostName.endsWith("michaelpageafrica.com") ? "ZA" : hostName.endsWith("com") ? "US" : hostName.substring(hostName.lastIndexOf(".") + 1).toUpperCase();
	}
	
	public static Double getExchangeRate(String currencyCode, Date date) {
		if ("GBP".equalsIgnoreCase(currencyCode)) {
			return 1d;
		} else {
			Double rate = 0d;
			String rateStr = CurrencyExchangeUtil.getRateToGBP(currencyCode, date);
			if (!Util.isEmpty(rateStr)) {
				try {
					rate = Double.valueOf(rateStr);
				} catch (NumberFormatException e) {
					logger.error("Unable to convert {} exchange rate of {} to float value.", currencyCode, rateStr);
					throw new RuntimeException("Unable to get exchange rate");
				}
			}
			
			if (rate <= 0) {
				logger.error("Unable to obtain exchange rate of {}.", currencyCode);
				throw new RuntimeException("Unable to get exchange rate");
			} else {
				return rate;
			}
			
		}
	}

	public static Double getExchangeRate(String currencyCodeFrom, String currencyCodeTo, Date date) {
		if ("GBP".equalsIgnoreCase(currencyCodeFrom) && "GBP".equalsIgnoreCase(currencyCodeTo)) {
			return 1d;
		} else {
			Double rate = 0d;
			String rateStr = CurrencyExchangeUtil.getRateTo("1", currencyCodeFrom, currencyCodeTo, date);
			if (!Util.isEmpty(rateStr)) {
				try {
					rate = Double.valueOf(rateStr);
				} catch (NumberFormatException e) {
					logger.error("Unable to convert {} exchange rate of {} to {} to float value.", currencyCodeFrom, currencyCodeTo, rateStr);
					throw new RuntimeException("Unable to get exchange rate");
				}
			}
			
			if (rate <= 0) {
				logger.error("Unable to obtain exchange rate of {} to {}.", currencyCodeFrom, currencyCodeTo);
				throw new RuntimeException("Unable to get exchange rate");
			} else {
				return rate;
			}
		}
	}
	
	public static Timestamp strToDate(String date, String format) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return new Timestamp(sdf.parse(date).getTime());
	}
	
	public static void main(String[] args) {
		//System.out.println(getExchangeRate("AUD"));
	}
}

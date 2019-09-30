package au.com.michaelpage.gap.common.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

public class HashUtil {

	private static final Logger logger = LoggerFactory.getLogger(HashUtil.class);

	public static void main(String[] args) throws Exception {
		
		String baseFolder = "c:\\temp\\RAM\\";
		
		logger.info("Using base folder {}", baseFolder);
		
		Set<String> allFiles = findAllDataFiles(baseFolder);
		
		logger.info("Found {} files", allFiles.size());
		
		for (String fileName : allFiles) {
			
			int total = 0;
			
			logger.info("Hashing file {}", fileName);
			
			CSVReader reader = null;
			
			List<String[]> modifiedData = new ArrayList<String[]>();
			
			try {		            
			    reader = new CSVReader(new FileReader(fileName));
			    String[] header = reader.readNext();
			    String[] headerModified = modifyHeader(header);
			    modifiedData.add(headerModified);
			    
			    int uidPosition = getIndexOfElement(headerModified, "uid");
			    
			    String[] data;
			    while ((data = reader.readNext()) != null) {
			    	total++;
			    	String[] dataModified = modifyData(data, uidPosition);
			    	modifiedData.add(dataModified);
			    }
			    
			    createCsvFile(modifiedData, renameFile(fileName));
			    
			    logger.info("Processed file {} with {} records", fileName, total);
			    
			} catch (Exception e) {
				logger.error("An error occurred during processing file {}. {}", fileName, e.getMessage());
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

		}
		
	}
	
	private static String[] modifyHeader(String[] header) {
		List<String> modifiedHeader = new ArrayList<String>();
		
		for (String s : header) {
			modifiedHeader.add(s);
			if (s.equals("uid")) {
				modifiedHeader.add("cid");
				modifiedHeader.add("cd2");
			}
		}
		
		return modifiedHeader.toArray(new String[modifiedHeader.size()]);
	}
	
	private static String[] modifyData(String[] data, int uidPosition) {
		List<String> modifiedData = new ArrayList<String>();
		
		for (int i = 0; i < data.length; i++) {
			if (i != uidPosition) {
				modifiedData.add(data[i].trim());
			} else {
				modifiedData.add(Md5Util.hash(data[i].trim()));
				modifiedData.add(Md5Util.hash(data[i].trim()));
				modifiedData.add(Md5Util.hash(data[i].trim()));
			}
		}
		
		return modifiedData.toArray(new String[modifiedData.size()]);
	}
	
	private static int getIndexOfElement(String[] header, String element) {
		for (int i = 0; i < header.length; i++) {
			if (element.equals(header[i])) {
				return i;
			}
		}
		return -1;
	}
	
	private static String renameFile(String fileName) {
		String name = Util.extractFileName(fileName);
		String folder = Util.extractFilePath(fileName);
		return folder + "\\" + name + ".hashed.csv";
	}
	
	private static void createCsvFile(List<String[]> data, String fileName) throws Exception {
		PrintWriter pw = new PrintWriter(new File(fileName));
		
		try {
			for (String[] s : data) {
				pw.write(String.join(",", s) + "\r\n");
			}
		} finally {
			pw.close();
		}
	}
	
	private static Set<String> findAllDataFiles(String base) throws IOException {
		File folder = new File(base);
		File[] files = folder.listFiles();
		Set<String> set = new TreeSet<String>();

		if (files != null && files.length > 0) {
			for (File file : files) {
				if (!file.isDirectory() && file.getCanonicalPath().endsWith("csv") 
						&& !file.getCanonicalPath().endsWith(".hashed.csv")) {
					set.add(file.getCanonicalPath());
				}
			}
		}
		
		return set;
	}
	
}

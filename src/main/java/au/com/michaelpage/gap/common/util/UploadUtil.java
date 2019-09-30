package au.com.michaelpage.gap.common.util;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.michaelpage.gap.common.google.GoogleHitsUploader;

public class UploadUtil {

	private static final Logger logger = LoggerFactory.getLogger(UploadUtil.class);
	
	public static void main(String[] args) throws Exception {

		String baseFolder = "c:\\temp\\RAM_hashed\\";
		
		logger.info("Using base folder {}", baseFolder);
		
		Set<String> allFiles = findAllDataFiles(baseFolder);
		
		logger.info("Found {} files", allFiles.size());
		
		for (String fileName : allFiles) {
			
			logger.info("Uploading file {}", fileName);
			GoogleHitsUploader googleHitsUploader = new GoogleHitsUploader();
			googleHitsUploader.uploadWithoutDatabase(fileName);
			Util.renameFile(fileName, fileName.replace(".hashed.csv", ".uploaded.csv"));
		}
		
	}
	
	private static Set<String> findAllDataFiles(String base) throws IOException {
		File folder = new File(base);
		File[] files = folder.listFiles();
		Set<String> set = new TreeSet<String>();

		if (files != null && files.length > 0) {
			for (File file : files) {
				if (!file.isDirectory() && file.getCanonicalPath().endsWith(".hashed.csv") 
						&& !file.getCanonicalPath().endsWith(".uploaded.csv")) {
					set.add(file.getCanonicalPath());
				}
			}
		}
		
		return set;
	}
	
}

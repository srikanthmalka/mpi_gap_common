package au.com.michaelpage.gap.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

public class CsvFileCombineUtil {

	public static void main(String[] args) throws Exception {
		
		String hostName = System.getProperty("gts.hostName");
		String workingFolder = System.getProperty("gts.workingFolder");
		String brandCountry = System.getProperty("gts.brandCountry");
		String[] fileNames = System.getProperty("gts.fileNames").split(",");
		
		System.out.println("Hostname: " + hostName);
		System.out.println("Working Folder: " + workingFolder);
		System.out.println("Brand Country: " + brandCountry);
		System.out.println("Filenames: " + Arrays.toString(fileNames));
		
		for (String fileName : fileNames) {
			String outputFolder = (!Util.isEmpty(workingFolder) ? workingFolder : "c:\\GTS\\Output\\RPM\\" + hostName) + "\\";
			combineFiles(outputFolder + fileName.trim() + "_" + brandCountry + ".csv", 
					findAllDataFiles(outputFolder, fileName.trim() + ".csv"));
		}
		
	}
	
	private static Set<String> findAllDataFiles(String base, String fileName) throws IOException {
		File folder = new File(base);
		File[] files = folder.listFiles();
		Set<String> set = new TreeSet<String>();

		if (files != null && files.length > 0) {
			for (File file : files) {
				if (!file.isDirectory() && file.getCanonicalPath().endsWith(fileName)) {
					set.add(file.getCanonicalPath());
				} else {
					set.addAll(findAllDataFiles(file.getCanonicalPath(), fileName));
				}
			}
		}
		
		return set;
	}

	private static void combineFiles(String destFile, Set<String> sourceFiles) throws IOException {
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(destFile), "UTF-8"));
		
			int i = 0;
			for (String file : sourceFiles) {
				try {
					br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
					
					String line = null;
					boolean headerLine = true;
					while ((line = br.readLine()) != null) {
						if (i != 0 && headerLine) {
							headerLine = false;
							continue;
						} else {
							bw.append(line);
							bw.append("\r\n");
							bw.flush();
						}
					}
				} finally {
					if (br != null) {
						br.close();
					}
				}
			
				i++;
			}

		} finally {
			if (bw != null) {
				bw.close();
			}
		}
		
	}
	
}

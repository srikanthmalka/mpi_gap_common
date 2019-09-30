package au.com.michaelpage.gap.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class FilePrepend {
	
	private String source;

	public FilePrepend(String source) {
		super();
		this.source = source;
	}
	
	public void prepend(String prepend, String target, String encoding) throws IOException {
		File output = new File(target);
		
		// Create all nested folders
		File path = new File(output.getParent()); 
		if (!path.exists()) {
			path.mkdirs();
		}
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		try {
			
			String sCurrentLine;
			FileInputStream inputStream = new FileInputStream(source);
			br = new BufferedReader(new InputStreamReader(inputStream, encoding));
			
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output), encoding));
			
			bw.write(prepend);
			bw.flush();
			
			while ((sCurrentLine = br.readLine()) != null) {
				bw.write(sCurrentLine);
				bw.write("\r\n");
				bw.flush();
			}
 
		} finally {
			if (br != null) {
				br.close();
			}
			if (bw != null) {
				bw.close();
			}
		}
	}

}

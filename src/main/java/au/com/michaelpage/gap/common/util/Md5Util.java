package au.com.michaelpage.gap.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.hash.Hashing;

public class Md5Util {

	public static String hash(String input) {
		String lowerCaseInput = input.trim().toLowerCase();
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.update(lowerCaseInput.getBytes("UTF-8"), 0, lowerCaseInput.length());
			String s = String.format("%032x", new BigInteger(1, m.digest()));
			StringBuilder output = new StringBuilder();
			for (int i = 0; i < s.length(); i++) {
				output.append(s.charAt(i));
//				if (i == 7 || i == 11 || i == 15 || i == 19) {
//					output.append("-");
//				}
			}
			return output.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

		}
		return null;
	}

	public static String hashSha256(String input) {
		String lowerCaseInput = input.toLowerCase().trim();
		try {
			return Hashing.sha256().hashString(lowerCaseInput, StandardCharsets.UTF_8).toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
		return null;
	}
	
	public static void main(String[] args) {
		
//		System.out.println(hash(null));
//		System.exit(0);
//		System.out.println(hash(""));
		
//		System.out.println(hashSha256("Steve.Claxton@abpwessex.com"));
//		System.out.println(hashSha256("steve.claxton@abpwessex.com"));
//		System.exit(0);
		
		BufferedReader br = null;
		BufferedWriter bw = null;
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream("c:\\Temp\\1.csv"), "UTF-8"));
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("c:\\Temp\\_.csv"), "UTF-8"));
					
					String line = null;
					while ((line = br.readLine()) != null) {
						bw.append(line + "," + hash(line));
						bw.append("\r\n");
						bw.flush();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (bw != null) {
						try {
							bw.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			
		
		
	}
}

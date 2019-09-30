package au.com.michaelpage.gap.common.util.derby;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import au.com.michaelpage.gap.common.util.Util;

public class StringUtil {

	
	public static void main(String[] args) {
		// test
		//System.out.println("13442767/001".indexOf("/") == charIndex("/", "13442767/001"));
		//System.out.println("13442767001".indexOf("/") == charIndex("/", "13442767001"));
		
		System.out.println(StringUtil.substringBefore("13442767/001", "/"));
		System.out.println(StringUtil.substringBefore("13442767001", "/"));
		System.out.println(ordinalIndexOf("HRGlasgow.mpScotland.pageuk", ".", 2));
	}
	
	public static String substringBefore(String value, String a) {
		int posA = value.indexOf(a);
		if (posA == -1) {
		    return value;
		}
		return value.substring(0, posA);
	}

	public static int indexOf(String value, String a) {
		int posA = value.indexOf(a);
		if (posA == -1) {
		    return 0;
		}
		return posA+1;
	}

	public static int ordinalIndexOf(String str, String a, int occurrence) {
	    if (Util.isEmpty(str) || Util.isEmpty(a)) {
	    	return 0;
	    }
		int pos = str.indexOf(a, 0);
		int n = occurrence-1;
	    while (n-- > 0 && pos != -1)
	        pos = str.indexOf(a, pos+1);
	    return pos+1;
	}
	
	public static String replace(String value, String a, String b) {
		return value == null? "" : value.replaceAll(a, b);
	}

	public static Timestamp strToDate(String date, String format) throws Exception {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return new Timestamp(sdf.parse(date).getTime());
	}
	
}

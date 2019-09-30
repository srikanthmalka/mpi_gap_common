package au.com.michaelpage.gap.common.util;

import java.beans.PropertyDescriptor;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

public class SQLGeneratorHelper {
	
	public static String generateCreateTable(Class clazz) throws Exception {
		return generateCreateTable(PropertyUtils.getPropertyDescriptors(clazz), clazz.getSimpleName().toUpperCase());
	}
	
	public static String generateCreateTable(PropertyDescriptor[] propertyDescriptors, String tableName) {
		StringBuilder sb = new StringBuilder();
		StringBuilder sbColumns = new StringBuilder();
		for (PropertyDescriptor pd : propertyDescriptors) {
			if (pd.getName().equalsIgnoreCase("class")) continue; //skip
		
//			if (pd.getPropertyType() == Integer.class) {
//				sb.append(String.format("%s integer, ", pd.getName()));
//			} else 
			if (pd.getPropertyType() == java.util.Date.class) {
				sb.append(String.format("%s timestamp, ", pd.getName()));
			} else if (pd.getPropertyType() == Double.class) {
				sb.append(String.format("%s decimal(20,12), ", pd.getName()));
			} else if (pd.getPropertyType() == Float.class) {
				sb.append(String.format("%s decimal(20,2), ", pd.getName()));
			} else {
				sb.append(String.format("%s varchar(%d), ", pd.getName(), 32672));
			}
			
			sbColumns.append(String.format("%s,", pd.getName()));
		}
		return String.format("CREATE TABLE %s (%s)",tableName.toUpperCase(), sb.substring(0, sb.length() - 2));
	}
	
	public static String generateCreateTable(List<String> describe, String tableName) throws Exception {
		StringBuilder sb = new StringBuilder();
		StringBuilder sbColumns = new StringBuilder();
		for (String key: describe) {
			if (key.equalsIgnoreCase("class")) continue; //skip
			sb.append(String.format("%s varchar(%d), ", key, 32672));
			sbColumns.append(String.format("%s,", key));
		}
		return String.format("CREATE TABLE %s (%s)", tableName.toUpperCase(), sb.substring(0, sb.length() - 2));
	}
	
	public static String generateInsert(Class clazz) throws Exception {
		StringBuilder sbColumns = new StringBuilder();
		StringBuilder sbPlaceholders = new StringBuilder();
		
		PropertyDescriptor[] pds = PropertyUtils.getPropertyDescriptors(clazz);	
		for (PropertyDescriptor pd: pds) {
			if (pd.getName().equalsIgnoreCase("class")) continue; //skip
			sbColumns.append(String.format("%s,", pd.getName()));
			sbPlaceholders.append("?,");
		}
		//System.out.println(String.format("INSERT INTO %s (%s) VALUES (%s)", clazz.getSimpleName().toUpperCase(),sbColumns.substring(0, sbColumns.length() - 1), sbPlaceholders.substring(0, sbPlaceholders.length() - 1)));
		return String.format("INSERT INTO %s (%s) VALUES (%s)", clazz.getSimpleName().toUpperCase(),sbColumns.substring(0, sbColumns.length() - 1), sbPlaceholders.substring(0, sbPlaceholders.length() - 1));
	}
}

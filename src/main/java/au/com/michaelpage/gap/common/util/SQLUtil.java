package au.com.michaelpage.gap.common.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.apache.commons.beanutils.PropertyUtils;

public class SQLUtil {
	
	public static void main(String[] args) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
//		Person person = new Person();
//		person.setCreateTimestamp(new Date());
//		person.setEmailAddress("1");
//		person.setMd5EmailAddress("2");
//		person.setPersonRef(1);
//		populatePS(person);
		//System.out.println(PropertyUtils.getPropertyDescriptors(person));
//		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(person);
//		for (PropertyDescriptor pd : propertyDescriptors) {
//			
//		}
		
		
	}
	
	
	public static void populatePS(PreparedStatement ps, Object obj) throws Exception {
		PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(obj);
		int index = 1;
		for (PropertyDescriptor pd : propertyDescriptors) {
			
			if (pd.getName().equalsIgnoreCase("class")) continue;
			
			Object property = PropertyUtils.getProperty(obj, pd.getName());
			
			if (pd.getPropertyType() == java.util.Date.class) {
				Date date = (Date)property;
				ps.setTimestamp(index++, date != null ? new Timestamp(date.getTime()) : null);
			} else if (pd.getPropertyType() == Float.class) {
				if (property != null) {
					ps.setFloat(index++, property != null ? (Float)property : null);
				} else {
					ps.setNull(index++, Types.DECIMAL);
				}
			} else {
				ps.setString(index++, property != null ? String.valueOf(property) : null);
			}
		}
	}
	
//	public static void populatePS(PreparedStatement ps, Map<String,Object> row) throws SQLException {
//		int index = 1;
//		for (Entry<String,Object> column : row.entrySet()) {
//			if (column.getKey().equalsIgnoreCase("class")) continue;
//			
//			//System.out.println("Column: <" + column.getKey() + "> <" + column.getValue() + "> <" + column.getValue().getClass() + ">");			
//			
//			if (column.getValue() instanceof java.util.Date) {
//				
//				Date date = (Date)column.getValue();
//				//System.out.println(date);
//				ps.setTimestamp(index++, new Timestamp(date.getTime()));
//			} else {
//				ps.setString(index++, column.getValue() != null ? String.valueOf(column.getValue()) : null);
//			}
//		}
//	}
}

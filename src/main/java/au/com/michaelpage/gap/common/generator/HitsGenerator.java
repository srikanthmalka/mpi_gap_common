package au.com.michaelpage.gap.common.generator;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.michaelpage.gap.common.Settings;
import au.com.michaelpage.gap.common.util.DatabaseManager;
import au.com.michaelpage.gap.common.util.DeleteDirUtil;
import au.com.michaelpage.gap.common.util.FilePrepend;
import au.com.michaelpage.gap.common.util.Util;

public class HitsGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(HitsGenerator.class);
	
	private String outputFolder;
	
	public HitsGenerator(String outputFolder) {
		this.outputFolder = outputFolder;
	}
	
	public void generate(DataOrigin dataOrigin, boolean includeBrandCondition) throws GeneratorException {
		Connection conn = null;
		
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			
			// Combine hits from Hit enum and Json config into one list
			List<HitObj> hits = getHitList(dataOrigin);
			
			for (HitObj hit : hits) {
				String tempFilename = hit.name + "_" + UUID.randomUUID() + ".csv";
				String exportFilename = String.format("%s%s.csv", outputFolder, hit.name);
				
				String query = hit.sql;
				
				logger.info("Generating {} hit CSV file: {}", hit.name, exportFilename);
				
				if (dataOrigin == DataOrigin.BROADBEAN) {
					query = customizeQuery(query, includeBrandCondition); 
				}
				
				PreparedStatement ps = conn.prepareStatement(query);
				System.out.println("query----"+query);
				ResultSet rs = ps.executeQuery();
				
				StringBuilder columnNames = new StringBuilder();
				
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					columnNames.append(String.format("\"%s\",",rs.getMetaData().getColumnName(i).toLowerCase()));
				}
				
				String columnsCommaList = columnNames.substring(0, columnNames.length() - 1);
				logger.debug("Columns: {}", columnsCommaList);
				
				rs.close();
				ps.close();
				
				ps = conn.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY (?,?,?,?,?)");
			    ps.setString(1,query);
			    ps.setString(2,tempFilename);
			    ps.setString(3,",");
			    ps.setString(4,"\"");
			    ps.setString(5,"UTF-8");
			    System.out.println(ps);
			    ps.execute();
				ps.close();
				
				new FilePrepend(tempFilename)
					.prepend(String.format("%s\r\n", columnsCommaList), 
						exportFilename, 
						 "UTF-8");
				
				DeleteDirUtil.delete(new File(tempFilename));
				
				if (!hit.name.contains("VALIDATION_FAILED")) {
					populateUploadTables(conn, exportFilename, columnsCommaList, query);
				}
			}
			
		} catch (Throwable t) {
			throw new GeneratorException("Unable to generate hits data. Message: " + t.getMessage(), t);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(null, null, conn);
		}
		
	}
	
	private String customizeQuery(String query, boolean includeBrandCondition) {
		if (includeBrandCondition) {
			String brand = Util.getBrandFromHostName(Settings.INSTANCE.getHostName());
			String countryCode = Util.getCountryCodeFromHostName(Settings.INSTANCE.getHostName());

			logger.info("Using the following data: brand: {}", brand);
			
			if ("UK".equals(countryCode)) { // UK doesn't seem to follow this naming convention and have 2 separate BB profiles for MP & PP
				return query.replace("${brand_condition}", "");
			}
			
			if ("PP".equals(brand)) {
				return query.replace("${brand_condition}", "and lower(BB_office) like 'pp%'");
			} else {
				return query.replace("${brand_condition}", "and lower(BB_office) not like 'pp%'");
			}
		} else {
			return query.replace("${brand_condition}", "");
		}
	}
	
	private HitObj getQueryFromJson(String name, String dataOrigin) {
		List<Map<String, Object>> queries = Settings.INSTANCE.getOptionArray("queries");
		if (queries != null) {
			for (Map<String, Object> query : queries) {
				if (query == null) {
					continue;
				}
				
				String typeJson = (String)query.get("type");
				String nameJson = (String)query.get("name");
				String dataOriginJson = (String)query.get("dataOrigin");
				String sqlJson = (String)query.get("sql");
				
				if (name.equalsIgnoreCase(nameJson) && dataOrigin.equalsIgnoreCase(dataOriginJson) && "hit".equalsIgnoreCase(typeJson)) {
					HitObj hit = new HitObj();
					hit.name = nameJson.toUpperCase();
					hit.sql = sqlJson;
					return hit;
				}
			}
		}
		return null;
	}
	
	private List<HitObj> getQueriesFromJson(DataOrigin dataOrigin) {
		List<Map<String, Object>> queries = Settings.INSTANCE.getOptionArray("queries");
		
		List<HitObj> result = new ArrayList<HitObj>();
		
		if (queries != null) {
			for (Map<String, Object> query : queries) {
				if (query == null) {
					continue;
				}
				
				String typeJson = (String)query.get("type");
				String nameJson = (String)query.get("name");
				String dataOriginJson = (String)query.get("dataOrigin");
				String sqlJson = (String)query.get("sql");
				
				if (dataOrigin.toString().equalsIgnoreCase(dataOriginJson) && "hit".equalsIgnoreCase(typeJson)) {
					HitObj hit = new HitObj();
					hit.name = nameJson.toUpperCase();
					hit.sql = sqlJson;
					result.add(hit);
				}
			}
		}
		
		return result;
	}
	
	private void populateUploadTables(Connection conn, String fileName, String columns, String query) throws Exception {
		String insertFileSql = "insert into FilesToUpload (FileName, UploadType) values (?,?)";
		int fileId = -1;
		PreparedStatement ps1 = null; 
		try {
			ps1 = conn.prepareStatement(insertFileSql, Statement.RETURN_GENERATED_KEYS);
			ps1.setString(1, fileName);
			ps1.setString(2, "HIT");
			System.out.println(ps1);
			ps1.execute();
			ResultSet generatedKeys = ps1.getGeneratedKeys();
			if (generatedKeys.next()) {
				fileId = generatedKeys.getInt(1);
			}
		} finally {
			if (ps1 != null) {
				ps1.close();
			}
		}
		
		if (fileId <= 0) {
			throw new RuntimeException("Unable to insert data into FilesToUpload table, auto-generated key hasn't returned.");
		}
		
		String insertHitsSql = "insert into HitsToUpload (FileId, " + columns.replaceAll("\"", "") + ") " + 
				query.replaceAll("select 1 as v,", "select ?, 1 as v,");
		PreparedStatement ps2 = null; 
		try {
			ps2 = conn.prepareStatement(insertHitsSql);
			ps2.setInt(1, fileId);
			System.out.println(ps2);
			ps2.execute();
		} finally {
			if (ps2 != null) {
				ps2.close();
			}
		}
	}
	
	private List<HitObj> getHitList(DataOrigin dataOrigin) {
		List<HitObj> result = new ArrayList<HitObj>();
		List<HitObj> jsonQueries = getQueriesFromJson(dataOrigin);
		
		for (Hit hit : Hit.values()) {
			if (hit.getDataOrigin() == dataOrigin) {
				HitObj hitObj = new HitObj();
				hitObj.name = hit.name();
				hitObj.sql = hit.getQuery();
				
				for (HitObj h : jsonQueries) {
					if (h.name.equalsIgnoreCase(hit.name())) {
						hitObj.sql = h.sql;
						break;
					}
				}
				
				if (Util.isEmpty(hitObj.sql)) {
					logger.info("Skipping {} hit CSV file because SQL in external configuration is empty", hit.name());
				} else {
					result.add(hitObj);
				}
			}
		}
		
		for (HitObj h : jsonQueries) {
			boolean found = false;
			for (Hit hit : Hit.values()) {
				if (hit.name().equalsIgnoreCase(h.name)) {
					found = true;
					break;
				}
			}
			if (!found) {
				result.add(h);
				
			}
		}
		
		return result;
	}

	
	private class HitObj {
		private String name;
		private String sql;
	}
	
}


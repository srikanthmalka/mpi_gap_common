package au.com.michaelpage.gap.common.generator;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

public class DimensionsGenerator {
	
	private static final Logger logger = LoggerFactory.getLogger(DimensionsGenerator.class);
	
	private String outputFolderLocal;
	
	private String outputFolderGlobalRegional;
	
	private String fileTimestamp;
	
	public DimensionsGenerator(String outputFolderLocal, String outputFolderGlobalRegional, String fileTimestamp) {
		this.outputFolderLocal = outputFolderLocal;
		this.outputFolderGlobalRegional = outputFolderGlobalRegional;
		this.fileTimestamp = fileTimestamp; 
	}
	
	public boolean generate(DataOrigin dataOrigin) throws GeneratorException {
		boolean result = false;
		try {
			// Combine dimensions from Dimension enum and Json config into one list
			List<DimensionObj> dimensions = getDimensionList(dataOrigin);
			
			for (DimensionObj dimension : dimensions) {
				List<Map<String, Object>> customDataSources = null;
				if (dataOrigin == DataOrigin.BROADBEAN) {
					customDataSources = Settings.INSTANCE.getOptionArray("broadbean:customDataSources"); 
				} else if (dataOrigin == DataOrigin.RPM) {
					customDataSources = Settings.INSTANCE.getOptionArray("rpm:customDataSources");
				} else if (dataOrigin == DataOrigin.NEXTGEN) {
					customDataSources = Settings.INSTANCE.getOptionArray("nextgen:customDataSources");
				}
					
				if (customDataSources != null) {

					for (Map<String, Object> customDataSource : customDataSources) {
						if (customDataSource == null) {
							continue;
						}
							
						if (customDataSource.size() == 5) {
							String profileId = (String)customDataSource.get("profileId");
							String profileType = (String)customDataSource.get("profileType");
							String customDataSourceName = (String)customDataSource.get("customDataSourceName");
							String customDataSourceId = (String)customDataSource.get("customDataSourceId");
							Map<String, String> customDimensions =  (Map<String, String>)customDataSource.get("customDimensions");
							
							if (Util.isEmpty(profileId) || Util.isEmpty(customDataSourceName) || Util.isEmpty(customDataSourceId) ||
									customDimensions == null || customDimensions.size() < 2) {
								throw new RuntimeException("Incorrect format of custom data source JSON configuration for " + Settings.INSTANCE.getHostName());
							}
							
							String exportFileName = null;
							
							if ("local".equalsIgnoreCase(profileType)) {
								exportFileName = outputFolderLocal + customDataSourceName + "---" + profileId + "---" + customDataSourceId + ".csv";
							} else {
								exportFileName = outputFolderGlobalRegional + profileId + "\\" + customDataSourceName + "---" + customDataSourceId + "\\" + 
										Settings.INSTANCE.getHostName() + "-" + fileTimestamp + ".csv";
							}
							
							logger.info("Generating {} dimension for profile {}. CSV file: {}", customDataSourceName, profileId, exportFileName);
							
							String query = dimension.sql;
							if (dataOrigin == DataOrigin.BROADBEAN) {
								query = customizeQueryForBroadbean(query);
							} else if (dataOrigin == DataOrigin.NEXTGEN) {
								query = customizeQueryForNextgen(query, profileType);
							} else if (dataOrigin == DataOrigin.RPM) {
								query = customizeQueryForRpm(query);
							}
							
							generateFile(exportFileName, profileType, query, customDimensions);
							result = true;
						} else {
							throw new RuntimeException("Incorrect format of custom data source JSON configuration for " + Settings.INSTANCE.getHostName());
						}
					}
				} else {
					logger.info("Configuration doesn't contain dimension data");
				}
			}
		} catch (Throwable t) {
			logger.info("Unable to generate dimensions data. Message: " + t.getMessage());
			logger.debug(t.getMessage(), t);
			throw new GeneratorException("Unable to generate dimensions data. Message: " + t.getMessage(), t);
		}
		return result;
	}
	
	private String customizeQueryForBroadbean(String query) {
		String hostName = Settings.INSTANCE.getHostName();
		
		String brand = Util.getBrandFromHostName(hostName);
		String countryCode = Util.getCountryCodeFromHostName(hostName);
		
		String s = query.replace("${brand}", brand);
		s = s.replace("${country_code}", countryCode);
		
		if (!"UK".equals(countryCode)) { // UK doesn't seem to follow this naming convention and have 2 separate BB profiles for MP & PP
			if ("PP".equals(brand)) {
				s += " and lower(BB_office) like 'pp%'";
			} else {
				s += " and lower(BB_office) not like 'pp%'";
			}
		}
		
		return s;
	}

	private String customizeQueryForRpm(String query) {
		String hostName = Settings.INSTANCE.getHostName();
		
		String brand = Util.getBrandFromHostName(hostName);
		String countryCode = Util.getCountryCodeFromHostName(hostName);
		
		String s = query.replace("${brand}", brand);
		s = s.replace("${country_code}", countryCode);
		
		return s;
	}
	
	private String customizeQueryForNextgen(String query, String profileType) {
		String s = query;
		
		if ("regional".equalsIgnoreCase(profileType)) {
			s += " where job_location_country_code != 10";
		}
		
		return s;
	}
	
	private void generateFile(String exportFileName, String profileType, String query, Map<String, String> customDimensions) {
		Connection conn = null;
		PreparedStatement ps = null;
				
		try {
			String tempFilename = "c:\\temp\\" + new File(exportFileName).getName() + "_" + UUID.randomUUID() + ".csv";
			
			// Modify the query to include only required dimensions
			StringBuilder columnNames = new StringBuilder();
			StringBuilder dimensionNames = new StringBuilder();
			for (Map.Entry<String, String> dimension : customDimensions.entrySet()) {
				columnNames.append(dimension.getKey()).append(",");
				dimensionNames.append("\"").append(dimension.getValue()).append("\"").append(",");
			}

			String columnsNamesStr = columnNames.substring(0, columnNames.length() - 1);
			String dimensionNamesStr = dimensionNames.substring(0, dimensionNames.length() - 1);
			
			logger.debug("Columns: {}, dimensions: {}", columnsNamesStr, dimensionNamesStr);
			
			String updatedQuery = "select " + columnsNamesStr + " from (" + query + ") a";
			
			conn = DatabaseManager.INSTANCE.getConnection();
			ps = conn.prepareStatement("CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY (?,?,?,?,?)");
		    ps.setString(1, updatedQuery);
		    ps.setString(2, tempFilename);
		    ps.setString(3, ",");
		    ps.setString(4, "\"");
		    ps.setString(5, "UTF-8");
		    System.out.println(ps);
		    ps.execute();
			ps.close();
			
			new FilePrepend(tempFilename).prepend(dimensionNamesStr + "\r\n", exportFileName, "UTF-8");
			
			DeleteDirUtil.delete(new File(tempFilename));
			
			if ("local".equalsIgnoreCase(profileType)) {
				new GeneratorDao().populateFilesToUploadTables(exportFileName, "DIMENSION");
			}
		
		} catch (Throwable t) {
			throw new RuntimeException("Unable to generate dimensions data. Message: " + t.getMessage(), t);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(null, null, conn);
		}
		
	}
	
	
	private List<DimensionObj> getQueriesFromJson(DataOrigin dataOrigin) {
		List<Map<String, Object>> queries = Settings.INSTANCE.getOptionArray("queries");
		
		List<DimensionObj> result = new ArrayList<DimensionObj>();
		
		if (queries != null) {
			for (Map<String, Object> query : queries) {
				if (query == null) {
					continue;
				}
				
				String typeJson = (String)query.get("type");
				String nameJson = (String)query.get("name");
				String dataOriginJson = (String)query.get("dataOrigin");
				String sqlJson = (String)query.get("sql");
				
				if (dataOrigin.toString().equalsIgnoreCase(dataOriginJson) && "dimension".equalsIgnoreCase(typeJson)) {
					DimensionObj dimension = new DimensionObj();
					dimension.name = nameJson.toUpperCase();
					dimension.sql = sqlJson;
					result.add(dimension);
				}
			}
		}
		
		return result;
	}
	
	private List<DimensionObj> getDimensionList(DataOrigin dataOrigin) {
		List<DimensionObj> result = new ArrayList<DimensionObj>();
		List<DimensionObj> jsonQueries = getQueriesFromJson(dataOrigin);
		
		for (Dimension dimension : Dimension.values()) {
			if (dimension.getDataOrigin() == dataOrigin) {
				DimensionObj dimensionObj = new DimensionObj();
				dimensionObj.name = dimension.name();
				dimensionObj.sql = dimension.getQuery();
				
				for (DimensionObj d : jsonQueries) {
					if (d.name.equalsIgnoreCase(dimension.name())) {
						dimensionObj.sql = d.sql;
						break;
					}
				}
				
				if (Util.isEmpty(dimensionObj.sql)) {
					logger.info("Skipping {} hit CSV file because SQL in external configuration is empty", dimension.name());
				} else {
					result.add(dimensionObj);
				}
			}
		}
		
		for (DimensionObj h : jsonQueries) {
			boolean found = false;
			for (Dimension dimension : Dimension.values()) {
				if (dimension.name().equalsIgnoreCase(h.name)) {
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

	
	private class DimensionObj {
		private String name;
		private String sql;
	}
	
	
	
}

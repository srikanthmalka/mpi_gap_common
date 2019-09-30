package au.com.michaelpage.gap.common.util;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import au.com.michaelpage.gap.common.Settings;
import au.com.michaelpage.gap.common.model.GapProfile;
import au.com.michaelpage.gap.common.model.GapProfileType;
import au.com.michaelpage.gap.common.model.Host;
import au.com.michaelpage.gap.common.model.JobBoard;
import au.com.michaelpage.gap.common.model.RPM;

public enum DatabaseManager {
	INSTANCE;
	
	private String databaseLocation;
	
	public Connection getConnection() {
		Connection connection = null;
		
		try {
			connection = DriverManager.getConnection("jdbc:derby:" + databaseLocation + ";create=true", "", "");
		} catch (SQLException e) {
			throw new RuntimeException("Can't establish connection to the local DB " + databaseLocation + ". Message: " + e.getMessage(), e);
		}
		
		return connection;
	}
	
	public void closeConnection(ResultSet rs, PreparedStatement ps, Connection conn) {
		if (rs != null) {
			try {
				rs.close();
			} catch (SQLException e) {
			}
		}
		if (ps != null) {
			try {
				ps.close();
			} catch (SQLException e) {
			}
		}
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
			}
		}
	}
	
	public void shutdownDatabase(boolean delete, boolean cleanup) {
		try {
			DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
		} catch (Exception e) {
			throw new RuntimeException("Can't load database driver. Message: " + e.getMessage(), e);
		}

		try {
			if (cleanup) {
				cleanupDatabase();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to clean up database", e);
		} finally {
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true", "", "");
			} catch (SQLException e) {
				//Derby System Shutdown exception is part of the shutdown process
			}
		}
		
		if (delete) {
			Util.delete(databaseLocation);
		}
		
	}
	
	public void initDatabase(String databaseLocation) {
		initDatabase(databaseLocation, true);
	}
	
	public void initDatabase(String databaseLocation, boolean createGenerationTables) {
		this.databaseLocation = databaseLocation;

		try {
			DriverManager.registerDriver(new org.apache.derby.jdbc.EmbeddedDriver());
		} catch (Exception e) {
			throw new RuntimeException("Can't load database driver. Message: " + e.getMessage(), e);
		}
		
		try {
			if (databaseIsClean()) {
				createAdditionalTables();
			}
			
			if (!additionalTablesExist()) {
				if (createGenerationTables) {
					createGenerationTables();
					createDerbyFunctions();
				}
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Can't init DB. Message: " + e.getMessage(), e);
		}
		
	} 
	
	private void createDerbyFunctions() throws Exception {
		Connection conn = getConnection();
		
		PreparedStatement ps = conn.prepareStatement("CREATE FUNCTION MD5HASH(str VARCHAR(500)) RETURNS VARCHAR(500) PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA EXTERNAL NAME 'au.com.michaelpage.gap.common.util.derby.Md5Util.md5Hash'");
		System.out.println(ps);
		ps.execute();
		ps.close();
		
		
		ps = conn.prepareStatement("CREATE FUNCTION SUBSTRING_BEFORE(expressionToSearch VARCHAR(500), expressionToFind VARCHAR(500)) RETURNS varchar(500) PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA EXTERNAL NAME 'au.com.michaelpage.gap.common.util.derby.StringUtil.substringBefore'");
		System.out.println(ps);
		ps.execute();
		ps.close();

		ps = conn.prepareStatement("CREATE FUNCTION REPLACE(expressionToSearch VARCHAR(500), expressionToFind VARCHAR(500), expressionToReplace VARCHAR(500)) RETURNS varchar(500) PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA EXTERNAL NAME 'au.com.michaelpage.gap.common.util.derby.StringUtil.replace'");
		System.out.println(ps);
		ps.execute();
		ps.close();

		ps = conn.prepareStatement("CREATE FUNCTION STR_TO_DATE(date VARCHAR(500), format VARCHAR(500)) RETURNS timestamp PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA EXTERNAL NAME 'au.com.michaelpage.gap.common.util.derby.StringUtil.strToDate'");
		System.out.println(ps);
		ps.execute();
		ps.close();

		ps = conn.prepareStatement("CREATE FUNCTION ORDINAL_INDEXOF(expressionToSearch VARCHAR(500), expressionToFind VARCHAR(500), occurrence INT) RETURNS INT PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA EXTERNAL NAME 'au.com.michaelpage.gap.common.util.derby.StringUtil.ordinalIndexOf'");
		System.out.println(ps);
		ps.execute();
		ps.close();
		
		conn.close();
	}
	
	private boolean databaseIsClean() {
		try {
			return !tableExists("FILESTOUPLOAD");
		} catch (Exception e) {
			throw new RuntimeException("An error occured during database existance check. Message: " + e.getMessage(), e);
		}
	}

	private boolean additionalTablesExist() {
		try {
			return tableExists("GAPPROFILE");
		} catch (Exception e) {
			throw new RuntimeException("An error occured during database existance check. Message: " + e.getMessage(), e);
		}
	}
	
	private boolean tableExists(String tableName) {
		Connection connection = getConnection();
		try {
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			ResultSet rs = databaseMetaData.getTables(connection.getCatalog(), null, tableName, null);
			return rs.next();
		} catch (SQLException e) {
			throw new RuntimeException("An error occured during checking if table exists. Message: " + e.getMessage(), e);
		}
	}
	
	private void createAdditionalTables() throws Exception {
		Connection conn = getConnection();
		
		PreparedStatement ps = conn.prepareStatement(
				"create table FILESTOUPLOAD ("
				+ "FileId int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, "
				+ "FileName varchar(1000) not null, "
				+ "UploadType varchar (20) not null, "
				+ "IsUploaded boolean not null default false"
				+ ")"
				);
		System.out.println(ps);
		ps.execute();
		ps.close();
		
		ps = conn.prepareStatement(
				"create table HITSTOUPLOAD ("
				+ "Id int NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1) PRIMARY KEY, "
				+ "FileId int not null, "
				+ "v int not null, "
				+ "ni int, "
				+ "tid varchar(20) not null, "
				+ "cid varchar(100), "
				+ "uid varchar(100), "
				+ "cd2 varchar(100), "
				+ "dh varchar(30) not null, "
				+ "cs varchar(30) not null, "
				+ "cm varchar(30) not null, "
				+ "cn varchar(30) not null, "
				+ "cd10 varchar(30) not null, "
				+ "dp varchar(200) not null, "
				+ "t varchar(30) not null, "
				+ "cd50 varchar(100), "
				+ "cd51 varchar(100), "
				+ "cd52 varchar(100), "
				+ "cd53 varchar(100), "
				+ "cd54 varchar(100), "
				+ "ti varchar(100), "
				+ "tr varchar(30), "
				+ "pa varchar(30), "
				+ "IsUploaded boolean not null default false "
				+ ")"
				);
		System.out.println(ps);
		ps.execute();
		ps.close();
		
		conn.close();
	}
	
	private void createGenerationTables() throws Exception {	
		Connection conn = getConnection();
		PreparedStatement ps;
		
		for (Class clazz : new Class[] {GapProfile.class, RPM.class, Host.class, JobBoard.class}) {
			String sqlCreateTable = SQLGeneratorHelper.generateCreateTable(clazz);
			ps = conn.prepareStatement(sqlCreateTable);
			System.out.println(sqlCreateTable);
			ps.execute();
			ps.close();
		}

		ps = null;
		for (Host host : new Host[] {new Host(Settings.INSTANCE.getHostName(), Settings.INSTANCE.getCountryCode(), Settings.INSTANCE.getBrand())}) {
			if (ps == null) {
				ps = conn.prepareStatement(SQLGeneratorHelper.generateInsert(host.getClass()));
			}
			SQLUtil.populatePS(ps, host);
			System.out.println(ps);
			ps.executeUpdate();
		}
		
		ps = null;
		
		for (GapProfile profile : getGapProfileList()) {
			if (ps == null) {
				ps = conn.prepareStatement(SQLGeneratorHelper.generateInsert(profile.getClass()));
			}
			SQLUtil.populatePS(ps, profile);
			System.out.println(ps);
			ps.executeUpdate();
		}
		
		ps = null;
		for (RPM rpm : new RPM[] {new RPM(Settings.INSTANCE.getRpmId())}) {
			if (ps == null) {
				ps = conn.prepareStatement(SQLGeneratorHelper.generateInsert(rpm.getClass()));
			}
			SQLUtil.populatePS(ps, rpm);
			System.out.println(ps);
			ps.executeUpdate();
		}
		
		if (Settings.INSTANCE.getJobBoards() != null) {
			ps = null;
			for (JobBoard jobBoard : Settings.INSTANCE.getJobBoards()) {
				if (ps == null) {
					ps = conn.prepareStatement(SQLGeneratorHelper.generateInsert(jobBoard.getClass()));
				}
				SQLUtil.populatePS(ps, jobBoard);
				System.out.println(ps);
				ps.executeUpdate();
			}
		}
		conn.close();
	}
	
	private void cleanupDatabase() throws Exception {
		executeSql("drop table FILESTOUPLOAD", true);
		executeSql("drop table HITSTOUPLOAD", true);
		executeSql("drop table GAPPROFILE", true);
		executeSql("drop table HOST", true);
		executeSql("drop table RPM", true);
		executeSql("drop table RESP2", true);
		executeSql("drop table JOBBOARD", true);
		executeSql("drop function MD5HASH", true);	
		executeSql("drop function SUBSTRING_BEFORE", true);	
		executeSql("drop function REPLACE", true);	
		executeSql("drop function STR_TO_DATE", true);	
		executeSql("drop function ORDINAL_INDEXOF", true);	
		renameOrCreateTable("LIVEJOBS", "LIVEJOBS_PREV", 
					new String[] {
						"create table LIVEJOBS_PREV (BB_USER varchar(32672), BB_TEAM varchar(32672), BB_OFFICE varchar(32672), BB_REFERENCE varchar(32672))", 
						"create index IDX_LIVEJOBS_" + Util.getRandom(100000) + " on LIVEJOBS_PREV (BB_USER, BB_TEAM, BB_OFFICE, BB_REFERENCE)"
					});
		
		renameOrCreateTable("nextgen_extract", "nextgen_extract_PREV", 
				new String[] {
					"create table nextgen_extract_PREV (job_id varchar(20))", 
					"create index IDX_nextgen_extract_" + Util.getRandom(100000) + " on nextgen_extract_PREV (job_id)"
				});
		
	}
	
	public void restorePreviousDatabase() {
		renameOrCreateTable("LIVEJOBS_PREV", "LIVEJOBS", null);
		renameOrCreateTable("nextgen_extract_PREV", "nextgen_extract", null);
	}
	
	public void cleanupUploadData() {
		executeSql("truncate table HITSTOUPLOAD");
		executeSql("truncate table FILESTOUPLOAD");
	}
	
	public Date getExtractDate() {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Date result = null;
		String query = "select createtimestamp from event FETCH FIRST ROW ONLY";
		try {
			conn = getConnection();
			ps = conn.prepareStatement(query);
			System.out.println(query);
			rs = ps.executeQuery();
			if (rs.next()) {
				result = rs.getDate("createtimestamp");
			}
			
			return result;
			
		} catch (SQLException e) {
			throw new RuntimeException("Unable to get extract date from event table. Message: " + e.getMessage(), e);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(rs, ps, conn);
		}
		
	}
	
	private void executeSql(String sql) {
		executeSql(sql, false);
	}
	
	private void executeSql(String sql, boolean ignoreErrors) {
		Connection conn = null; 
		PreparedStatement ps = null;
		
		try {
			conn = getConnection();
			ps = conn.prepareStatement(sql);
			System.out.println(sql+"----------");
			ps.execute();
		} catch (Throwable t) {
			if (!ignoreErrors) {
				throw new RuntimeException(t);
			}
		} finally {
			closeConnection(null, ps, conn);
		}
	}
	
	private void renameOrCreateTable(String oldTableName, String newTableName, String[] newTableSql) {
		if (tableExists(newTableName)) {
			executeSql("drop table " + newTableName);
		}

		if (tableExists(oldTableName)) {
			executeSql("rename table " + oldTableName + " to " + newTableName);
		} else {
			if (newTableSql != null) {
				for (String s : newTableSql) {
					executeSql(s);
				}
			}
		}
	}
	
	
	private GapProfile[] getGapProfileList() {
		return new GapProfile[] {
				new GapProfile(Settings.INSTANCE.getRegionalProfileId(), GapProfileType.REGIONAL, 
						Settings.INSTANCE.getExchangeRate()), 
				new GapProfile(Settings.INSTANCE.getLocalProfileId(), GapProfileType.LOCAL, 1d)
			};
	}
	
}

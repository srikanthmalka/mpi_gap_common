package au.com.michaelpage.gap.common.generator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import au.com.michaelpage.gap.common.util.DatabaseManager;

public class GeneratorDao {

	public void populateFilesToUploadTables(String fileName, String fileType) throws Exception {
		Connection conn = null;
		PreparedStatement ps1 = null; 

		String insertFileSql = "insert into FilesToUpload (FileName, UploadType) values (?,?)";
		
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			ps1 = conn.prepareStatement(insertFileSql, Statement.RETURN_GENERATED_KEYS);
			ps1.setString(1, fileName);
			ps1.setString(2, fileType);
			System.out.println(ps1);
			ps1.execute();
		} finally {
			DatabaseManager.INSTANCE.closeConnection(null, ps1, conn);
		}
	}

}

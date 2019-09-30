package au.com.michaelpage.gap.common.google;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import au.com.michaelpage.gap.common.util.DatabaseManager;
import au.com.michaelpage.gap.common.util.Util;

public class GoogleUploaderDao {

	public Map<Integer, String> findOutstandingFiles() {
		return findOutstandingFiles(null);
	}
	
	public Map<Integer, String> findOutstandingFiles(String uploadType) {
		
		Map<Integer, String> map = new HashMap<Integer, String>();

		if (!checkUploadsTableExists()) {
			return map;
		}
		
		String query = "select FileId, FileName from FilesToUpload where IsUploaded = false"; 
		
		if (uploadType != null && (uploadType.equals("HIT") || uploadType.equals("DIMENSION"))) {
			query += " and UploadType = '" + uploadType + "'";
		}
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			while (rs.next()) {
				map.put(rs.getInt(1), rs.getString(2));
			}
			
			return map;
			
		} catch (SQLException e) {
			throw new RuntimeException("Unable to check FilesToUpload. Message: " + e.getMessage(), e);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(rs, ps, conn);
		}
	}

	public Map<Integer, String> findOutstandingHits(String fileName) {
		
		Integer fileId = findFileIdByFileName(fileName);
		
		Map<Integer, String> map = new HashMap<Integer, String>();
		
		String query = "select * from HitsToUpload where FileId = ? and IsUploaded = false"; 
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			ps = conn.prepareStatement(query);
			ps.setInt(1, fileId);
			rs = ps.executeQuery();
			while (rs.next()) {
				StringBuilder sb = new StringBuilder();
				sb.append("v=").append(rs.getInt("v"));
				
				if (rs.getInt("ni") == 1) {
					sb.append("&ni=").append(rs.getInt("ni"));
				}
				
				sb.append("&tid=").append(rs.getString("tid"));
				sb.append("&cid=").append(rs.getString("cid"));
				sb.append("&uid=").append(rs.getString("uid"));
				sb.append("&cd2=").append(rs.getString("cd2"));
				sb.append("&dh=").append(rs.getString("dh"));
				sb.append("&cs=").append(rs.getString("cs"));
				sb.append("&cm=").append(rs.getString("cm"));
				sb.append("&cn=").append(rs.getString("cn"));
				sb.append("&cd10=").append(rs.getString("cd10"));
				sb.append("&dp=").append(rs.getString("dp"));
				sb.append("&t=").append(rs.getString("t"));
				sb.append("&cd50=").append(rs.getString("cd50"));
				sb.append("&cd51=").append(rs.getString("cd51"));
				sb.append("&cd52=").append(rs.getString("cd52"));
				sb.append("&cd53=").append(rs.getString("cd53"));
				sb.append("&cd54=").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				
				if (!Util.isEmpty(rs.getString("ti"))) {
					sb.append("&ti=").append(rs.getString("ti"));
					sb.append("&tr=").append(rs.getString("tr"));
					sb.append("&pa=").append(rs.getString("pa"));
				}
				
				map.put(rs.getInt("id"), sb.toString());
			}
			
			return map;
			
		} catch (SQLException e) {
			throw new RuntimeException("Unable to check FilesToUpload. Message: " + e.getMessage(), e);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(rs, ps, conn);
		}
		
	}
	
	public void updateHitUploadStatus(Integer id, boolean isUploaded) {
		String query = "update HitsToUpload set IsUploaded = ? where Id = ?"; 
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			ps = conn.prepareStatement(query);
			ps.setBoolean(1, isUploaded);
			ps.setInt(2, id);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to update hit upload status. Message: " + e.getMessage(), e);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(null, ps, conn);
		}
	}

	public void updateFileUploadStatus(String fileName, boolean isUploaded) {
		String query = "update FilesToUpload set IsUploaded = ? where FileName = ?"; 
		
		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			ps = conn.prepareStatement(query);
			ps.setBoolean(1, isUploaded);
			ps.setString(2, fileName);
			ps.executeUpdate();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to update file upload status. Message: " + e.getMessage(), e);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(null, ps, conn);
		}
	}
	
	private Integer findFileIdByFileName(String fileName) {
		String query = "select FileId from FilesToUpload where fileName = ?"; 
		
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			ps = conn.prepareStatement(query);
			ps.setString(1, fileName);
			rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("FileId");
			} else {
				throw new RuntimeException("Can't find file to upload with name " + fileName);
			}
		} catch (SQLException e) {
			throw new RuntimeException("Unable to check FilesToUpload. Message: " + e.getMessage(), e);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(null, ps, conn);
		}
		
	}
	
	private boolean checkUploadsTableExists() {
		
		Connection conn = null;
		ResultSet rs = null;
		try {
			conn = DatabaseManager.INSTANCE.getConnection();
			DatabaseMetaData dbmd = conn.getMetaData();
			rs = dbmd.getTables(null, "APP", "FILESTOUPLOAD", null);
			return rs.next();
		} catch (SQLException e) {
			throw new RuntimeException("Unable to check whether FilesToUpload table exists. Message: " + e.getMessage(), e);
		} finally {
			DatabaseManager.INSTANCE.closeConnection(rs, null, conn);
		}
		
	}
	
}

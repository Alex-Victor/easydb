package walker.easydb.assistant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import walker.easydb.connection.ConnectionPool;

/**
 * ʵ������븨�����ɹ���
 * 
 * @author HuQingmiao
 * 
 */
public class EntityGenerator {

	public static void main(String[] args) {
		try {
			EntityGenerator.createEntityFile("COMPANY", new File("d:/"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ȡ��ĳ������е�Ԫ����
	 * 
	 * @param tableName
	 * @return
	 * @throws Exception
	 */
	private static TreeMap<String, MyMetaData> getColumnMap(String tableName) throws Exception {

		ConnectionPool connPool = ConnectionPool.getInstance();
		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		// �����������͵�����ֵ
		TreeMap<String, MyMetaData> map = new TreeMap<String, MyMetaData>();
		try {
			String sql = "SELECT * FROM " + tableName + " WHERE 1=2";

			conn = connPool.getConnection();
			stmt = conn.prepareStatement(sql);
			rs = stmt.executeQuery();

			ResultSetMetaData rsmd = rs.getMetaData();

			for (int i = 1; i <= rsmd.getColumnCount(); i++) {
				String colName = rsmd.getColumnName(i).toUpperCase();
				int colType = rsmd.getColumnType(i);
				String colTypeName = rsmd.getColumnTypeName(i);

				System.out.println(colName + " :" + colType + ": " + rsmd.getColumnTypeName(i));

				MyMetaData md = new MyMetaData();
				md.setColName(colName);

				md.setColTypeName(rsmd.getColumnTypeName(i));

				if ("BLOB".equalsIgnoreCase(colTypeName) || "CLOB".equalsIgnoreCase(colTypeName)) {
					md.setPrecision(0);
				} else {
					md.setPrecision(rsmd.getPrecision(i));
				}

				md.setScale(rsmd.getScale(i));

				map.put(colName, md);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			connPool.release(conn, stmt, rs);
		}

		return map;

	}

	/**
	 * ����ʵ�����ļ����ַ�������
	 * 
	 * @param tableName
	 * @param colMap
	 * @return
	 */
	private static StringBuffer buildContent(String tableName, Map<String, MyMetaData> colMap) {
		StringBuffer buff = new StringBuffer();

		// public class AA {
		buff.append("@SuppressWarnings(\"serial\")\n");
		buff.append("public class ");
		buff.append(MappingUtil.getEntityName(tableName));
		buff.append(" extends BaseEntity{\n");

		// private EString xxx;
		for (Iterator<String> it = colMap.keySet().iterator(); it.hasNext();) {
			String colName = (String) it.next();
			MyMetaData metaData = (MyMetaData) colMap.get(colName);

			// int type = metaData.getColType();
			String typeName = metaData.getColTypeName().toUpperCase();

			if (typeName.equals("VARCHAR2") || typeName.equals("VARCHAR") || typeName.equals("CHAR")) {
				buff.append("private EString ");

			} else if (typeName.equals("DOUBLE") || typeName.equals("DECIMAL")) {
				buff.append("private EDouble ");

			} else if (typeName.equals("FLOAT")) {
				buff.append("private EFloat ");

			} else if (typeName.indexOf("BIGINT") >= 0) {
				buff.append("private ELong ");

			} else if (typeName.indexOf("INT") >= 0) {
				buff.append("private EInteger ");

			} else if (typeName.equals("DATE") || typeName.equals("DATETIME") || typeName.equals("TIME")) {
				buff.append("private ETimestamp ");

			} else if (typeName.equals("LONGVARCHAR") || typeName.equals("CLOB")) {
				buff.append("private ETxtFile ");

			} else if (typeName.equals("LONGBLOB") || typeName.equals("BLOB")) {
				buff.append("private EBinFile ");

			} else if (typeName.equals("NUMBER")) {
				if (metaData.getScale() > 0) {// ��С��λ��, ��ض��Ǹ������ֶ�
					buff.append("private EDouble ");

				} else if (metaData.getPrecision() > 9) {// ���>9λ, �����ELong
					buff.append("private ELong ");
				} else {
					buff.append("private EInteger ");
				}
			}
			buff.append(MappingUtil.getFieldName(colName) + ";\n");
		}

		// default constructor method
		buff.append("\npublic String[] pk() {return new String[]{};}\n");

		buff.append("}\n");

		return buff;
	}

	/**
	 * ���ı�����д��ָ�����ļ�
	 * 
	 * @param fileContent
	 * @param fileName
	 */
	private static void createFile(String fileContent, String fileName) throws IOException {
		OutputStreamWriter osw = null;
		try {
			osw = new OutputStreamWriter(new FileOutputStream(fileName));
			osw.write(fileContent, 0, fileContent.length());
			osw.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (osw != null) {
				osw.close();
			}
		}
	}

	/**
	 * Ϊָ�������ݿ����������Ӧ��ʵ�����ļ�.
	 * 
	 * @param tableName
	 *            ���ݿ��еı���
	 * @param dirc
	 *            ʵ�����ļ��Ĵ��Ŀ¼
	 */
	public static void createEntityFile(String tableName, File dirc) {
		try {

			TreeMap<String, MyMetaData> colMap = getColumnMap(tableName);
			StringBuffer buff = buildContent(tableName, colMap);

			dirc.mkdirs();

			String filename = dirc.getCanonicalPath() + File.separator + MappingUtil.getEntityName(tableName) + ".java";
			createFile(buff.toString(), filename);

			buff.delete(0, buff.length());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ��Ԫ����
	private static class MyMetaData {
		private String colName;

		// private int colType;

		private String colTypeName;

		private int precision;// ��Ч����

		private int scale; // С��λ����

		@SuppressWarnings("unused")
		public String getColName() {
			return colName;
		}

		public void setColName(String colName) {
			this.colName = colName;
		}

		public int getPrecision() {
			return precision;
		}

		public void setPrecision(int precision) {
			this.precision = precision;
		}

		public int getScale() {
			return scale;
		}

		public void setScale(int scale) {
			this.scale = scale;
		}

		public String getColTypeName() {
			return colTypeName;
		}

		public void setColTypeName(String colTypeName) {
			this.colTypeName = colTypeName;
		}

	}
}

package edu.uiowa.loaders;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class JDBCReader extends Reader {
	static boolean debug = true;
	Connection conn = null;
	ResultSet rs = null;
	boolean first = true;
	boolean last = false;
	String header = null;
	String footer = null;
	
	public JDBCReader(String className, String jdbcURL, String userID, String password) throws IOException, ClassNotFoundException, SQLException {
		Class.forName(className); 
		conn =  DriverManager.getConnection(jdbcURL, userID, password);
		rs = queryDatabase();
	}
	
	abstract ResultSet queryDatabase();
	
	public int read(char[] theChars, int offset, int length) throws IOException {
		if (debug) System.out.println("read called: offset = " + offset + ", length = " + length);
		try {
			if (rs.next()) {
				String buffer = rs.getString(1);
				if (debug) System.out.println(buffer);
				theChars = buffer.toCharArray();
				return buffer.length();
			} else {
				return -1;
			}
		} catch (SQLException e) {
			throw new IOException(e.toString());
		}
	}
	
	public void close() throws IOException {
		try {
			rs.close();
			conn.close();
		} catch (SQLException e) {
			throw new IOException(e.toString());
		}
	}
}

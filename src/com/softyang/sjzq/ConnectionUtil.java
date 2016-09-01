package com.softyang.sjzq;


import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionUtil {

	public static Connection getConnection() {
		String driverName = "com.mysql.jdbc.Driver"; // 加载JDBC驱动
		String dbURL = "jdbc:mysql://localhost:3306/test";
		//String dbURL = "jdbc:jtds:sqlserver://192.168.18.108:1433; DatabaseName=FXMrfDB"; // 连接服务器和数据库sample

		String userName = "root"; // 默认用户名

		String userPwd = "root"; // 密码

		try {

			Class.forName(driverName);

			Connection conn = DriverManager.getConnection(dbURL, userName, userPwd);

			return conn;

		} catch (Exception e) {

			e.printStackTrace();

		}
		return null;
	}
}

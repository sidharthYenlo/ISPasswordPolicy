package com.yenlo.identity;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Sidharth on 6/4/16. take user name and password as parameter in
 * connection manager
 */
public class DatabaseConnectionManager {
	private static Connection connection = null;
	private static DatabaseConnectionManager connectionManager = new DatabaseConnectionManager();
	private static final String dbReconnect = "autoReconnect=true&useSSL=false";

	private DatabaseConnectionManager() {
	}

	public static DatabaseConnectionManager getInstance() {
		return connectionManager;
	}

	public Connection connectToDatabase(DataBaseDetails dataBaseDetails) {

		if (connection != null) {
			return connection;
		} else {
			try {

				Class.forName("com.mysql.jdbc.Driver").newInstance();
				String connectionURL = dataBaseDetails
						.getDatabaseDriverName()
						+ "//"
						+ dataBaseDetails.getIpAddress()
						+ ":"
						+ dataBaseDetails.getPortNumber()
						+ "/"
						+ dataBaseDetails.getDataBaseName()
						+ "?"
						+ dbReconnect;
				connection = DriverManager.getConnection(connectionURL,
						dataBaseDetails.getUserName(),
						dataBaseDetails.getPassword());
			} catch (ClassNotFoundException e) {
				System.out.println("MySQL JDBC Driver Missing");
				e.printStackTrace();
			} catch (SQLException e) {
				System.out.println("Connection to database Failed!");
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (connection != null) {
			System.out.println("Connected to DataBase");
		} else {
			System.out.println("Failed to make connection!");
		}

		return connection;
	}

	public boolean disconnectToDB() {

		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Failed to close connection!");
		}
		return false;
	}
}

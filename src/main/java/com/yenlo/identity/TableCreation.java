package com.yenlo.identity;

import java.sql.*;

public class TableCreation {
	// New Table "UserPasswordRecoveryTBL" will be created
	// if table exist with another name that table will be deleted and this new
	// one will be created;
	//
	static Connection connection = null;
	static String space = "  ";
	static Statement statement = null;

	public TableCreation(Connection databaseConnection) {
		this.connection = databaseConnection;
	}

	public boolean createTable(String tableName, String sqlScript) {
		try {

			if (!ifTableExist(connection, tableName)) {
				statement = connection.createStatement();
				executeQuery(sqlScript);
				return true;
			}
		} catch (SQLException e) {
			System.out.println("SqlException");
			e.printStackTrace();
		}

		return false;
	}


	public boolean createTable(TableInfo [] tableInfo) {

		try {
			for (int i=0;i<tableInfo.length;i++) {

				if (!ifTableExist(connection, tableInfo[i].getTableName())) {
					statement = connection.createStatement();
					String sqlScript = "";
					if (tableInfo[i].getPasswordHistoryCollumn() == 0) {
						sqlScript = buildPasswordCounterFixedValueTableQuery(tableInfo[i]);
						executeQuery(sqlScript);
						insertDataPasswordCounterFixedValueTable(tableInfo[0],tableInfo[1]);
						addTabledependency(tableInfo[0].getTableName(), "latestPasswordCounter",
								tableInfo[1].getTableName(), "PasswordCounter");
					} else {
						sqlScript = buildPasswordHistoryTableQuery(tableInfo[i]);
						executeQuery(sqlScript);
					}


				}
			}
			return true;
		} catch (SQLException e) {
			System.out.println("SqlException");
			e.printStackTrace();
		}

		return false;
	}

	public boolean ifTableExist(Connection connection, String tableName) {

		DatabaseMetaData dbm = null;
		try {
			dbm = connection.getMetaData();
			ResultSet tables = dbm.getTables(null, null, tableName, null);
			if (tables.next()) {
				// Table exists
				return true;
			} else {
				// Table does not exist
				return false;
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}

	public String buildPasswordHistoryTableQuery(TableInfo tableInfo) {
		String query = "CREATE TABLE " + tableInfo.getTableName() + " ("
				+ "UserID varchar(60) NOT NULL,";
		for (int i = 1; i <= tableInfo.getPasswordHistoryCollumn(); i++) {
			query += "Password" + String.valueOf(i);
			query += space;
			query += "varchar(1000) DEFAULT NULL,";
		}
		query += "LastUpdateDate TIMESTAMP DEFAULT now() ON UPDATE now(),";
		query += "LatestPasswordCounter  TINYINT UNSIGNED NOT NULL,";
		query += "  PRIMARY KEY (UserID)) ;";
		return query;
	}

	public String buildPasswordCounterFixedValueTableQuery(TableInfo tableInfo) {

		String query = "CREATE TABLE "+ tableInfo.getTableName()+" (PasswordCounter TINYINT UNSIGNED NOT NULL,PRIMARY KEY (PasswordCounter)) ;";
		return query;
	}

	public void insertDataPasswordCounterFixedValueTable(TableInfo primaryTable, TableInfo counterTable) {

		String query = "INSERT INTO " + counterTable.getTableName()+"( PasswordCounter ) VALUES ";

		for (int i = 1; i <= primaryTable.getPasswordHistoryCollumn(); i++) {
			query += "(" + String.valueOf(i) + "),";
		}

		query = query.substring(0, query.length() - 1);
		query += ";";
		executeQuery(query);
	}

	public void addTabledependency(String table1, String table1Column,
			String table2, String table2Column) {
		String query = "ALTER TABLE " + table1 + " ADD FOREIGN KEY ("
				+ table1Column + ") REFERENCES  " + table2 + "(" + table2Column
				+ ");";
		executeQuery(query);
	}

	public void executeQuery(String query) {
		if (connection != null) {
			try {
				statement = connection.createStatement();
				statement.executeUpdate(query);
				statement.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}

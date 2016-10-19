package com.yenlo.identity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.carbon.user.core.UserStoreException;

import java.io.FileReader;
import java.sql.Connection;


public class PasswordHistoryVerfication {

    static long historyCount;
    static long passwordChangeMinTime;
    static String tablename1;
    static String tablename2;
    static UserPasswordManagement userPasswordManagement;
    static TableInfo primaryTable;
    static TableInfo counterTable;
    private static Log log = LogFactory.getLog(PasswordHistoryVerfication.class);

    PasswordHistoryVerfication(Connection connection) {
        //DatabaseConnectionManager dataBaseConnectionManager = new DatabaseConnectionManager();
        readDataFromJson();
        createTableIfNotExist(connection);
        userPasswordManagement = new UserPasswordManagement(connection, primaryTable);

    }

    public boolean updatePassword(Connection connection, String userID, String password) throws UserStoreException {
        log.info("inside update passsword method");
        if (null == userPasswordManagement) {
            userPasswordManagement = new UserPasswordManagement(connection, primaryTable);
        }
        return userPasswordManagement.updateUserPassword(userID, password);

    }

    public boolean addnewUser(Connection connection, String userID, String password) throws UserStoreException {
        log.info("inside adding user method");
        if (null == userPasswordManagement) {
            userPasswordManagement = new UserPasswordManagement(connection, primaryTable);
        }
        return userPasswordManagement.addNewUser(userID, password);

    }

    public static void main(String args[]) {


////      "jdbc:mysql://localhost:3306/"+databaseName,"root", "india@1947"
//        String dataBaseClassName = "com.mysql.jdbc.Driver";//"com.mysql.jdbc.Driver"
//        String databaseDriverName = "jdbc:mysql:";//"jdbc:mysql:"
//        String ipAddress = "localhost";
//        String portNumber = "3306";
//        String DataBaseName = "firstSampleDB";
//        String userName = "root";
//        String password = "password";
//        DataBaseDetails dataBaseDetails = new DataBaseDetails.Builder()
//                .dataBaseClassName(dataBaseClassName)
//                .databaseDriverName(databaseDriverName)
//                .dataBaseName(DataBaseName)
//                .ipAddress(ipAddress)
//                .portNumber(portNumber)
//                .userName(userName)
//                .password(password).build();


//        DatabaseConnectionManager dataBaseConnectionManager = DatabaseConnectionManager.getInstance();
//        Connection connection = dataBaseConnectionManager.connectToDatabase(dataBaseDetails);
//        readDataFromJson();
//        createTableIfNotExist(connection);

//        UserPasswordManagement passwordManagement = new UserPasswordManagement(connection, primaryTable);
//        try {
//            passwordManagement.addNewUser("1", "1272");
//        } catch (UserStoreException e) {
//            e.printStackTrace();
//        }

        try {
            passwordManagement.updateUserPassword("1", "6272");
        } catch (UserStoreException e) {
            e.printStackTrace();
        }

//        try {
//            passwordManagement.addNewUser("2", "8272");
//        } catch (UserStoreException e) {
//            e.printStackTrace();
//        }


    }

    private static void createTableIfNotExist(Connection connection) {

        primaryTable = new TableInfo.Builder()
                .tableName(tablename1)
                .passwordHistoryCollumn((int) historyCount).build();

        counterTable = new TableInfo.Builder()
                .tableName(tablename2)
                .passwordHistoryCollumn(0).build();


        TableCreation tableCreation = new TableCreation(connection);
        TableInfo tableInfoArray[] = {primaryTable, counterTable};
        tableCreation.createTable(tableInfoArray);

    }

    private static void readDataFromJson() {
        try {

            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new FileReader("/Users/Sidharth/Desktop/ISPasswordPloicy_Backup/userPasswordHistory.json"));
            JSONObject jsonObject = (JSONObject) obj;
            historyCount = (Long) jsonObject.get("passwordHistoryCount");
            passwordChangeMinTime = (Long) jsonObject.get("passwordChangeMinTime");
            tablename1 = (String) jsonObject.get("tablename1");
            tablename2 = (String) jsonObject.get("tablename2");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

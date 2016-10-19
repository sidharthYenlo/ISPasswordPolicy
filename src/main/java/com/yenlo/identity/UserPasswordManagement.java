package com.yenlo.identity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserPasswordManagement {
    private Connection databaseConnection;
    private PreparedStatement selectUserPasswordPS;
    private PreparedStatement selectLastPasswordCounterPS;
    private PreparedStatement updatePasswordPS;
    private PreparedStatement updateLatestPasswordCounterPS;
    private PreparedStatement selectTimeStampPS;
    private PreparedStatement insertUserPS;
    private PreparedStatement checkifUserExistPS;

    private TableInfo tableInfo;
    private String insertUserQuery = "";
    private String checkUserExixt = "";
    private String selectPasswordQuery = "";
    private String passwordUpdateQuery = "";
    private String passwordCounterUpdateQuery = "";
    private String counterValueQuery = "";
    private String timeStampSelectQuery = "";
    private String checkIfUserExistQuery = "";
    private String space = "  ";
    private StringBuilder queryBuilder = new StringBuilder();
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat javaDateFormat = new SimpleDateFormat(
            "EEE MMM dd HH:mm:ss z yyyy");
    private static Log log = LogFactory.getLog(PasswordHistoryVerfication.class);
    UserPasswordManagement(Connection dbConnection, TableInfo tableInfo) {
        databaseConnection = dbConnection;
        this.tableInfo = tableInfo;
    }

    // adds new user if dosent exist
    public boolean addNewUser(String userID, String password) throws UserStoreException {
        if (ifUserExist(userID)) {
            throw new UserStoreException("The user already exist");
        } else {
            try {
                if (databaseConnection != null) {

                    insertUserQuery = createUserInsertQuery();
                    insertUserPS = databaseConnection
                            .prepareStatement(insertUserQuery);
                    insertUserPS.setString(1, userID);
                    insertUserPS.setString(2, password);
                    insertUserPS.setInt(3, 1);
                    insertUserPS.executeUpdate();
                    insertUserPS.close();
                    databaseConnection.commit();
                    closeConnections(insertUserPS);

                    return true;
                }
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NumberFormatException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean ifUserExist(String userID) {

        queryBuilder.setLength(0);
        try {
            if (tableInfo != null) {
                queryBuilder.append("SELECT userID FROM");
                queryBuilder.append(space);
                queryBuilder.append(tableInfo.getTableName());
                queryBuilder.append(space);
                queryBuilder.append("WHERE UserID =?");
            }
            checkIfUserExistQuery = queryBuilder.toString();
            checkifUserExistPS = databaseConnection
                    .prepareStatement(checkIfUserExistQuery);
            checkifUserExistPS.setString(1, userID);
            ResultSet checkifUserExistRS = checkifUserExistPS.executeQuery();
            if (checkifUserExistRS.next()) {
                if (checkifUserExistRS.getInt(1) > 0) {
                    // ("user Exist");
                    return true;
                } else {
                    return false;
                }
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            closeConnections(checkifUserExistPS);
        }

        return false;
    }

    public boolean updateUserPassword(String userID, String password) throws UserStoreException {

        try {
            if (databaseConnection != null) {
                setPreDefinedValues();

                List<String> passwordList = getPasswordAsList(userID);
                boolean differnceTimeGreater = isPasswordUpdateBeforeTimeLimit(userID);
                log.info("the time limit is passed ------------------------"+differnceTimeGreater);
                if (!differnceTimeGreater) {
                    throw new UserStoreException("You have recently changed your password. Try after some time.");
                    //throw exception
                }
                if (passwordMatchsHistoryPassword(password, passwordList)) {
                    // throw exception
                    throw new UserStoreException(" Your Password matches to last " + tableInfo.getPasswordHistoryCollumn() + " password");
                } else {
                    // update password
                    int currentPasswordCounter = getLastPasswordCounterValue(userID);
                    log.info("the password counter is ------------------------"+currentPasswordCounter);
                    int nextPasswordCounter = calculateNextPasswordUpdateFeild(currentPasswordCounter);
                    log.info("the next password counter is ------------------------"+nextPasswordCounter);
                    passwordUpdateQuery = createPasswordUpdateQuery(nextPasswordCounter);
                    updatePasswordPS = databaseConnection
                            .prepareStatement(passwordUpdateQuery);
                    updatePasswordPS.setString(1, password);
                    updatePasswordPS.setString(2, userID);
                    updatePasswordPS.executeUpdate();
                    databaseConnection.commit();
                    updatePasswordPS.close();
                    // update counter value
                    passwordCounterUpdateQuery = createPasswordCounterUpdateQuery();
                    updatePasswordCounterValue(passwordCounterUpdateQuery,
                            nextPasswordCounter, userID);
                    databaseConnection.commit();
                    return true;

                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } finally {
            closeConnections(updatePasswordPS);
        }
        return false;
    }

    private boolean passwordMatchsHistoryPassword(String Password,
                                                  List<String> passwordList) {
        for (int i = 0; i < passwordList.size(); i++) {
            if (passwordList.get(i) != null
                    && passwordList.get(i).equalsIgnoreCase(Password)) {
                return true;
            }
        }

        return false;
    }

    private void setPreDefinedValues() {
        // query to select password from user table;
        if (selectPasswordQuery == "") {
            selectPasswordQuery = createSelectPasswordQuery();
        }

        // query to get counter value
        if (counterValueQuery == "") {
            counterValueQuery = createGetCounterValueQuery();
        }
        if (timeStampSelectQuery == "") {
            timeStampSelectQuery = createTimeStampSelectQuery();
        }
        if (insertUserQuery == "") {
            insertUserQuery = createUserInsertQuery();
        }

        if (selectUserPasswordPS == null || selectLastPasswordCounterPS == null
                || selectTimeStampPS == null) {
            try {
                selectUserPasswordPS = databaseConnection
                        .prepareStatement(selectPasswordQuery);
                selectLastPasswordCounterPS = databaseConnection
                        .prepareStatement(counterValueQuery);
                selectTimeStampPS = databaseConnection
                        .prepareStatement(timeStampSelectQuery);

            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

    private int calculateNextPasswordUpdateFeild(int currentPasswordCounter) {
        int nextPasswordCounter = 0;
        if (tableInfo != null) {
            int numberOfPassword = tableInfo.getPasswordHistoryCollumn();
            if (currentPasswordCounter + 1 <= numberOfPassword) {
                nextPasswordCounter = currentPasswordCounter + 1;
            } else {
                nextPasswordCounter = 1;
            }
        }
        return nextPasswordCounter;
    }

    private boolean isPasswordUpdateBeforeTimeLimit(String userID) {
        float differenceTime = 0;
        boolean isBeforeTime=false;
        int hour=3600000;

        String lastUpdateDate = getPasswordLastUpdateDate(userID);
        try {
            if (lastUpdateDate != "") {
                Date dbDate = dbDateFormat.parse(lastUpdateDate);
                Date currentDateJava = javaDateFormat.parse(new Date().toString());
                float differneceInHour = (currentDateJava.getTime() - dbDate
                        .getTime())/hour;

                    differenceTime = (differneceInHour) - (PasswordHistoryVerfication.passwordChangeMinTime);
                if(differenceTime>0) {
                    isBeforeTime=true;
                    return isBeforeTime;
                }
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return isBeforeTime;
    }

    private String createSelectPasswordQuery() {
        queryBuilder.setLength(0);
        if (tableInfo != null) {
            queryBuilder.append("SELECT ");
            int passwordCount = tableInfo.getPasswordHistoryCollumn();
            for (int i = 1; i < passwordCount; i++) {
                queryBuilder.append("Password");
                queryBuilder.append(i);
                queryBuilder.append(",");

            }
            queryBuilder.append("Password");
            queryBuilder.append(passwordCount);
            queryBuilder.append(space);
            queryBuilder.append("FROM");
            queryBuilder.append(space);
            queryBuilder.append(tableInfo.getTableName());
            queryBuilder.append(space);
            queryBuilder.append("WHERE UserID =?");

        }

        return queryBuilder.toString();
    }

    private String createTimeStampSelectQuery() {
        queryBuilder.setLength(0);
        if (tableInfo != null) {
            queryBuilder.append("SELECT");
            queryBuilder.append(space);
            queryBuilder.append("LastUpdateDate");
            queryBuilder.append(space);
            queryBuilder.append("FROM");
            queryBuilder.append(space);
            queryBuilder.append(tableInfo.getTableName());
            queryBuilder.append(space);
            queryBuilder.append("WHERE UserID =?");
        }
        return queryBuilder.toString();

    }

    private String createPasswordUpdateQuery(int passwordCounter) {
        queryBuilder.setLength(0);
        if (tableInfo != null) {
            queryBuilder.append("UPDATE");
            queryBuilder.append(space);
            queryBuilder.append(tableInfo.getTableName());
            queryBuilder.append(space);
            queryBuilder.append("SET");
            queryBuilder.append(space);
            queryBuilder.append("Password" + passwordCounter + "=?");
            queryBuilder.append(space);
            queryBuilder.append("WHERE UserID =?");
        }

        return queryBuilder.toString();
    }

    private String createPasswordCounterUpdateQuery() {
        queryBuilder.setLength(0);
        if (tableInfo != null) {
            queryBuilder.append("UPDATE");
            queryBuilder.append(space);
            queryBuilder.append(tableInfo.getTableName());
            queryBuilder.append(space);
            queryBuilder.append("SET LatestPasswordCounter =?");
            queryBuilder.append(space);
            queryBuilder.append("WHERE UserID =?");
        }

        return queryBuilder.toString();
    }

    private String createGetCounterValueQuery() {
        queryBuilder.setLength(0);
        if (tableInfo != null) {
            queryBuilder.append("SELECT");
            queryBuilder.append(space);
            queryBuilder.append("LatestPasswordCounter");
            queryBuilder.append(space);
            queryBuilder.append("FROM");
            queryBuilder.append(space);
            queryBuilder.append(tableInfo.getTableName());
            queryBuilder.append(space);
            queryBuilder.append("WHERE UserID =?");

        }

        return queryBuilder.toString();
    }

    private List<String> getPasswordAsList(String userID) {

        List<String> passwordList = new ArrayList<String>();
        try {
            selectUserPasswordPS.setString(1, userID);
            ResultSet selectPasswordRS = selectUserPasswordPS.executeQuery();
            int passwordCounter = 1;
            // do not touch this logic ; this gives all the passwords as a list

            while (selectPasswordRS.next()) {
                while (passwordCounter <= tableInfo.getPasswordHistoryCollumn()) {
                    passwordList.add(selectPasswordRS.getString("Password"
                            + String.valueOf(passwordCounter)));
                    passwordCounter++;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return passwordList;

    }

    private int getLastPasswordCounterValue(String userID) {
        int counterValue = 0;
        try {
            selectLastPasswordCounterPS.setString(1, userID);
            ResultSet selectLastPasswordCounterRS = selectLastPasswordCounterPS
                    .executeQuery();
            while (selectLastPasswordCounterRS.next()) {

                counterValue = selectLastPasswordCounterRS
                        .getInt("LatestPasswordCounter");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(selectLastPasswordCounterPS);
        }
        return counterValue;
    }

    private void updatePasswordCounterValue(String passwordCounterUpdateQuery,
                                            int latestPasswordCounter, String userID) {

        try {
            updateLatestPasswordCounterPS = databaseConnection
                    .prepareStatement(passwordCounterUpdateQuery);
            updateLatestPasswordCounterPS.setInt(1, latestPasswordCounter);
            updateLatestPasswordCounterPS.setString(2, userID);
            updateLatestPasswordCounterPS.executeUpdate();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            closeConnections(updateLatestPasswordCounterPS);
        }
    }

    private String getPasswordLastUpdateDate(String userID) {
        String lastUpdateDate = "";
        try {
            selectTimeStampPS.setString(1, userID);
            ResultSet selectLastPasswordCounterRS = selectTimeStampPS
                    .executeQuery();
            while (selectLastPasswordCounterRS.next()) {

                lastUpdateDate = selectLastPasswordCounterRS
                        .getString("LastUpdateDate");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeConnections(selectTimeStampPS);
        }
        return lastUpdateDate;
    }

    private String createUserInsertQuery() {
        queryBuilder.setLength(0);
        if (tableInfo != null) {
            queryBuilder.append("INSERT INTO");
            queryBuilder.append(space);
            queryBuilder.append(tableInfo.getTableName());
            queryBuilder.append(space);
            queryBuilder.append("(UserID,Password1,LatestPasswordCounter)");
            queryBuilder.append(space);
            queryBuilder.append("VALUES");
            queryBuilder.append("(?,?,?)");
        }

        return queryBuilder.toString();

    }


    private void closeConnections(PreparedStatement preparedStatement) {
        try {
            if (preparedStatement != null)
                preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

package com.yenlo.identity;

public class DataBaseDetails {
	//    "jdbc:mysql://localhost:3306/"+databaseName,"root", "india@1947"
	
	private  String dataBaseClassName="";
	private  String databaseDriverName="";
	private  String ipAddress="";
	private  String portNumber="";
	private  String DataBaseName="";
	private  String userName="";
	private  String password="";
	
	public String getDataBaseClassName() {
		return dataBaseClassName;
	}

	public String getDatabaseDriverName() {
		return databaseDriverName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getPortNumber() {
		return portNumber;
	}

	public String getDataBaseName() {
		return DataBaseName;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	
	public static class Builder {
		private  String dataBaseClassName="";
		private  String databaseDriverName="";
		private  String ipAddress="";
		private  String portNumber="";
		private  String DataBaseName="";
		private  String userName="";
		private  String password="";
		
		public Builder dataBaseClassName(String className) {
			this.dataBaseClassName=className;
			return this;
		}
		public Builder databaseDriverName(String DriverName){
			this.databaseDriverName=DriverName;
			return  this;
		}
		
		public Builder ipAddress(String ipAddress){
			this.ipAddress=ipAddress;
			return  this;
		}
		public Builder portNumber(String portNumber){
			this.portNumber=portNumber;
			return  this;
		}
		public Builder dataBaseName(String DataBaseName){
			this.DataBaseName=DataBaseName;
			return  this;
		}
		public Builder userName(String userName){
			this.userName=userName;
			return  this;
		}
		public Builder password(String password){
			this.password=password;
			return  this;
		}
		
		public DataBaseDetails build(){
			return new DataBaseDetails(this);
		}
	}
	
	private DataBaseDetails (Builder builder){
		this.dataBaseClassName=builder.dataBaseClassName;
		this.databaseDriverName=builder.databaseDriverName;
		this.ipAddress=builder.ipAddress;
		this.portNumber=builder.portNumber;
		this.DataBaseName=builder.DataBaseName;
		this.password=builder.password;
		this.databaseDriverName=builder.databaseDriverName;
		this.userName=builder.userName;
	}
	
}

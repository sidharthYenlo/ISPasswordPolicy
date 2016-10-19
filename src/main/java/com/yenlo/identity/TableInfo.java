package com.yenlo.identity;


public class TableInfo {
	private String tableName="";
	private int passwordHistoryCollumn;
	public String getTableName() {
		return tableName;
	}
	public int getPasswordHistoryCollumn() {
		return passwordHistoryCollumn;
	}
	public  static class Builder {
		private String tableName="";
		private int passwordHistoryCollumn;
		
		public Builder tableName(String tableName) {
			this.tableName=tableName;
			return this;
		}
		public Builder passwordHistoryCollumn(int passwordHistoryCollumn) {
			this.passwordHistoryCollumn=passwordHistoryCollumn;
			return this;
		}
		
		public TableInfo build(){
			return new TableInfo(this);
		}
	}
	
	private TableInfo (Builder builder){
		this.tableName=builder.tableName;
		this.passwordHistoryCollumn=builder.passwordHistoryCollumn;
	}

}

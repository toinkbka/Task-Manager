package com.hust.database;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "AppSave")
public class AppSave extends Model {

	@Column(name = "PROCESS_NAME")
	public String processName;

	public AppSave() {
		super();
	}


	

}

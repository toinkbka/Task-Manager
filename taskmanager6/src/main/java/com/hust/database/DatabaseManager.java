package com.hust.database;

import java.util.List;

import com.activeandroid.query.Select;

public class DatabaseManager {

	private static DatabaseManager instance;

	public DatabaseManager() {
		// TODO Auto-generated constructor stub
	}

	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	public List<AppSave> getAllListApp() {
		return new Select().from(AppSave.class).execute();
	}

	public AppSave getItemAppSave(String processName) {
		AppSave app = new Select().from(AppSave.class)
				.where("PROCESS_NAME=?", processName).executeSingle();
		return app;
	}

	public boolean isExistAppSave(AppSave app) {
		return getItemAppSave(app.processName) == null ? false : true;

	}

	public void deleteApp(AppSave app) {
		AppSave app1 = getItemAppSave(app.processName);
		if (app1 != null) {
			app1.delete();
		}

	}

}

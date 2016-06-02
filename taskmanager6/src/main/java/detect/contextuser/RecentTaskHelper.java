package detect.contextuser;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Lai Dong on 4/13/2016.
 */
public class RecentTaskHelper extends SQLiteOpenHelper{
    private static final String DATABASE_NAME = "recentTask.db";
    private static final int SCHEMA_VERSION = 1;

    static RecentTaskHelper instance;

    public static RecentTaskHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RecentTaskHelper(context);
        }

        return instance;
    }

    public RecentTaskHelper(Context context) {
        super(context, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE recentTask (_id INTEGER PRIMARY KEY AUTOINCREMENT, nameApp TEXT, count TEXT, packApp TEXT);");
        db.execSQL("CREATE TABLE disableTask (_id INTEGER PRIMARY KEY AUTOINCREMENT, nameApp TEXT, count TEXT, packApp TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // no-op, since will not be called until 2nd schema
        // version exists
    }

    public List<Task> getAllTask() {
        List<Task> tasks = new ArrayList<Task>();
        Cursor c = getReadableDatabase().rawQuery("select * from recentTask",
                null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            do {
                Task task = new Task();
                task.id = c.getInt(0);
                task.nameApp = c.getString(1);
                task.count = c.getInt(2);
                task.packApp=c.getString(3);
                tasks.add(task);
            } while (c.moveToNext() == true);
        }

        return tasks;
    }
    // get list of disabled apps
    public List<Task> getDisabledTask(){
        List<Task> tasks = new ArrayList<Task>();
        Cursor c = getReadableDatabase().rawQuery("select * from disableTask",
                null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            do {
                Task task = new Task();
                task.id = c.getInt(0);
                task.nameApp = c.getString(1);
                task.count = c.getInt(2);
                task.packApp=c.getString(3);
                tasks.add(task);
            } while (c.moveToNext() == true);
        }

        return tasks;
    }
    //insert an app to disabled List
    public void insertDisableTask(Task task){
        ContentValues cv=new ContentValues();
        cv.put("nameApp", task.nameApp);
        cv.put("count", task.count);
        cv.put("packApp", task.packApp);
        getWritableDatabase().insert("disableTask", "packApp", cv);
        Log.e("Database", "insert disabled task " + task.nameApp);
    }

    public void insert(Task task) {
        ContentValues cv = new ContentValues();

        cv.put("nameApp", task.nameApp);
        cv.put("count", task.count);
        cv.put("packApp", task.packApp);
        getWritableDatabase().insert("recentTask", "nameApp", cv);
        Log.e("Database", "insert task " + task.nameApp);
    }

    public void delete(String table,String pkg){
        getWritableDatabase().delete(table,"packApp= ?", new String[]{String.valueOf(pkg)});
    }
    public void update(int id, Task task) {
        ContentValues cv = new ContentValues();

        cv.put("nameApp", task.nameApp);
        cv.put("count", task.count);
        cv.put("packApp", task.packApp);
        getWritableDatabase().update("recentTask", cv, "_id=" + id, null);
        Log.e("Database", "update task " + task.nameApp);
    }

    public Task getTaskByNameApp(String nameApp) {
        Task task = null;
        Cursor c = getReadableDatabase().rawQuery(
                "select * from recentTask where nameApp=?",
                new String[] { nameApp });
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            task = new Task();
            task.id = c.getInt(0);
            task.nameApp = c.getString(1);
            task.count = c.getInt(2);
            task.packApp=c.getString(3);
        }

        return task;
    }

}

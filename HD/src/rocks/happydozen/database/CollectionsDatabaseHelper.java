package rocks.happydozen.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <h1>CollectionsDatabaseHelper is a {@link SQLiteOpenHelper} subclass</h1>
 * 
 * <p>Rather than create the tables here a class is created for each table
 * and the onCreate and onUpgrade are handled in the respective table
 * class</p>
 * 
 * @author Ross Studtman
 *
 */
public class CollectionsDatabaseHelper extends SQLiteOpenHelper {
	
	/**
	 * Database fields
	 */
	private static final String DATABASE_NAME = "happyDozen.db";
	
	/**
	 * Version 1
	 */
	private static final int DATABASE_VERSION = 1;
	
	public CollectionsDatabaseHelper(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		CollectionsTable.onCreate(db);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		CollectionsTable.onUpgrade(db, oldVersion, newVersion);		
	}
}

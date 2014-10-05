package rocks.happydozen.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * <h1>CollectionsTable creates the SQLite database table that
 * stores the following details:</h1>
 * 
 * <ul>
 * 		<li>Collection title</li>
 * 		<li>Image URI - as a String</li>
 * 		<li>Image Title</li>
 * 		<li>Image Sequence - image position in collection</li>
 * </ul>
 * 
 * @author Ross Studtman
 *
 */
public class CollectionsTable {
	// debugging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "CollectionsTable: ";
	
	// Database table
	/**
	 * Name of SQL table.
	 */
	public static final String COLLECTIONS_TABLE = "collections";
	/**
	 * Name of SQL id row.
	 */
	public static final String COL_ID = "_id";
	/**
	 * Name of collection.
	 */
	public static final String COL_NAME = "name";
	/**
	 * Uri of an image, as a string.
	 */
	public static final String COL_IMAGEURI = "uri_string";
	/**
	 * Image title.
	 */
	public static final String COL_TITLE = "title";
	/**
	 * 
	 */
	public static final String COL_SEQ = "sequence";
	
	// For backing up database during upgrades
	public static final String OLD_COLLECTIONS_TABLE = "old_table";
	
	public static void onCreate(SQLiteDatabase db){
		// sql string
		String sql = String
				.format("create table %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s TEXT, " +
						"%s TEXT, " +
						"%s TEXT, " +
						"%s INTEGER)",
						COLLECTIONS_TABLE, COL_ID, COL_NAME, COL_IMAGEURI, COL_TITLE, COL_SEQ);
		//Log.d(TAG, "Database created");
		db.execSQL(sql);
	}
	
	public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
//		// destroys all old data with this implementation		
//		db.execSQL("DROP TABLE IF EXISTS " + COLLECTIONS_TABLE);
//		CollectionsTable.onCreate(db);
		
		//Log.w(TAG, SCOPE + "Upgrading database from version "+oldVersion +" to " +newVersion);
		
		/* Copy data from old table to new table 
		 *
		 * NOTE: This is being done so I can work through the process. At this time
		 * I am not actually updating the database schema.
		 * 
		 * Also, a different technique: http://www.sqlite.org/faq.html#q11,
		 * under "How do I add or delete columns from an existing table in SQLite."
		 */
		
		// rename table	to backup table	
		String sqlRename = String.format("ALTER TABLE %s RENAME TO %s",
				COLLECTIONS_TABLE, OLD_COLLECTIONS_TABLE);
		
		db.execSQL(sqlRename);
		
		// Re-create the table you want
		String sqlRecreate = String
				.format("create table %s (" +
						"%s INTEGER PRIMARY KEY AUTOINCREMENT, " +
						"%s TEXT, " +
						"%s TEXT, " +
						"%s TEXT, " +
						"%s INTEGER)",
						COLLECTIONS_TABLE, COL_ID, COL_NAME, COL_IMAGEURI, COL_TITLE, COL_SEQ);
		
		db.execSQL(sqlRecreate);
		
		// insert old data into new table
		String sqlTransferData = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) SELECT %s, %s, %s, %s, %s FROM %s",
				COLLECTIONS_TABLE, COL_ID, COL_NAME, COL_IMAGEURI, COL_TITLE, COL_SEQ,
				COL_ID, COL_NAME, COL_IMAGEURI, COL_TITLE, COL_SEQ, 
				OLD_COLLECTIONS_TABLE);
		
		db.execSQL(sqlTransferData);
		
		// data has been transfered to new table, drop old table
		String sqlDropOldTable = String.format("DROP TABLE %s", OLD_COLLECTIONS_TABLE);
		
		db.execSQL(sqlDropOldTable);
		
	}
}

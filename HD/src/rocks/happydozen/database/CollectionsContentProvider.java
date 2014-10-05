package rocks.happydozen.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

public class CollectionsContentProvider extends ContentProvider {
	// debugging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "CollectionsContentProvider: ";
	
	/**
	 * Database
	 */
	private CollectionsDatabaseHelper dbHelper;	
	/**
	 * UriMatcher code for doing special query on CollectionsTable.
	 */
	private static final int GROUP_BY = 1;
	/**
	 * UriMatcher code for single row of CollectionsTable.
	 */
	private static final int SINGLE_ROW = 2;
	/**
	 * UriMatcher code for CollectionsTable.
	 */
	private static final int COLLECTIONS = 3;		
	/**
	 * SQL alias: Count(CollectionsTable.COL_NAME) AS "image_count"
	 */
	public static final String NUMBER_OF_IMAGES = "number_of_images";	
	/**
	 * Scheme
	 */
	private static final String SCHEME = "content://";	
	/**
	 * Authority
	 */
	private static final String AUTHORITY = "rocks.happydozen.provider";
	/**
	 * Content uri <b>path</b> for Collections table. 
	 */
	private static final String PATH = "collections";
	/**
	 * Content uri <b>path</b> for special query on Collections table.
	 */
	private static final String GROUP = "count_group";	
	
	/**
	 * Content URI that points to the CollectionsTable.
	 */
	public static final Uri COLLECTIONS_URI = Uri.parse(SCHEME + AUTHORITY + "/" + PATH);
	/**
	 * Content URI for getting distinct collections & number of images in them
	 */
	public static final Uri CONTENT_URI_GROUP = Uri.parse(SCHEME + AUTHORITY + "/" + GROUP);	
	
	/**
	 * Content type, used in <code>getType()</code>.
	 */
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/collections";
	/**
	 * Content item type.
	 */
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/collection";
	
	/**
	 * UriMatcher.
	 * Used to determine which case in switch is triggered.
	 */
	private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);	
	static{
		URI_MATCHER.addURI(AUTHORITY, GROUP, GROUP_BY);
		URI_MATCHER.addURI(AUTHORITY, PATH + "/#", SINGLE_ROW);
		URI_MATCHER.addURI(AUTHORITY, PATH, COLLECTIONS);
	}
	
	/**
	 * When creating this Provider also create a SQLiteOpenHelper.
	 */
	@Override
	public boolean onCreate() {
		dbHelper = new CollectionsDatabaseHelper(getContext());
		return false;
	}

	/*
	 * The URI is used to determine which query to run.	
	 */	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,	String[] selectionArgs, String sortOrder) {	
		
		Cursor cursor;
		
		// Provider could handle multiple tables, each case sets its own table.
		String table;
		
		// get writable database
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		// Choose table to query and sort order based on UriMatcher code 
		switch(URI_MATCHER.match(uri)){		
		
			// URI for querying Collections Table with special query.
			case GROUP_BY:
				/*
				 * Note getContentResolver could possibly do this:
				 * 		Cursor c = getContentResolver().query(COLLECTIONS_URI, new String[] {
				 * 			"count(distinct name) AS count"}, null, null, null);
				 */
				
				
				String sql = String.format("SELECT %s, %s, COUNT(%s) AS %s FROM %s GROUP BY %s",
						CollectionsTable.COL_ID, CollectionsTable.COL_NAME, CollectionsTable.COL_NAME, NUMBER_OF_IMAGES, 
						CollectionsTable.COLLECTIONS_TABLE, CollectionsTable.COL_NAME);				
				
				cursor = db.rawQuery(sql, null);	
				
				// This required to show real-time updates that reflect database changes.
				cursor.setNotificationUri(getContext().getContentResolver(), uri);
				
				// show what's in the cursor
				//Log.d(TAG, DatabaseUtils.dumpCursorToString(c));
				
				return cursor;
				
			// URI for querying a single row from CollectionTable, id is appended.
			case SINGLE_ROW:
				selection = selection + CollectionsTable.COL_ID + " = "	+ uri.getLastPathSegment();
				table = CollectionsTable.COLLECTIONS_TABLE;
				break;
	
			// URI for querying the ColelctionsTable.
			case COLLECTIONS:
	
				// Define default sort order.
				if (sortOrder == null) {
					sortOrder = CollectionsTable.COL_SEQ + " ASC";
				}
				table = CollectionsTable.COLLECTIONS_TABLE;
				break;

			// URI has no match.
			default:
				// Log.d("ROSS", SCOPE +"Cursor query Problem with URI: " + uri);
				throw new IllegalArgumentException("Uknown URI: " + uri);
		}
		
		cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);	
		
		// This required to show real-time updates that reflect database changes.
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		// Provider could handle multiple tables, each case sets which table.
		String table;
		
		// get database handle
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		// # of deleted rows
		int deleted = 0;
		
		switch(URI_MATCHER.match(uri)){
		
			case SINGLE_ROW:
				String rowID = uri.getLastPathSegment();			
				
				if(TextUtils.isEmpty(selection)){
					
					selection = CollectionsTable.COL_ID +" = " +rowID; 
					
				}else{
					
					selection = selection +" AND " +CollectionsTable.COL_ID +" = " +rowID;
				}
				
				table = CollectionsTable.COLLECTIONS_TABLE;
				break;
				
			case COLLECTIONS:
				table = CollectionsTable.COLLECTIONS_TABLE;
				break;
				
			default:
				//Log.d("ROSS", "CollectionsContentProvider Delete problem with URI: " + uri);
				throw new IllegalArgumentException("Uknown URI: " + uri);
		}
		
		deleted = db.delete(table, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return deleted;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		// Uri of table
		Uri tableUri;
		
		// get database handle
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		long id = 0;
		
		switch(URI_MATCHER.match(uri)){
		
			case COLLECTIONS:
				id = db.insert(CollectionsTable.COLLECTIONS_TABLE, null, values);	
				tableUri = COLLECTIONS_URI;
				break;
				
			default:
				//Log.d("ROSS", "CollectionsContentProvider Insert problem with URI: " + uri);
				throw new IllegalArgumentException("Uknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return Uri.parse(tableUri + "/" + id);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		// Provider could handle multiple tables, each case sets which table.
		String table;
		
		// get handle to database
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		
		// # of updated rows
		int updated = 0;
		
		switch(URI_MATCHER.match(uri)){
		
			case COLLECTIONS:
				table = CollectionsTable.COLLECTIONS_TABLE;
				break;
				
			case SINGLE_ROW:
				String rowID = uri.getLastPathSegment();			
				
				if(TextUtils.isEmpty(selection)){
					
					selection = CollectionsTable.COL_ID +" = " +rowID; 
					
				}else{
					
					selection = selection +" AND " +CollectionsTable.COL_ID +" = " +rowID;
				}
				
				table = CollectionsTable.COLLECTIONS_TABLE;
				break;		
				
			default:
				//Log.d("ROSS", "CollectionsContentProvider Update problem with URI: " + uri);
				throw new IllegalArgumentException("Uknown URI: " + uri);			
		}
		
		updated = db.update(table, values, selection, selectionArgs);
		
		//Log.d(TAG, "Provider calling notifyChange. The uri: " +uri.toString());
		getContext().getContentResolver().notifyChange(uri, null);				
		return updated;
	}
}

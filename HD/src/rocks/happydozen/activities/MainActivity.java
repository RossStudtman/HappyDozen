package rocks.happydozen.activities;

import rocks.happydozen.R;
import rocks.happydozen.database.CollectionsContentProvider;
import rocks.happydozen.database.CollectionsTable;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/**
 * <h1>MainActivity displays a list of collections the user has created.</h1>
 * <p>This is HappyDozen's launcher activity, the "landing page" of the app.</p>
 * <p>Users can accomplish the following actions from this activity:</p>
 * <ul>
 * 		<li>Add a collection of images to the app - menu bar click</li>
 * 		<li>Edit a collection - long click on list item</li>
 * 		<li>View a collection - click on list item</li>
 * </ul>
 * @author Ross Studtman
 *
 */
public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

	// logging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "MainActivity: ";
	
	/**
	 * Default bitmap.
	 */
	public static Bitmap defaultBitmap;
	
	// context menu id
	private static final int CONTEXT_MENU_EDIT_ID = 1;
	
	// loader manager id
	private static final int LOADER_ID = 1;
	private static final int LI_GALLERY_ACTIVITY = 2;
	
	// adapter
	private SimpleCursorAdapter simpleCursorAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set default bitmap
		defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.deleted );
		
		// get handle to listview
		ListView listvView = (ListView)findViewById(R.id.main_listview);
		
		listvView.setDividerHeight(2);
		listvView.setEmptyView(findViewById(R.id.main_empty_list));
		
		// register for context menu for long click on listview
		registerForContextMenu(listvView);
		
		
		String[] from = new String[] {CollectionsTable.COL_NAME, CollectionsContentProvider.NUMBER_OF_IMAGES};
		
		int[] to = new int[] { R.id.main_lvrow_title, R.id.main_lvrow_numberOfImages};
		
		simpleCursorAdapter = new SimpleCursorAdapter(
				getApplicationContext(),	// context
				R.layout.main_lv_row,		// xml row layout
				null,						// cursor
				from,						// columns from cursor
				to,							// views to map data to
				0);		
				
		listvView.setAdapter(simpleCursorAdapter);
		listvView.setOnItemClickListener(this);
		getLoaderManager().initLoader(LOADER_ID, null, this);
		//Log.d(TAG, SCOPE +"Finished MainActivity onCreate");
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// start a new or restart an existing Loader
		getLoaderManager().restartLoader(LOADER_ID, null, this);
	}	
	
	@Override
	public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
		
		// get cursor
		Cursor cursor = (Cursor) adapter.getItemAtPosition(position);		
		
		String collectionName = cursor.getString(cursor.getColumnIndex(CollectionsTable.COL_NAME));
		
		viewCollection(collectionName);
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		
		switch(id){
		
			case LOADER_ID:				
				String[] projectionMainActivity = {
						CollectionsTable.COL_ID,
						CollectionsTable.COL_NAME,
						CollectionsContentProvider.NUMBER_OF_IMAGES	};		
				
				CursorLoader cursorLoader = new CursorLoader(MainActivity.this,
						CollectionsContentProvider.CONTENT_URI_GROUP, projectionMainActivity, 
						null, null, null);
				
				return cursorLoader;
				

			case LI_GALLERY_ACTIVITY:
	
				// changed my mind on going this route
				
				return null;
				
			default:
				return null;
		}
		
		//Log.d(TAG, "Finished onCreateLoader");		
	}	
	
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		switch(loader.getId()){
		
			case LOADER_ID:
				
				//Log.d(TAG, SCOPE +"TEST MAIN Start onLoadFinished");
				
				simpleCursorAdapter.swapCursor(data);	
				
				//Log.d(TAG, "Finished onLoadFinished");
				
				break;
			}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
		simpleCursorAdapter.swapCursor(null);	
		
		//Log.d(TAG, SCOPE +"Finished onLoaderReset");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()){
			
			case R.id.menu_add:
				addCollection();
				
				// if you handle a menu item then return true.
				return true;
				
				// if you do not handle a menu item then return default super class implementation
			default:
				return super.onOptionsItemSelected(item);		
		}		
	}
	
	/*
	 * Floating menu on listview long click.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {		
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_MENU_EDIT_ID, 1, R.string.edit_collection);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {	
		
		switch(item.getItemId()){
		
			case CONTEXT_MENU_EDIT_ID:
				
				AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
				
				int itemPosition = info.position;
				
				// get handle to my cursor adapter
				Cursor cursor = simpleCursorAdapter.getCursor();
				
				cursor.moveToPosition(itemPosition);
				
				String collectionName = cursor.getString(cursor.getColumnIndex(CollectionsTable.COL_NAME));
				
				// prove I have the name of the collection
				//Log.d(TAG, SCOPE + "Collection name: " +etCollectionName);
				
				editCollection(collectionName);			
				
				return true;
			default:
				return super.onContextItemSelected(item);			
		}		
	}		
	
	/*
	 * Invoke AddActivity.class
	 * Creates a new collection.
	 */
	private void addCollection(){		
		Intent i = new Intent(MainActivity.this, AddActivity.class);
		startActivity(i);	
	}

	/*
	 * Send intent to EditActivity.class.
	 */
	private void editCollection(String collectionName) {	
		Intent i = new Intent(this, EditActivity.class);
		i.putExtra(EditActivity.IK_EDIT_ACTIVITY, collectionName);
		startActivity(i);
	}
	
	
	/*
	 * Invoke GalleryActivity.class.
	 * For viewing user's image gallery.
	 */
	private void viewCollection(final String collectionName){		
		Intent i = new Intent(this, GalleryActivity.class);
		i.putExtra(GalleryActivity.INTENT_KEY_GA, collectionName);
		startActivity(i);
	}


}

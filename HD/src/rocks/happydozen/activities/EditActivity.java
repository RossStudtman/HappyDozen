package rocks.happydozen.activities;

import rocks.happydozen.R;
import rocks.happydozen.adapters.EditCursorAdapter;
import rocks.happydozen.database.CollectionsContentProvider;
import rocks.happydozen.database.CollectionsTable;
import rocks.happydozen.fragments.AddImageFragment;
import rocks.happydozen.fragments.EditCollectionsFragment;
import rocks.happydozen.fragments.EditCollectionsFragment.OnAddImageListener;
import rocks.happydozen.fragments.EditImageFragment;
import rocks.happydozen.fragments.ListViewFragment;
import rocks.happydozen.fragments.ListViewFragment.ListFragListener;
import rocks.happydozen.utility.Constants;
import rocks.happydozen.utility.ImageHandler;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <h1>EditActivity gives user ability to edit a collection and the images within it.</h1>
 * <p>The following are editable:</p>
 * <ul>
 * 		<li>Add an image to the selected collection.</li>
 * 		<li>Change the name of the selected collection.</li>
 * 		<li>Delete the selected collection.</li>
 * 		<li>Change an image in the collection to another image.</li>
 * 		<li>Change the title of an image to a different title.</li>
 * </ul>
 * @author Ross Studtman
 *
 */
public class EditActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor>, 
	OnAddImageListener, ListFragListener {
	
	// logging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "EditActivity: ";
	
	// loader id
	public static final int LOADER_ID = 2;
	
	// intent key
	public static final String IK_EDIT_ACTIVITY = "edit activity";
	
	// fragment handles
	ListViewFragment listFrag;
	EditCollectionsFragment editCollectionFragment;
	AddImageFragment addImageFrag;
	EditImageFragment editImageFragment;
	
	
	// Fragment TAGS
	private final static String EDIT_COLLECTIONS_TAG = "edit collection fragment";
	public final static String EDIT_IMAGE_TAG = "edit image fragment";
	
	// adapter handle
	EditCursorAdapter adapter;
	
	// collection name
	protected String collectionTitle;	
	
	// ImageHandler
	ImageHandler imageHandler;
	
	/**
	 * The number of rows in the cursor that was returned by the Provider.
	 * This should equal the number of images in the collection.
	 * And is used to check against the maximum number of images allowed in the collection.
	 */
	public static int numberOfImagesInCollection;
	
	///////////////////////////////////////////////
	///////			LIFE CYCLE METHODS		///////
	///////////////////////////////////////////////	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit);
		
		// Create ImageHandler that holds LruCache
		imageHandler = new ImageHandler(this, getFragmentManager());
		
		if(savedInstanceState != null){
			collectionTitle = savedInstanceState.getString("collection title");
		}else{
			collectionTitle = getIntent().getExtras().getString(IK_EDIT_ACTIVITY);
		}

		
		// get handle to list view header
		TextView lvHeader = (TextView)findViewById(R.id.edit_act_lvHeader);
		lvHeader.setText("Change image or title:");
		
		if(savedInstanceState == null){					
			// create fragment for collection edit buttons
			editCollectionFragment = EditCollectionsFragment.newInstance(collectionTitle);				
			
			// programmatically add fragment to ViewGroup
			getFragmentManager().beginTransaction().replace(R.id.edit_topFrame, editCollectionFragment, EDIT_COLLECTIONS_TAG).commit();
			
		}else{
			
			// fragment that for sure exists
			//editCollectionFragment = (EditCollectionsFragment)getFragmentManager().findFragmentByTag(EDIT_COLLECTIONS_TAG);
			
			// fragments that might exist depending on user actions
			if(savedInstanceState.containsKey(EDIT_IMAGE_TAG)){
				
				// NOT CALLED
				//Log.d("ROSS", "EditActivity EditImageFragment obtained");
				editImageFragment = (EditImageFragment)getFragmentManager().getFragment(savedInstanceState, EDIT_IMAGE_TAG);				
			}
		}
		
		// Get fragment from XML
		listFrag = (ListViewFragment)getFragmentManager().findFragmentById(R.id.ListFragment);	

		// create adapter
		adapter = new EditCursorAdapter(this, null);
		
		// set list fragment adapter
		listFrag.setListAdapter(adapter);
		
		// prepare the loader
		getLoaderManager().initLoader(LOADER_ID, null, this);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString("collection title", collectionTitle);
		super.onSaveInstanceState(outState);
	}

	///////////////////////////////////////////////
	///////			PROVIDER METHODS		///////
	///////////////////////////////////////////////	
	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		
		// columns to fetch from Provider (must include _id column if used in Android's adapters).
		String[] projection = {
				CollectionsTable.COL_ID,
				CollectionsTable.COL_NAME,
				CollectionsTable.COL_IMAGEURI,
				CollectionsTable.COL_TITLE,
				CollectionsTable.COL_SEQ
		};		
		
		// SQL "WHERE"
		String selection = String.format("%s = ?", CollectionsTable.COL_NAME);
		
		// Replacement for WHERE's "?" - eliminates SQL inject attacks (?).
		String[] selectionArgs = {collectionTitle};
		
		// Create the cursor loader.
		CursorLoader cursorLoader = new CursorLoader(
				EditActivity.this,
				CollectionsContentProvider.COLLECTIONS_URI,
				projection, selection, selectionArgs, null);
		
		/*
		 *  CollectiosnContentProvider for the "COLLECTIONS_URI" is default to sort
		 *  by sequence number.
		 */
		
		return cursorLoader;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch(loader.getId()){
		case LOADER_ID:
			
			// inform the EditCollectionsFragment of imageCount
			numberOfImagesInCollection = cursor.getCount();
			
			// swap cursor
			adapter.swapCursor(cursor);	
			
			//Log.d(TAG, SCOPE + DatabaseUtils.dumpCursorToString(cursor));			
			break;
		}		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		
		Log.d("ROSS", "EditActivity...onLoaderReset called");
		adapter.swapCursor(null);		
	}

	///////////////////////////////////////////////
	///////			LISTENERS				///////
	///////////////////////////////////////////////	

	@Override
	public void createAddImageActivity() {		
	
		// user wants to add an image to the collection; ensure collection # !> 12	
		if(EditActivity.numberOfImagesInCollection < Constants.DEFAULT_COLLECTION_SIZE){
			
			Intent intent = new Intent(this, AddImage.class);		
			startActivityForResult(intent, 789);		
			
		}else{
			Toast.makeText(this, "Collection size: full.", Toast.LENGTH_LONG).show();
			
		}
				
	}
	
	/**
	 * AddImage activity sends a response back, catch it here.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		


		switch(requestCode){
		
		case 789:
			if(resultCode ==Activity.RESULT_OK){
				

				String ui = data.getStringExtra("ui");
				String title = data.getStringExtra("title");
				
				saveImage(ui, title);
			}
			else if(resultCode == Activity.RESULT_CANCELED){
				Log.d("ROSS", "AddActivity result canceled");
			}
		
			break;		
		
		}
	}


	public void saveImage(final String imageUri, final String title) {

		// Save user's selected image to database (via Provider).
		
		// AsyncTask					
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			
			final int NEXT_SEQUENCE_VALUE = 1;
			ContentValues values;				
			
			@Override
			protected Void doInBackground(Void... params) {
				/*
				 * Determine what the image's sequence number should be.
				 */
				// get handle to cursor
				Cursor cursor = adapter.getCursor();
				
				/*
				 * Testing why changing orientation of phone causes the implicit intent to obtain
				 * an image from the Gallery to cause a NullPointerException.
				 * What is known: when phone is tipped while in Gallery-searching mode,
				 * onLoaderReset is called, which null's out the cursor. And when that
				 * happens the cursor is null and the app crashed. Curiously, when the
				 * phone is tilted back again, even if while in the Gallery-searching mode,
				 * there is no issue, this cursor is not null. I don't know why one configuration
				 * change causes Null but the other doesn't. It boggles.
				 * 
				 * This does not occur with AddActivity even though the same mechanism is employed;
				 * but AddActivity does not use a cursor, either.
				 */
				if(cursor == null){
					//Log.d("ROSS", "cursor is null");
					
					// this didn't fix the null cursor.
					//getLoaderManager().initLoader(LOADER_ID, null, EditActivity.this);
				}
				

				
				//Log.d("ROSS", "1.5: cursor: " + DatabaseUtils.dumpCursorToString(cursor));
				// Move to last image in the database collection
				cursor.moveToLast();
				
				// NOTE: imperative that CursorLoader returned sort order ASCENDING of COL_SEQ,
				// otherwise "cursor.moveToLast()" would not guarantee the last image of the 
				// collection has the highest sequence number associated with it.
				// The provider has been coded to return a collection sorted by ascending 
				// sequence number. 
				
				// Get sequence number for that last image
				int sequence = cursor.getInt(cursor.getColumnIndex(CollectionsTable.COL_SEQ));
				
				
				// Add +1 to sequence number for image about to be added.
				sequence += NEXT_SEQUENCE_VALUE;

				values= new ContentValues();			
				values.put(CollectionsTable.COL_NAME, collectionTitle);
				values.put(CollectionsTable.COL_IMAGEURI, imageUri);
				values.put(CollectionsTable.COL_TITLE, title);
				values.put(CollectionsTable.COL_SEQ, sequence);
				
				
				getContentResolver().insert(CollectionsContentProvider.COLLECTIONS_URI, values);
				
				return null;
			}			
		};
		
		task.execute();					
		
	}

	/*
	 * <h1>Handler method for ListViewFragment.ListFragListener interface.</h1>
	 * 
	 * @param cursor is a cursor passed in from ListViewFragment's onItemClick method.
	 * 
	 * Question: if you didn't need a backstack...then why use ".addToBackStack"?
	 * Answer: I need *one* in the backstack or else when user clicks "back" they
	 * leave this whole activity rather than dismissing one fragment and reclaiming the 
	 * last one that was visible.
	 */
	@Override
	public void listFragListener(Cursor cursor) {
		
		// With each listview click there should be only one item in the backstack.
		getFragmentManager().popBackStack();
		
		// create new fragment
		editImageFragment = EditImageFragment.newInstance(cursor);
		
		// programmatically add new fragment
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.replace(R.id.edit_topFrame, editImageFragment, EDIT_IMAGE_TAG);
		ft.addToBackStack(null);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.commit();
	}

	public String getCollectionTitle() {
		return collectionTitle;
	}

	public void setCollectionTitle(String collectionTitle) {
		this.collectionTitle = collectionTitle;
	}
	
	public ImageHandler getImageHandler(){
		return imageHandler;
	}
	
}

package rocks.happydozen.activities;

import rocks.happydozen.R;
import rocks.happydozen.database.CollectionsContentProvider;
import rocks.happydozen.database.CollectionsTable;
import rocks.happydozen.fragments.ImageFragment;
import rocks.happydozen.utility.ImageHandler;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

/**
 * <h1>GalleryActivity displays a collection of images.</h1>
 * 
 * <p>Uses ImageFragments inside a ViewPager to display each
 * image from the collection.</p>				
 * 
 * @author Ross Studtman
 *
 */
public class GalleryActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	// logging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "GalleryActivity: ";
	
	/**
	 * Intent Key
	 */
	public static final String INTENT_KEY_GA = "gallery activity";
	
	// collection name
	String collectionName;
	
	// ImageHandler
	ImageHandler imageHandler;
	
	// handles to fragments
	ImageFragment imageFragment = null;
	
	private ViewPager mPager;
	private MyPagerAdapter mPagerAdapter;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// hide title bar
		requestWindowFeature(Window.FEATURE_NO_TITLE);		
		
		// set XML of this activity
		setContentView(R.layout.activity_gallery);		
		
		// hide status bar
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
				
		// Create ImageHandler that holds LruCache
		imageHandler = new ImageHandler(this, getFragmentManager());
		
		
		mPagerAdapter = new MyPagerAdapter(getSupportFragmentManager(), null);
		
		mPager = (ViewPager)findViewById(R.id.viewpager);
		
		mPager.setAdapter(mPagerAdapter);
						
		collectionName = getIntent().getStringExtra(INTENT_KEY_GA);	
		
		// Views
		TextView headerView = (TextView)findViewById(R.id.gallery_header);
		
		// Augment Views
		headerView.setText("Happy Collection: " +collectionName);	
		
		// Use Loader to handle saving cursor across configuration changes (eg, screen rotation).
		getLoaderManager().initLoader(1, null, this);
	}
	

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		// columns to fetch from Provider (don't need _id column since not using an Android adapter).
		String[] projection = {CollectionsTable.COL_IMAGEURI, CollectionsTable.COL_TITLE};	
		
		// SQL "WHERE"
		String selection = String.format("%s = ?", CollectionsTable.COL_NAME);
		
		// Replacement for WHERE's "?" - eliminates SQL inject attacks (?).
		String[] selectionArgs = {collectionName};
		
		
		return new CursorLoader(this, CollectionsContentProvider.COLLECTIONS_URI,
				projection, selection, selectionArgs, null);
	}


	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		
		mPagerAdapter.swapCursor(data);
		
		mPager.setCurrentItem(0);
	}


	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		
		mPagerAdapter.swapCursor(null);
		
	}	
	
	public ImageHandler getImageHandler(){
		return imageHandler;
	}
	
	private class MyPagerAdapter extends FragmentStatePagerAdapter{
		
		// cursor returned by provider
		Cursor galleryCursor;

		public MyPagerAdapter(FragmentManager fm, Cursor c) {
			super(fm);
			
			galleryCursor = c;
		}

		@Override
		public Fragment getItem(int position) {
			
			// Ensure that position sought is always within the confines of the Cursor's collection. 
			int movePosition = position % getCount();
			
			// Get row of cursor corresponding to movePosition
			galleryCursor.moveToPosition(movePosition);
			
			// Get details from cursor
			String imageUri = galleryCursor.getString(galleryCursor.getColumnIndex(CollectionsTable.COL_IMAGEURI));
			String imageTitle = galleryCursor.getString(galleryCursor.getColumnIndex(CollectionsTable.COL_TITLE));
			
			// create image fragment
			return ImageFragment.newInstance(imageUri, imageTitle);	
	
		}

		@Override
		public int getCount() {
			
			return galleryCursor == null? 0 : galleryCursor.getCount();			
		}
		
		public void swapCursor(Cursor c){
			if(galleryCursor == c){
				return;
			}
			
			galleryCursor = c;
			notifyDataSetChanged();
		}
		
		public Cursor getCursor(){
			return galleryCursor;
		}		
	}
}

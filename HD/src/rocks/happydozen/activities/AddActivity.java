package rocks.happydozen.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rocks.happydozen.R;
import rocks.happydozen.adapters.AddCollectionAdapter;
import rocks.happydozen.database.CollectionsContentProvider;
import rocks.happydozen.database.CollectionsTable;
import rocks.happydozen.fragments.AddActivity_Frag1;
import rocks.happydozen.fragments.AddActivity_Frag2;
import rocks.happydozen.fragments.AddActivity_Frag3;
import rocks.happydozen.fragments.AddImageFragment;
import rocks.happydozen.fragments.ListViewFragment;
import rocks.happydozen.interfaces.UpperFragmentsListener;
import rocks.happydozen.utility.Constants;
import rocks.happydozen.utility.ImageHandler;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

/**
 * <h1>AddActivity lets user create a new collection of images.</h1>
 * 
 * <p>	AddActivity is created when user wants to add a collection of images.
 * 		This activity allows the user to:</p>
 * 
 * 		<ul>
 * 			<li>Enter a name for the collection
 * 				<ul>
 * 					<li>Name must be present prior to being allowed to select images for the collection.</li>
 * 					<li>Checks if collection name is already present in database (via an AsyncTask)</li>
 * 				</ul>
 * 			</li>
 * 			<li>Add image(s) to the collection
 * 				<ul>
 * 					<li>Creates an AddImageFragment</li>
 * 				</ul>
 * 			</li>
 *  		<li>Display Images selections & titles in ListFragment</li> 
 * 		</ul>
 * @author Ross Studtman
 * 
 * <p>Issues:</p>
 * <ul><li>Currently breaks if user uses back button while choosing an image,
 * the screen will show AddActivity_Fragment3...but with just a title, no button</li></ul>
 *
 */
public class AddActivity extends Activity implements OnClickListener, UpperFragmentsListener {
	// logging
	private static final String TAG = "ROSS";
	
	// data collection
	List<ImageBean> beanList;
	
	// adapter
	AddCollectionAdapter adapter;
	
	/**
	 * ListViewFragment tag
	 */
	private static final String LISTFRAGMENT_TAG = "list fragment tag";
	
	private static final String TITLE_STATE = "nameOfCollection";
	
	/**
	 * Activity Tag
	 */
	public static final int ADD_IMAGE_CODE = 12345;
	
	// fragment handles
	ListViewFragment listFrag;
	
	// Fragments for upper frame
	AddActivity_Frag1 frag1;
	AddActivity_Frag2 frag2;
	AddActivity_Frag3 frag3;
	
	// fields
	String nameOfCollection;
	
	/**
	 * For handling bitmap creation
	 */
	ImageHandler imageHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add);
		
		// Create ImageHandler that holds LruCache
		imageHandler = new ImageHandler(this, getFragmentManager());		
		
		// Obtain retained List<ImageBean> or create new List<ImageBean>.
		RetainedFragment retainFragment = RetainedFragment.findOrCreateRetainFragment(getFragmentManager());
		
		beanList = retainFragment.list;
		
		if(beanList == null){
			
			beanList = new ArrayList<ImageBean>();
			
			retainFragment.list = beanList;			
		}		
		
		// create fragments
		if(savedInstanceState == null){
			
			// Create upper fragment, first one to be shown
			frag1 = new AddActivity_Frag1();
			
			// Create ListFragment
			listFrag = new ListViewFragment();	
			

			FragmentTransaction ft = getFragmentManager().beginTransaction();
			
			// Add fragments to ViewGroups
			//ft.add(R.id.add_fragFrame, listFrag, LISTFRAGMENT_TAG);			
			ft.replace(R.id.add_fragFrame, listFrag, LISTFRAGMENT_TAG);	
			ft.replace(R.id.add_upperFragFrame, frag1, AddActivity_Frag1.F1_TAG);
			ft.commit();
			
		}else{
			
			// Get collection's title
			nameOfCollection = savedInstanceState.getString(TITLE_STATE);
			
//			// This fragment will always be present.
			listFrag = (ListViewFragment)getFragmentManager().findFragmentByTag(LISTFRAGMENT_TAG);
//			//listFrag = (ListViewFragment)getFragmentManager().getFragment(savedInstanceState, LISTFRAGMENT_TAG);
//			
			Set<String> savedKeys = savedInstanceState.keySet();
//			
//			
			Log.d("ROSS", "AddActivity savedKeys (not null): " + savedKeys.toString());
//			/*
//			 * These fragments may or may not be present depending on user actions
//			 */
//			
//			// Determine if frag1 is present
//			if(savedInstanceState.containsKey(AddActivity_Frag1.F1_TAG)){
//				Log.d("ROSS", "AddActivity F1 fragment obtained");
//				frag1 = (AddActivity_Frag1)getFragmentManager().getFragment(savedInstanceState, AddActivity_Frag1.F1_TAG);
//			}
//			
//			// Determine if frag2 is present
//			if(savedInstanceState.containsKey(AddActivity_Frag1.F1_TAG)){
//				Log.d("ROSS", "AddActivity F2 fragment obtained");
//				frag2 = (AddActivity_Frag2)getFragmentManager().getFragment(savedInstanceState, AddActivity_Frag2.F2_TAG);
//			}			
//			
//			// Determine if frag3 is present
//			if(savedInstanceState.containsKey(AddActivity_Frag1.F1_TAG)){
//				frag3 = (AddActivity_Frag3)getFragmentManager().getFragment(savedInstanceState, AddActivity_Frag3.F3_TAG);
//			}	
//			
//			// Determine if AddImageFragment is present
//			if(savedInstanceState.containsKey(AddImageFragment.ADD_IMAGE_TAG)){
//				Log.d("ROSS", "AddActivity AddImageFragment obtained");
//				
//				addImageFrag = (AddImageFragment)getFragmentManager().getFragment(savedInstanceState, AddImageFragment.ADD_IMAGE_TAG);
//			}
		}
		
		// create adapter
		adapter = new AddCollectionAdapter(this, beanList);		
		
		// set list fragment adapter
		listFrag.setListAdapter(adapter);
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);
		
		outState.putString(TITLE_STATE, nameOfCollection);
		
		// You could save each fragment this way; but I found Android saves them anyway.
//		if(addImageFrag != null){
//			
//			getFragmentManager().putFragment(outState, AddImageFragment.ADD_IMAGE_TAG, addImageFrag);
//		}
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		
			case R.id.add_imageButton:
			case R.id.add_image_halfButton:		
				
				createAddImageActivity();				
				break;
				
			case R.id.add_saveAll:
				
				if(beanList.size() > 0){
					saveToDatabase();
				}else{
					Toast toast = Toast.makeText(this, getString(R.string.no_images_to_save), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}
				break;				
		}		
	}	
	
	/**
	 * Create an AddImage activity so user can add an image to the collection.
	 */
	private void createAddImageActivity() {
		
		Intent intent = new Intent(this, AddImage.class);		
		startActivityForResult(intent, ADD_IMAGE_CODE);			
	}

	/**
	 * AddImage activity sends a response back, catch it here.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch(requestCode){
		
		case ADD_IMAGE_CODE:
			if(resultCode ==Activity.RESULT_OK){
				String ui = data.getStringExtra("ui");
				String title = data.getStringExtra("title");
				
				beanList.add(new ImageBean(nameOfCollection, ui, title));
				adapter.notifyDataSetChanged();
				
				// user should not be able to add more than 12 images
				if(beanList.size() >= Constants.DEFAULT_COLLECTION_SIZE){
					
					// Inform user maximum number of images reached.
					Toast.makeText(this, "A Happy Dozen!", Toast.LENGTH_LONG).show();
					
					// Disable the ability to add more images.
					if(frag3 != null){
						frag3.disableAddImageButton();
					}		
				}
			}
			else if(resultCode == Activity.RESULT_CANCELED){
				//Log.d("ROSS", "AddActivity result canceled");
			}
		
			break;		
		
		}
	}

	/*
	 * ImageBean is an object for holding image details: collection title,
	 * image uri (as a string), and image title.
	 */
	public static class ImageBean{
		private String collectionName;
		private String imageUri;
		private String imageTitle;
		
		public ImageBean(String name, String uri, String title){
			collectionName = name;
			imageUri = uri;
			imageTitle = title;
		}
	
		public String getCollectionName() {
			return collectionName;
		}
	
		public String getImageUri() {
			return imageUri;
		}
	
		public String getImageTitle() {
			return imageTitle;
		}		
	}


	/*
	 * Called when user is finished selecting images.
	 */
	private void saveToDatabase() {
		int arraySize = beanList.size();
		
		final ContentValues[] valuesArray = new ContentValues[arraySize];
		
		ContentValues values;
		String imageuri;
		String title;
		int counter = 0;
		
		
		for(ImageBean image : beanList){
			
			imageuri = image.getImageUri();
			title = image.getImageTitle();			
			values = new ContentValues();	
			
			values.put(CollectionsTable.COL_NAME, nameOfCollection);
			values.put(CollectionsTable.COL_IMAGEURI, imageuri);
			values.put(CollectionsTable.COL_TITLE, title);
			values.put(CollectionsTable.COL_SEQ, counter +1);
			
			valuesArray[counter] = values;
			counter++;
		}
		
		AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
			
			@Override
			protected Void doInBackground(Void... arg0) {
				getContentResolver().bulkInsert(CollectionsContentProvider.COLLECTIONS_URI, valuesArray);	
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				
				// End this activity.
				finish();	
			}			
		};
		
		task.execute();					
	}	
	
	public ImageHandler getImageHandler(){
		return imageHandler;
	}
	
	public void setCollectionTitle(String title){
		nameOfCollection = title;
	}



	@Override
	public void upperFragmentListener(String fragTag) {
		
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		
		switch(fragTag){
		
		case AddActivity_Frag1.F1_TAG:
			
			// Create upper frag2
			frag2 = AddActivity_Frag2.newInstance(nameOfCollection);
			
			// programmatically add fragment to ViewGroup
			ft.replace(R.id.add_upperFragFrame, frag2, AddActivity_Frag2.F2_TAG).commit();			
			
			// set frag1 to null
			frag1 = null;
			break;
			
		case AddActivity_Frag2.F2_TAG:
			
			// Create Upper fragment 3
			frag3 = AddActivity_Frag3.newInstance(nameOfCollection);

			// programmatically add fragment to ViewGroup
			ft.replace(R.id.add_upperFragFrame, frag3, AddActivity_Frag3.F3_TAG).commit();	
			
			// Set frag2 to null
			frag2 = null;
	
			break;
			
		case AddActivity_Frag3.F3_TAG:			
			// don't need to do anything,
			// frag3 dies when activity finishes.			
			break;		
		}		
	}
	

}

/**
 * RetainedFragment retains a List<ImageBean> across configuration changes like screen rotation.
 * @author Ross Studtman
 *
 */
class RetainedFragment extends Fragment{
	
	// Make sure this tag is different from the RetainedFragment tag
	// used in MyLruCache!
	private static final String TAG = "RetainedFragment";
	
	/**
	 * Default constructor
	 */
	public RetainedFragment(){
		// Default constructor per the Fragment docs.
	}
	
	// data to retain
	public List<AddActivity.ImageBean> list;
	
	public static RetainedFragment findOrCreateRetainFragment(FragmentManager fm){
		
		RetainedFragment fragment = (RetainedFragment)fm.findFragmentByTag(TAG);
		
		if(fragment == null){
			
			fragment = new RetainedFragment();
			fm.beginTransaction().add(fragment, TAG).commit();
		}
		
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}	
}

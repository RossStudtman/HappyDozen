package rocks.happydozen.fragments;

import rocks.happydozen.R;
import rocks.happydozen.activities.EditActivity;
import rocks.happydozen.database.CollectionsContentProvider;
import rocks.happydozen.database.CollectionsTable;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <h1>EditImageFragment lets the user edit an Image.</h1>
 * <p>The user can edit an image in the following ways:</p>
 * <ul>
 * 		<li>Change the image to another image.</li>
 * 		<li>Change the title of the image.</li>
 * 		<li>Delete the image.</li>
 * </ul>
 * @author Ross Studtman
 *
 */
public class EditImageFragment extends Fragment implements OnClickListener {
	// logging
	public static final String TAG = "ROSS";
	private static final String SCOPE = "EditImageFragment: ";
	
	// activity code for browsing gallery
	private static final int PHOTO_GALLERY_RQCODE = 1;
	
	// Image fields
	long id;
	String imageUri;
	String title;
	
	// view handles for scope reasons.
	TextView tvImageHeader;
	Button btnChangeTitle;
	ImageButton imageButton;
	
	// Default bitmap
	Bitmap defaultBitMap; 	

	/**
	 * Constructor.
	 */
	public static EditImageFragment newInstance(Cursor cursor){
		EditImageFragment fragment = new EditImageFragment();
		
		Log.d("ROSS", "EditImageFragment - new instance called.");
		Bundle bundle = fragment.getArguments();
		
		if(bundle == null){
			bundle = new Bundle();
		}

		bundle.putLong("cursorId", cursor.getLong(cursor.getColumnIndex(CollectionsTable.COL_ID)));
		bundle.putString("uri", cursor.getString(cursor.getColumnIndex(CollectionsTable.COL_IMAGEURI)));
		bundle.putString("title", cursor.getString(cursor.getColumnIndex(CollectionsTable.COL_TITLE)));
		
		fragment.setArguments(bundle);
		return fragment;
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		// TODO Auto-generated method stub
		super.onAttach(activity);
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if(savedInstanceState != null){
			//Log.d(TAG, SCOPE +"savedInstanceState.getString with key of getTag(): " +savedInstanceState.getString(getTag()));
		}

		// inflate view
		View view = inflater.inflate(R.layout.frag_edit_image, container, false);
		
		// get bundle args
		id = getArguments().getLong("cursorId");
		imageUri = getArguments().getString("uri");
		title = getArguments().getString("title");
		
		// get view handles
		tvImageHeader = (TextView)view.findViewById(R.id.edit_image_header);
		Button btnDelete = (Button)view.findViewById(R.id.edit_image_btnDelete);
		imageButton = (ImageButton)view.findViewById(R.id.edit_image_btnImage);
		btnChangeTitle = (Button)view.findViewById(R.id.edit_image_btnTitle);
		ImageButton btnFinished = (ImageButton)view.findViewById(R.id.edit_image_btnSave);
		
		// augment views		
		tvImageHeader.setText("Edit \"" +title +"\":");
		btnChangeTitle.setText(title);
		
		/*
		 * These are all zero. 
		 * 
		 * See ImageFragment for how it uses a ViewTreeObserver to do what
		 * you attempted to do here.
		 */
		// Get dimensions of Button with image.
//		int imageButtonWidth = imageButton.getWidth();
//		int imageButtonHeight = imageButton.getHeight();
//		
//		Log.d(TAG, SCOPE +"imageButton width: "+imageButtonWidth);
//		Log.d(TAG, SCOPE +"imageButton height: "+imageButtonHeight);
//		
//		int measuredWidth = imageButton.getMeasuredWidthAndState();
//		int measuredHeight = imageButton.getMeasuredHeightAndState();
//		
//		Log.d(TAG, SCOPE +"measured width: "+measuredWidth);
//		Log.d(TAG, SCOPE +"measured height: "+measuredHeight);
		
		// XML ImageButton attributes
		int XML_WIDTH = 250;
		int XML_HEIGHT = 250;
		
		// Load bitmap into imageButton
		((EditActivity)getActivity()).getImageHandler().loadBitmap(imageUri, imageButton, XML_WIDTH, XML_HEIGHT);
		
		// view actions
		btnDelete.setOnClickListener(this);
		imageButton.setOnClickListener(this);
		btnChangeTitle.setOnClickListener(this);
		btnFinished.setOnClickListener(this);
		
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		
		if(this.isVisible()){
			
			// TESTING how onSaveInstanceState works
			outState.putString(getTag(), getTag());
		}
		super.onSaveInstanceState(outState);
		//String tag = getActivity().getFragmentManager().getBackStackEntryAt(getActivity().getFragmentManager().getBackStackEntryCount() - 1).getName();
		//outState.putString("fragTest", tag);
	}


	@Override
	public void onClick(View view) {
		
		switch(view.getId()){
		
			case R.id.edit_image_btnDelete:
				deleteEntry();
				break;
				
			case R.id.edit_image_btnImage:
				browseGallery();
				break;
				
			case R.id.edit_image_btnTitle:
				changeTitle();
				break;
				
			case R.id.edit_image_btnSave:
				save();
				break;
		}		
	}
	
	/**
	 * User is finished with image edits.
	 */
	private void save() {
		
		//Log.d(TAG, SCOPE +"backstack size: "+ getActivity().getFragmentManager().getBackStackEntryCount());	
		
		getActivity().getFragmentManager().popBackStack();			
	}

	/**
	 * Delete image from collection.
	 */
	private void deleteEntry() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Delete Entry");
		builder.setMessage("This will delete this entry. Are you sure?");
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
					
					// inform user what is going on
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage("deleting entry...");
					final AlertDialog dlg = builder.create();
					dlg.show();
					
					// AsyncTask					
					AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
						
						@Override
						protected Void doInBackground(Void... params) {
							
							// Provider URI with appended id
							Uri uri = Uri.parse(CollectionsContentProvider.COLLECTIONS_URI + "/" +id);							
							
							// provider call
							getActivity().getContentResolver().delete(uri, null, null);
							
							return null;
						}
						@Override
						protected void onPostExecute(Void result) {
							
							// return to activity
							getActivity().getFragmentManager().popBackStack();
							dlg.dismiss();
						}			
					};
					
					task.execute();					
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.create().show();
		
	}

	/**
	 * User selecting new image.
	 */
	public void browseGallery() {
		
		// intent to explicitly launch photo gallery (rather than use implicit intent).
		Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	
		startActivityForResult(i, PHOTO_GALLERY_RQCODE);
	}

	/*
	 * Activities get the first whack at a returned result, then fragments.
	 * 
	 * If this doesn't work it may be because the Activity isn't implementing an onActivityResult;
	 * more specifically, for people that have implemented and gotten rid of the "super()" call
	 * then the fragment doesn't get a chance at the result.
	 * http://stackoverflow.com/questions/6147884/onactivityresult-not-being-called-in-fragment
	 * 
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		switch(requestCode){
		
		case PHOTO_GALLERY_RQCODE:
			if(resultCode == Activity.RESULT_OK){		
				
				// Defines which columns should be returned from Provider table.
				String[] filePathColumn = {MediaStore.Images.Media.DATA};								
				
				// Provider's URI and row id
				Uri galleryImageUri = intent.getData();
				
				// Only allow images from Gallery
				if(galleryImageUri.toString().startsWith("content://media")){

					Cursor cursor = getActivity().getContentResolver().query(galleryImageUri, filePathColumn, null,	null, null);								
					
					// move to first row of cursor
					cursor.moveToFirst();
					
					// set imageUri string
					imageUri = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
					
					// do database call here
					// inform user what is going on
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage("updating image...");
					
					final AlertDialog dlg = builder.create();
					dlg.show();
					
					// Update database with new image URI for the row with "_id"				
					AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
						
						@Override
						protected Void doInBackground(Void... params) {
							
							// Provider URI with appended id
							Uri uri = Uri.parse(CollectionsContentProvider.COLLECTIONS_URI + "/" +id);
							
							// values to insert
							ContentValues values = new ContentValues();
							values.put(CollectionsTable.COL_IMAGEURI, imageUri);
							
							// provider call
							getActivity().getContentResolver().update(uri, values, null, null);
							
							return null;
						}
						@Override
						protected void onPostExecute(Void result) {
							// augment views		
							imageButton.setImageBitmap(ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imageUri), 250, 250));
							dlg.dismiss();
						}			
					};
					
					task.execute();	
				}else{
					
					// Inform user that only Gallery images are currently supported.					
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setTitle("Gallery Images");
					
					TextView info = new TextView(getActivity());
					
					info.setText("Happy Dozen currently supports images selected from the Gallery.");
					
					builder.setView(info);
					
					builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							getActivity().getFragmentManager().popBackStack();
							
						}
					});
					
					//builder.setNegativeButton("Cancel", null);
					builder.create().show();	
				}
			}
			// user hits the back button: without this else-if it would crash with F2 emulator shortcut ("back button")
			else if(resultCode == Activity.RESULT_CANCELED){
				Toast.makeText(getActivity(), "Aww", Toast.LENGTH_SHORT).show();
			}			
			break;
		}
	}
	
	


	/*
	 * Change title of image.
	 */
	private void changeTitle() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Please enter new title");
		final EditText input = new EditText(getActivity());
		builder.setView(input);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if(input.getText().toString().trim().length() > 0){
					
					// change title string
					title = input.getText().toString();
					
					// inform user what is going on
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					builder.setMessage("updating title...");
					final AlertDialog dlg = builder.create();
					dlg.show();
					
					// AsyncTask					
					AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
						
						@Override
						protected Void doInBackground(Void... params) {
							// Provider URI with appended id
							Uri uri = Uri.parse(CollectionsContentProvider.COLLECTIONS_URI + "/" +id);
							
							// values to insert
							ContentValues values = new ContentValues();
							values.put(CollectionsTable.COL_TITLE, title);
							
							// provider call
							getActivity().getContentResolver().update(uri, values, null, null);
							
							return null;
						}
						@Override
						protected void onPostExecute(Void result) {
							// augment views		
							tvImageHeader.setText("Edit \"" +title +"\":");
							btnChangeTitle.setText(title);
							dlg.dismiss();
						}			
					};
					
					task.execute();	
				}else{
					Toast.makeText(getActivity(), "Title cannot be empty.", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		builder.setNegativeButton("Cancel", null);
		builder.create().show();		
	}

	
}
package rocks.happydozen.fragments;

import rocks.happydozen.R;
import rocks.happydozen.activities.AddImage;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * <h1>AddImageFragment lets the user select an image from their gallery.</h1>
 * 
 * <p>AddImageFragment is used by AddImage activity; AddImage activity 
 * is created by:</p>
 * <ul> 
 * 		<li>EditActivity</li>
 * 		<li>AddImage activity.</li>
 * </ul> 
 */
public class AddImageFragment extends Fragment {
	// logging
	private static final String TAG = "ROSS";
	public static final String SCOPE = "AddImageFragment: ";
	
	/**
	 * Tag for this fragment
	 */
	public static final String ADD_IMAGE_TAG = "add image fragment tag";
	
	/**
	 * Activity code for browsing gallery
	 */
	private static final int PHOTO_GALLERY_RQCODE = 1;

	/**
	 *  image uri as string
	 */
	String imageUri;
	/**
	 *  title of image
	 */
	String title;

	/**
	 *  view handles
	 */
	ImageView imageView;
	EditText titleEditText;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.frag_add_image, container, false);

		// get handle to views
		imageView = (ImageView) view.findViewById(R.id.frag_addImage_image);
		titleEditText = (EditText) view.findViewById(R.id.frag_addImage_title);
		ImageButton save = (ImageButton) view.findViewById(R.id.frag_addImage_btnSave);

		// view actions
		save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
					
				// is there text in the EditText view?
				if (titleEditText.getText().toString().trim().length() > 0) {
					
					title = titleEditText.getText().toString();
					
					// Return results to activity
					Intent data = new Intent();
					data.putExtra("ui", imageUri);
					data.putExtra("title", title);
					
					getActivity().setResult(Activity.RESULT_OK, data);
					
					getActivity().finish();
					
				} else {
					
					Toast toast = Toast.makeText(getActivity(), getActivity().getString(R.string.missing_title), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					
					//Toast.makeText(getActivity(), "Missing Title", Toast.LENGTH_SHORT).show();
				}								
			}
		});

		// Only browse Gallery if fragment has not been recreated with device rotation
		if(savedInstanceState == null){
			
			browseGallery();
		}else{
			imageUri = savedInstanceState.getString("image_uri");
			
			// Get image from ImageHandler and set on imageView.
			((AddImage)getActivity()).getImageHandler().loadBitmap(imageUri, imageView, imageView.getWidth(), imageView.getHeight());
		}

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("image_uri", imageUri);
		super.onSaveInstanceState(outState);
	}

	/**
	 * Note: I am trying, unsuccessfully to find a way for the user to ONLY select images from
	 * the Gallery. At this time I do not want to store URI's for images not on the device, I don't
	 * want the user to have to have a connection to view images; I don't want to query an online
	 * repository and copy the image to the Gallery; and time constraints do not avail themselves
	 * to rewriting each place that would need to query an online repository. 
	 */
	public void browseGallery() {
		
		// Explicit intent specifies which activity should handle the intent.
		// This is an implicit intent, it does not name the specific component to start, leaving it up to the system 
		// (which might give the user a choice of apps to satisfy the request) to decide which action to take.
		/*
		 * Note: This is considered a "depreciated" way to do this:
		 * 		http://stackoverflow.com/questions/6486716/using-intent-action-pick-for-specific-path/6486827#6486827
		 * 
		 * 
		 * 		Intent intent = new Intent(Intent.ACTION_PICK,	android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		 * 		startActivityForResult(intent, PHOTO_GALLERY_RQCODE);
		 * 
		 * Implementing new (to me) approach:
		 */
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
		
		// This creates a "Chooser" and gives the user a list of possible apps to use in getting their image
		startActivityForResult(Intent.createChooser(intent, "Please select Gallery:"), PHOTO_GALLERY_RQCODE);
		
		// Actually this code produces the same list of possibilities as the above
		//startActivityForResult(intent, PHOTO_GALLERY_RQCODE);

		
	}

	/*
	 * Activities get the first whack at a returned result, then fragments.
	 * 
	 * If this doesn't work it may be because the Activity isn't implementing an
	 * onActivityResult; more specifically, for people that have implemented and
	 * gotten rid of the "super()" call then the fragment doesn't get a chance
	 * at the result.
	 * http://stackoverflow.com/questions/6147884/onactivityresult
	 * -not-being-called-in-fragment
	 * 
	 * More Notes:
	 * 		intent.Data() returns a URI:
	 * 
	 * 			If user selects from Gallery:
	 * 				Uri uri = intent.getData():
	 * 					uri.toString() --> content://media/external/images/media/22" 
	 * 
	 * 					--> That is the address to the Provider's table and the last number is the row id of that record.
	 * 
	 * 				To show value of _ID column from returned URI
	 * 					ContentUris.parseID(uri) --> 22
	 * 
	 * 					(if above used with result returned below (Photos) throws NumberFormatException)
	 * 					
	 * 
	 * 			If user selects from Photos:
	 * 				content://com.google.android.apps.photos.content/0/https%3A%2F%2Flh4.googleusercontent.com%2FW7e_sUoprsDV94gbcmhft8UVI3ShBL5QADcoKxEX3X4%3Ds0-d
	 * 
	 * 			String[] filePathColumn = { MediaStore.Images.Media.DATA };
	 * 
	 * 				--> This is a column from the Provider's table. 
	 * 
	 * 
	 * 			Query a table: With the URI to target a table, and column from the table we are interested in:
	 * 
	 * 				Cursor cursor = getActivity().getContentResolver().query(
	 * 					galleryImageUri 	--> this is the content URI which maps to a table in the Provider
	 * 					filePathColumn 		-->	the column to return for each row, like a SQL SELECT column
	 * 					null				--> selection criteria, like a SQL WHERE
	 * 					null				--> selection criteria, if above uses "?" these are the arguments to replace those
	 * 					null);				--> the sort order, like SQL ORDER BY
	 * 
	 * 				--> Note the "resolver" resolves the URI into a content//authority/path/id
	 * 				--> Because there is an ID at the end of the URI the query will return only that row.
	 * 
	 * 			For my purposes, obtain URI as a string:
	 * 
	 * 				int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
	 * 				String imageUri = cursor.getString(columnIndex);
	 * 
	 * 				More often, combined into:
	 * 
	 * 				String imageUri = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
	 * 
	 * 					String looks like --> "/mnt/sdcard/ImageTitle.jpg"
	 * 
	 * 
	 * 				--> And that is the actual location of the user's selected image.
	 * 
	 * 				IE, that is the IMAGE's URI.
	 * 				The URI above is for the image's location in the Provider table. 				
	 * 	
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		
		switch (requestCode) {
		
		case PHOTO_GALLERY_RQCODE:
			if (resultCode == Activity.RESULT_OK) {				

				// Defines which columns should be returned from Provider table. Returned string represents the data stream for the file
				String[] filePathColumn = { MediaStore.Images.Media.DATA }; // http://developer.android.com/reference/android/provider/MediaStore.MediaColumns.html#DATA

				// URI of image, where the image is located in the Provider's table (Android's Gallery) that houses this information.
				Uri galleryImageUri = intent.getData();
				
				// Only allow images from Gallery
				if(galleryImageUri.toString().startsWith("content://media")){
					
					// Get the specific row from the Provider table
					Cursor cursor = getActivity().getContentResolver().query(galleryImageUri, filePathColumn, null,	null, null);

					// move to first row of cursor
					cursor.moveToFirst();

					// obtain column index for data sought
					int columnIndex = cursor.getColumnIndex(filePathColumn[0]); // MediaStore.Images.Media.DATA

					// set imageUri string
					imageUri = cursor.getString(columnIndex); // "/mnt/sdcard/ImageTitle.jpg"

					// set fragment's ImageView
					((AddImage)getActivity()).getImageHandler().loadBitmap(imageUri, imageView, imageView.getWidth(), imageView.getHeight());

					// move focus to EditText view.
					titleEditText.requestFocus();
					
					if(cursor !=null){
						cursor.close();
					}
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
							
							//getActivity().getFragmentManager().popBackStack();
							getActivity().finish();
							
						}
					});
					
					//builder.setNegativeButton("Cancel", null);
					builder.create().show();					
					

				}


			}
			// user hits the back button: without this else-if it would crash
			// with F2 emulator shortcut ("back button")
			else if (resultCode == Activity.RESULT_CANCELED) {
				
				Toast toast = Toast.makeText(getActivity(), getActivity().getString(R.string.user_didnt_pick_image), Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				
				getActivity().finish();
			}
			break;
		}
	}

}


/*   
		
		This is to remind that in some cases Views need not be assigned to variables:
		
		
		((Button) findViewById(R.id.Button01)).setOnClickListener( new OnClickListener() {

	        public void onClick(View v) {
	
	            Intent intent = new Intent();
	            
	            intent.setType("image/*");
	            
	            intent.setAction(Intent.ACTION_GET_CONTENT);
	            
	            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PHOTO_GALLERY_RQCODE);
	        }
                    
		});
                
*/

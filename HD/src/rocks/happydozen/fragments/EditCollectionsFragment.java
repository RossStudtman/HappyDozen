package rocks.happydozen.fragments;

import rocks.happydozen.R;
import rocks.happydozen.activities.EditActivity;
import rocks.happydozen.database.CollectionsContentProvider;
import rocks.happydozen.database.CollectionsTable;
import rocks.happydozen.utility.Constants;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * <h1>EditCollectionsFragment allows the user to edit an image collection.</h1>
 * <p>The user can edit an image collection in the following ways:</p>
 * <ul>
 * 		<li>Add an image --> request parent activity to create AddImageFragment.</li>
 * 		<li>Change collection name.</li>
 * 		<li>Delete the collection --> which finishes the parent activity.</li>
 * </ul>
 * @author Ross Studtman
 *
 */
public class EditCollectionsFragment extends Fragment implements OnClickListener {	
	// logging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "EditCollectionsFragment: ";

	/**
	 * Listener.
	 * @author Ross Studtman
	 *
	 */
	public interface OnAddImageListener{
		public void createAddImageActivity();
	}
	
	// listener handle
	OnAddImageListener listener;
	
	// View handles
	Button addImageButton;
	TextView tvCollectionTitle;
	
	// collection name
	private String collectionTitle;
	
	/*
	 * Constructor.
	 */
	public static EditCollectionsFragment newInstance(String collectionTitle){
		EditCollectionsFragment f = new EditCollectionsFragment();
		
		Bundle bundle = f.getArguments();
		
		if(bundle == null){
			bundle = new Bundle();
		}
		
		bundle.putString("title", collectionTitle);
		f.setArguments(bundle);
		return f;
	}
	
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if(activity instanceof OnAddImageListener){
			
			listener = (OnAddImageListener)activity;		
			
		}else{
			//Log.d(TAG, SCOPE + "Instantiating activity does not implement OnAddImageListener.");
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		if(savedInstanceState != null){
			collectionTitle = savedInstanceState.getString("collection title");
		}else{
			// get bundle args
			collectionTitle = getArguments().getString("title");
		}
		
		// inflate view
		View view = inflater.inflate(R.layout.frag_edit_collection, container, false);
		
		// view handles
		tvCollectionTitle = (TextView)view.findViewById(R.id.edit_frag_collectionTitle);
		tvCollectionTitle.setText("Edit \"" + collectionTitle +"\" Collection" +":");
		addImageButton = (Button)view.findViewById(R.id.edit_frag_btnAddImage);

		Button changeNameButton = (Button)view.findViewById(R.id.edit_frag_btnChangeName);
		Button deleteButton = (Button)view.findViewById(R.id.edit_frag_btnDelete);
		
		// view actions
		addImageButton.setOnClickListener(this);
		changeNameButton.setOnClickListener(this);
		deleteButton.setOnClickListener(this);
			
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("collection title", collectionTitle);
	}

	@Override
	public void onClick(View v) {
		
		switch(v.getId()){
		
			case R.id.edit_frag_btnAddImage:
				addImage();
				break;
				
			case R.id.edit_frag_btnChangeName:
				updateCollectionName();
				break;		
				
			case R.id.edit_frag_btnDelete:
				deleteCollection();
				break;			
		}		
	}	

	
	/*
	 * Add an image to the collection.
	 */
	private void addImage() {
		/*
		 * Send the activity a notification to build the AddImage activity.
		 */
		if(EditActivity.numberOfImagesInCollection < Constants.DEFAULT_COLLECTION_SIZE){
			
			listener.createAddImageActivity();
			
		}else{
			
			Toast toast = Toast.makeText(getActivity(), getString(R.string.collection_size_full_), Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}		
	}


	/*
	 * Update the name of a collection.
	 * 
	 * NOTE: another option would have been to pass the activity the results of the user's
	 * input and have the activity run the asynctask and db.update.
	 */
	private void updateCollectionName() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Please enter new Happy Collection Name");
		
		final EditText input = new EditText(getActivity());
		
		builder.setView(input);
		
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				if(input.getText().toString().trim().length() > 0){
					
					// inform user what is going on
					AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
					
					builder.setMessage("updating collection name...");
					
					final AlertDialog dlg = builder.create();
					dlg.show();
					
					// New Collection name
					final String newCollectionName = input.getText().toString();
					
					// AsyncTask					
					AsyncTask<String, Void, Void> task = new AsyncTask<String, Void, Void>() {
						
						@Override
						protected Void doInBackground(String... params) {
							
							// Get new name of collection and save in a ContentValue
							ContentValues values = new ContentValues();
							
							values.put(CollectionsTable.COL_NAME, newCollectionName);							
							
							// WHERE SQL column "COL_NAME" = ?
							String where = String.format("%s =?", CollectionsTable.COL_NAME);
							
							// Supply argument for WHERE's question mark.
							String[] selectionArgs = {collectionTitle};							
							
							getActivity().getContentResolver().update(CollectionsContentProvider.COLLECTIONS_URI, values, where, selectionArgs);
							
							return null;
						}
						
						@Override
						protected void onPostExecute(Void result) {
							
							// Change activity's collectionTitle String.
							((EditActivity)getActivity()).setCollectionTitle(newCollectionName);
							
							// Make activity restart its loader
							getActivity().getLoaderManager().restartLoader(EditActivity.LOADER_ID, null, (EditActivity)getActivity());
							
							// Set the Collection title in this fragment
							collectionTitle = newCollectionName;	
							 
							// Set the text on the textview:
							tvCollectionTitle.setText("Edit \"" + collectionTitle +"\" Collection");

							dlg.dismiss();
						}			
					};
					
					task.execute(newCollectionName);	
					
				}else{
					Toast.makeText(getActivity(), "Name cannot be empty.", Toast.LENGTH_LONG).show();
				}
				
			}
		});
		
		builder.setNegativeButton("Cancel", null);
		builder.create().show();
	}

	/*
	 * Delete a collection.
	 */
	private void deleteCollection() {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {	
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//String where = String.format("%s =?", CollectionsTable.COL_NAME);
				//String[] selectionArgs = {collectionTitle};
				
				//getActivity().getContentResolver().delete(CollectionsContentProvider.CONTENT_URI, where, selectionArgs);
				
				// end EditActivity since we just deleted the collection it was based on
				//listener.dataFromEditFrag(true);
				new DeleteCollectionTask().execute(collectionTitle);
			}
		});
		
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				return;				
			}
		});
		
		builder.setMessage("This will delete this entire collection. Are you sure?");
		
		AlertDialog dialog = builder.create();		
		dialog.show();
	}
	
	/*
	 * Async Task for deleting a collection.
	 */
	private class DeleteCollectionTask extends AsyncTask<String, Void, Boolean>{
		
		@Override
		protected Boolean doInBackground(String... params) {
			
			String where = String.format("%s =?", CollectionsTable.COL_NAME);
			String[] selectionArgs = {params[0]};
			
			getActivity().getContentResolver().delete(CollectionsContentProvider.COLLECTIONS_URI, where, selectionArgs);
			return true;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			
			// Inform Activity to finish
			getActivity().finish();
		}		
	}
	
}

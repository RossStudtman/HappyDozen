package rocks.happydozen.fragments;


import rocks.happydozen.R;
import rocks.happydozen.activities.AddActivity;
import rocks.happydozen.database.CollectionsContentProvider;
import rocks.happydozen.database.CollectionsTable;
import rocks.happydozen.interfaces.UpperFragmentsListener;
import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * <h1>Displays at the top of AddActivity.</h1>
 * <p>Displays:</p> 
 * <ul>
 * 		<li>EditText - entering collection title</li>
 * 		<li>Button - for saving collection title</li>
 * 		<ul>
 * 		<li>Asynchronously checks if name is in database first.</li>
 * 		<li>If unique name inform Activity to replace this fragment
 * 			with AddActivity_Frag2.</li></ul>
 * </ul>
 * @author Ross Studtman
 *
 */
public class AddActivity_Frag1 extends Fragment {
	
	/**
	 * Tag for this fragment
	 */
	public static final String F1_TAG = "AddActivity_Frag1 Tag";
	
	// Interface listener
	UpperFragmentsListener listener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if(activity instanceof AddActivity){
			
			listener = (AddActivity)activity;			
		}		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.frag_addactivity_upper1, container, false);
		
		final EditText etCollectionTitle = (EditText)view.findViewById(R.id.add_collectionTitle);
		ImageButton btnAddFinalized = (ImageButton)view.findViewById(R.id.add_finalizeBtn);
		
		btnAddFinalized.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				// If has collection name
				if(etCollectionTitle.getText().toString().trim().length() > 0){
					
					// check if collection name already exists in database
					setCollectionName(etCollectionTitle.getText().toString());	
					
				}else{
					
					Toast toast = Toast.makeText(getActivity(), getActivity().getString(R.string.collection_needs_name), Toast.LENGTH_SHORT);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
				}				
			}
		});
		
		return view;		
	}
	
	
	/*
	 * Check if user's selected name for the collection already exists.
	 */
	private void setCollectionName(final String name) {
		
		// AsyncTask to get Cursor
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {	
			
			@Override
			protected Boolean doInBackground(Void... arg0) {
				
				// columns to fetch from Provider (don't need _id column since not using an Android adapter).
				String[] projectionGalleryActivity = {CollectionsTable.COL_NAME};		
				
				// SQL "WHERE"
				String selection = String.format("%s = ?", CollectionsTable.COL_NAME);
				
				// Replacement for WHERE's "?" - eliminates SQL inject attacks (?).
				String[] selectionArgs = {name};
				
				// fetch cursor
				Cursor cursor = getActivity().getContentResolver().query(
						CollectionsContentProvider.COLLECTIONS_URI, 
						projectionGalleryActivity, 
						selection, 
						selectionArgs, null);
					
				// Is there a collection with that name? If yes, cursor.count is greater than zero.
				if(cursor.getCount() > 0){
					return true;
				}else{
					return false;
				}
				
			}

			@Override
			protected void onPostExecute(Boolean nameAlreadyExists) {
				
				if(nameAlreadyExists){
					
					Toast toast = Toast.makeText(getActivity(), getActivity().getString(R.string.collection_name_exists), Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					
				}else{
					
					// user's selected name is good, save it.
					((AddActivity)getActivity()).setCollectionTitle(name);
					
					listener.upperFragmentListener(F1_TAG);
				}
			}			
		};
		task.execute();		
	}	

}

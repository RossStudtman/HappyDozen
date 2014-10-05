package rocks.happydozen.fragments;

import rocks.happydozen.R;
import rocks.happydozen.activities.AddActivity;
import rocks.happydozen.interfaces.UpperFragmentsListener;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * <h1>Displays at the top of AddActivity</h1>
 * <p>Displays:</p>
 * <ul>
 * 		<li>TextView - collection title</li>
 * 		<li>Button - for adding an image
 * 			<ul>
 * 				<li>Invoke Activity's onClick listener.
 * 					<ul><li>Creates an AddImageFragment in the Activity</li></ul>
 * 				</li>
 * 			</ul>
 * 		</li>
 * 		<li>TextView - display @string/or</li>
 * 		<li>Button - save all chosen images
 * 			<ul><li>Invoke Activity's onClick listener
 * 				<ul><li>Saves collection to database</li></ul>
 * 			</li></ul>
 * 		</li>
 * 
 * 		 
 * @author Ross Studtman
 *
 */
public class AddActivity_Frag3 extends Fragment {
	
	/**
	 * Tag for this fragment
	 */
	public static final String F3_TAG = "AddActivity_Frag3 Tag";
	
	private String collectionTitle;
	
	/**
	 * Button for adding images to collection
	 */
	Button btnAddImage;
	
	public static AddActivity_Frag3 newInstance(String title){
		AddActivity_Frag3 f = new AddActivity_Frag3();
		
		Bundle b = f.getArguments();
		
		if(b == null){
			
			b = new Bundle();
		}
		
		b.putString("title", title);
		f.setArguments(b);
		
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		collectionTitle = getArguments().getString("title");
		
		View view = inflater.inflate(R.layout.frag_addactivity_upper3, container, false);
		
		TextView tvCollectionTitle = (TextView)view.findViewById(R.id.add_finalizedName); 		
		btnAddImage = (Button)view.findViewById(R.id.add_image_halfButton);
		Button btnSaveAll = (Button)view.findViewById(R.id.add_saveAll);
		
		tvCollectionTitle.setText("Collection: " +collectionTitle);
		
		btnAddImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				// Send this click to the activity
				((AddActivity)getActivity()).onClick(view);				
			}
		});
		
		btnSaveAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				// Send this click to the activity
				((AddActivity)getActivity()).onClick(view);				
			}
		});
		return view;
	}
	
	
	
	public void disableAddImageButton(){
		btnAddImage.setVisibility(View.INVISIBLE);		
		// alternative: btnAddImage.setEnabled(false);
	}
}

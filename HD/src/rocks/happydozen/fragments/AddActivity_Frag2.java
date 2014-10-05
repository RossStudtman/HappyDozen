package rocks.happydozen.fragments;


import rocks.happydozen.R;
import rocks.happydozen.activities.AddActivity;
import rocks.happydozen.interfaces.UpperFragmentsListener;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
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
 * 		<li>TextView - instructions</li>
 * 		<li>Button - for adding an image</li>
 * 		<ul>
 * 		<li>Invoke Activity's onClick listener.</li>
 * 			<ul><li>Creates an AddImageFragment in the Activity</li></ul>
 * 		<li>Inform Activity to replace this fragment
 * 			with AddActivity_Frag3.</li></ul>
 * </ul> 
 * 
 * @author Ross Studtman
 *
 */
public class AddActivity_Frag2 extends Fragment {
	
	/**
	 * Tag for this fragment
	 */
	public static final String F2_TAG = "AddActivity_Frag2 Tag";
	
	// Interface listener
	UpperFragmentsListener listener;
	
	private String collectionTitle;
	
	Button btnAddImage;
	
	/**
	 * Make a new instance of AddActivity_Frag2
	 * @param title
	 * @return
	 */	
	public static AddActivity_Frag2 newInstance(String title){
		AddActivity_Frag2 f = new AddActivity_Frag2();
		
		Bundle b = f.getArguments();
		
		if(b == null){
			
			b = new Bundle();
		}
		
		b.putString("title", title);
		f.setArguments(b);
		
		return f;
	}
	
	
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
		
		collectionTitle = getArguments().getString("title");
		
		View view = inflater.inflate(R.layout.frag_addactivity_upper2, container, false);
		
		TextView tvCollectionTitle = (TextView)view.findViewById(R.id.add_finalizedName);
		btnAddImage = (Button)view.findViewById(R.id.add_imageButton);
		
		tvCollectionTitle.setText("Collection: " + collectionTitle);
		
		btnAddImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				// Send this click to the activity
				((AddActivity)getActivity()).onClick(view);	
				
				// Inform Activity it can swap this frag for UpperFragment3
				listener.upperFragmentListener(F2_TAG);				
			}
		});		
		
		return view;
	}


	public void disableAddImageButton(){
		// Hide this button
		btnAddImage.setVisibility(View.INVISIBLE);
	}
	
	
	
	
	


}

package rocks.happydozen.fragments;

import android.app.Activity;
import android.app.ListFragment;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

/**
 * <h1>ListViewFragment is a simple sublcass of ListFragment</h1>
 * 
 * 
 * This fragment is used:
 * <ul>
 * 		<li>AddActivity.java</li>
 * 		<li>EditActivity.java - uses the ListFragListener interface</li>
 * </ul>
 */
public class ListViewFragment extends ListFragment {
	// logging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "ListViewFragment: ";
	
	/**
	 * ListFragListener
	 */
	ListFragListener listener;
	
	/**
	 * ListViewFragment.ListFragListener is a listener interface.
	 * 
	 * @author Ross Studtman	 *
	 */
	public interface ListFragListener{
		public void listFragListener(Cursor cursor);
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		
		setEmptyText("No images to display.");
		super.onActivityCreated(savedInstanceState);
	}
	
	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		// Retain this fragment across configuration change
		setRetainInstance(true);
		
		super.onCreate(savedInstanceState);
	}



	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		// Set listener
		if(activity instanceof ListFragListener){
			
			listener = (ListFragListener)activity;		
			
		}else{
			
			//Log.d(TAG, SCOPE + "Instantiating activity does not implement ListFragListener.");
		}
	}

	@Override
	public void onListItemClick(ListView listView, View v, int position, long id) {
		
		// some Activities that use this fragment may not implement the interface.
		if(listener != null){
			
			Cursor cursor = (Cursor)listView.getItemAtPosition(position);
			listener.listFragListener(cursor);			
		}
	}	
}

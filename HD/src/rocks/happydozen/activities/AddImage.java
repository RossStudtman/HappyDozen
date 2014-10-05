package rocks.happydozen.activities;

import rocks.happydozen.R;
import rocks.happydozen.R.id;
import rocks.happydozen.R.layout;
import rocks.happydozen.R.menu;
import rocks.happydozen.fragments.AddImageFragment;
import rocks.happydozen.utility.ImageHandler;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

/**
 * <h1>An activity for allowing the user to add a single image to a collection.</h1>
 * <p>AddImage activity is basically a wrapper for AddImageFragment.</p>
 * 
 * <p>AddImage activity is created by AddActivity and EditActivity via 
 * <code>startActivityForResult</code> but is the fragment, AddImageFragment, that
 * uses <code>setReturn</code> for sending back a result to AddActivity or
 * EditActivity.</p>
 * 
 * @author Ross Studtman
 *
 */
public class AddImage extends ActionBarActivity {
	
	/**
	 * For handling bitmap creation
	 */
	ImageHandler imageHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_image);
		
		// Create ImageHandler that holds LruCache
		imageHandler = new ImageHandler(this, getFragmentManager());		

		if (savedInstanceState == null) {
			
			AddImageFragment addImageFrag = new AddImageFragment();
			
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.replace(R.id.container, addImageFrag, AddImageFragment.ADD_IMAGE_TAG );
			ft.commit();		
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_image, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public ImageHandler getImageHandler(){
		return imageHandler;
	}
}

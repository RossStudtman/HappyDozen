package rocks.happydozen.adapters;

import rocks.happydozen.R;
import rocks.happydozen.activities.EditActivity;
import rocks.happydozen.database.CollectionsTable;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * EditCursorAdapter is a sublcassed CursorAdapter for EditActivity.
 * This Adapter's task is to set the views populating a row of the List
 * Fragment in the EditActivity.
 * @author MalSolo
 *
 */
public class EditCursorAdapter extends CursorAdapter {
	
	// logging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "EditCursorAdapter: ";
	
	// TESTING how many times bindView is called:
	//static int count = 1;
	
	LayoutInflater inflater;
	
	public static Bitmap defaultBitmap;
	
	public EditCursorAdapter(Context context, Cursor c){
		super(context, c, 0);
		
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);		
	}
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View v = inflater.inflate(R.layout.frag_listview_row, null);
		return v;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		
		// get handles for views in xml
		ImageView imageView = (ImageView)view.findViewById(R.id.add_lvrow_image);
		TextView titleView = (TextView)view.findViewById(R.id.add_lvrow_title);
		
		// get data from cursor and "massage" if necessary
		String imageUri = cursor.getString(cursor.getColumnIndex(CollectionsTable.COL_IMAGEURI));
		String title = cursor.getString(cursor.getColumnIndex(CollectionsTable.COL_TITLE));
		
		int XML_WIDTH = 100;
		int XML_HEIGHT = 100;		

		// TESTING: how many times bindView is called:
		//Log.d(TAG, SCOPE + "bindView called: " +count);
		//count++;
		
		// Use parent activity's ImageHandler
		((EditActivity)context).getImageHandler().loadBitmap(imageUri, imageView, XML_WIDTH, XML_HEIGHT);
		
		// Set title
		titleView.setText(title);		
	}
}

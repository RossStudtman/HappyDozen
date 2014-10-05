package rocks.happydozen.adapters;

import java.util.List;

import rocks.happydozen.R;
import rocks.happydozen.activities.AddActivity;
import rocks.happydozen.activities.AddActivity.ImageBean;
import rocks.happydozen.utility.Constants;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * <h1>AddCollecionAdapter puts image data in AddActivity's ListView.</h1>
 * 
 * <p>Takes a collection of AddActivity.ImageBean and sets the instance fields
 * of the ImageBean into frag_listview_row.xml.</p>
 * 
 * <p>Holder Pattern is used.</p>
 * 
 * @author Ross Studtman
 *
 */
public class AddCollectionAdapter extends BaseAdapter {
	
	// data collection
	List<ImageBean> beanList;
	
	// layout inflator
	private LayoutInflater inflater;
	
	// context
	Context context;
	
	/**
	 * 
	 * @param context is the Activity.
	 * @param beanList is a collection of image Beans.
	 */
	public AddCollectionAdapter(Context context, List<ImageBean> beanList){
		this.context = context;
		this.beanList = beanList;
		inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return beanList.size();
	}

	@Override
	public Object getItem(int position) {
		return beanList.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// collection not from database nor is going directly to database; this is useless.
		return 0;
	}
	
	// holder pattern
	private class ViewHolder{
		ImageView imageView;
		TextView titleView;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		View xmlTemplate = convertView;
		
		if(xmlTemplate == null){
			
			//inflate xml
			xmlTemplate = inflater.inflate(R.layout.frag_listview_row, null);
			
			// initilaize ViewHolder
			holder = new ViewHolder();
			
			// get views that are inside the xml
			holder.imageView = (ImageView)xmlTemplate.findViewById(R.id.add_lvrow_image);
			holder.titleView = (TextView)xmlTemplate.findViewById(R.id.add_lvrow_title);
			
			// set tag
			xmlTemplate.setTag(holder);
			
		}else{
			
			holder = (ViewHolder)xmlTemplate.getTag();
		}
		
		// Get image details from List<ImageBean>
		ImageBean bean = beanList.get(position);		
		String imageUri = bean.getImageUri();
		String title = bean.getImageTitle();
		
		// Set Holder ImageView bitmap; Use parent activity's ImageHandler to load image into Holder's ImageView.
		((AddActivity)context).getImageHandler().loadBitmap(imageUri, holder.imageView, Constants.LISTVIEW_XML_WIDTH, Constants.LISTVIEW_XML_HEIGHT);		
		
		// Set Holder's TextView.
		holder.titleView.setText(title);
		
		// return view
		return xmlTemplate;
	}
}

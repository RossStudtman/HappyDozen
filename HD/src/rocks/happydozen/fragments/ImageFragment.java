package rocks.happydozen.fragments;

import rocks.happydozen.R;
import rocks.happydozen.activities.GalleryActivity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * <h1>ImageFragment displays a single image.</h1>
 * 
 * <p>These fragments are displayed via GalleryActivity's ViewPager.</p>
 * 
 * 
 * @author Ross Studtman
 *
 */
public class ImageFragment extends Fragment {
	// logging
	public static final String TAG = "ROSS";
	public static final String SCOPE = "ImageFragment: ";
	/**
	 *  bundle key
	 */
	private static final String IMAGE_URI_KEY = "uri";
	private static final String TITLE_KEY = "title";
	/**
	 * View handle
	 */
	ImageView imageView;
	TextView tvTitle;
	/**
	 * Image field.
	 */
	String imageUri;
	String title;

	/**
	 * ViewTree for obtaining ImageView dimensions.
	 */
	ViewTreeObserver vto;

	/**
	 * Constructs a ImageFragment
	 * @param imageUri
	 * @return a ImageFragment
	 */
	public static ImageFragment newInstance(String imageUri, String title) {
		
		ImageFragment imageFragment = new ImageFragment();

		Bundle bundle = imageFragment.getArguments();

		if (bundle == null) {
			bundle = new Bundle();
		}

		bundle.putString(IMAGE_URI_KEY, imageUri);
		bundle.putString(TITLE_KEY, title);

		imageFragment.setArguments(bundle);
		
		return imageFragment;
	}

	/**
	 * Empty constructor, required per Fragment docs.
	 * http://developer.android.com
	 * /training/displaying-bitmaps/display-bitmap.html
	 */
	public ImageFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {


		// inflate XML
		View view = inflater.inflate(R.layout.frag_gallery_image, container, false);

		// get bundle args
		imageUri = getArguments().getString(IMAGE_URI_KEY);
		title = getArguments().getString(TITLE_KEY);
		
		tvTitle = (TextView)view.findViewById(R.id.image_title);
		
		tvTitle.setText(title);

		// get ImageView from XML
		imageView = (ImageView) view.findViewById(R.id.gallery_image);
		
		/*
		 * Obtain ImageView dimensions:
		 * 		Get ViewTreeObserver.
		 * 		Add pre-draw listener.
		 * 		Discover dimensions of ImageView.
		 * 		Load image into ImageView.
		 * 		Remove pre-draw listener.
		 */
		ViewTreeObserver vto = imageView.getViewTreeObserver();
		
		//
		vto.addOnPreDrawListener(new OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				
				int viewWidth = imageView.getMeasuredWidth();
				int viewHeight = imageView.getMeasuredHeight();
				// Log.d(TAG, SCOPE +"onPreDraw viewWidth: " +viewWidth
				// +", viewHeight: " +viewHeight);

				// GalleryActivity has an ImageHandler, use that to load bitmaps and take advantage of the LruCache.
				// Each of these ImageFragments can house their images in the GalleryActivity's ImageHandler.
				((GalleryActivity)getActivity()).getImageHandler().loadBitmap(imageUri, imageView, viewWidth, viewHeight);

				// Remove listener.
				imageView.getViewTreeObserver().removeOnPreDrawListener(this);

				return true;
			}
		});

		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();	
	}
}



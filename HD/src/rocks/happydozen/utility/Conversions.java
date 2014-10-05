package rocks.happydozen.utility;

import android.content.Context;

public class Conversions {

	/**
	 * Convert desired DP into equivalent pixels.
	 * @param context is hook into application.
	 * @param dp is the dp value to convert into pixels.
	 * @return pixel value.
	 */
	public static int calculatePixelsFromDP(Context context, int dp){
		
		// An (int)cast truncates decimals, this rounds-up decimals before the cast.
		float ROUND_UP = 0.5f;
		
		// Get the Android device's screen density scale.
		float scale = context.getResources().getDisplayMetrics().density;
				
		// Convert dp to pixels, based on density scale.
		int pixels = (int)(dp * scale + ROUND_UP);
		
		return pixels;
	}
}

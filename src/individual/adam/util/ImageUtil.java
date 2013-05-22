package individual.adam.util;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * @author apple
 *
 */
public class ImageUtil {
	
	
	public Bitmap cutScreenshot(Bitmap originalBitmap, int startX, int startY,  int width,int height){
        Bitmap bitmap=Bitmap.createBitmap(originalBitmap, startX, startY, width, height);
        originalBitmap.recycle();
        return bitmap;
	}
	
    // return value is just a offset value, the real value is y-returnValue+1
    public int getTopOffset(Bitmap bitmap, int x, int y, int width){
    	int tempWidth = width;
    	/*int startX = x-tempWidth;
    	if(startX < 0){
    		startX = 0;
    	}*/
    	int count = -1;
    	int topOffset = -1;
    	//get the black count of every line until the count is 0; 
    	for(int i = 0;;i++){
    		if(y-i >= 0){
    			count = countBlackDot(bitmap, x, y-i, tempWidth, true);
    			Log.e("adam1", "getTopOffset count: "+count);
    		}
    		
    		if(count==0 ||y-i == 0 ){
    			topOffset = i;
    			break;
    		}
    	}
    	return topOffset;
    }
    public int getBottomOffset(Bitmap bitmap, int x, int y, int width){
    	
    	int tempWidth = width;
    	int bitmapHeight = bitmap.getHeight();
    	Log.e("adam1", "getBottomOffset Width: "+width);
    	/*int startX = x-tempWidth;
    	if(startX < 0){
    		startX = 0;
    	}*/
    	int count = -1;
    	int bottomOffset = -1;
    	//get the black count of every line until the count is 0; 
    	for(int i = 0;;i++){
    		if(y+i < bitmapHeight){
    			count = countBlackDot(bitmap, x, y+i, tempWidth, true);
    			Log.e("adam1", "getBottomOffset count: "+count);
    		}
    		
    		if(count==0 ||y+i == bitmapHeight ){
    			bottomOffset = i;
    			Log.e("adam1", "count:"+bottomOffset);
    			break;
    		}
    	}
    	return bottomOffset;
    }
	
    private int countBlackDot(Bitmap bitmap, int x, int y, int width, boolean direction){
    	Log.e("adam", "countBlackDot");
    	int count = 0;
    	if(direction){
    		for(int i=0;i<width;i++){
        		if((x+i<bitmap.getWidth())&&bitmap.getPixel(x+i, y)<= -16777000){
        			++count;
        		}
        	}
    	}else{
    		for(int i=0;y+i<bitmap.getHeight();i++){
        		if(bitmap.getPixel(x, y+i)<= -16777000){
        			++count;
        		}
        	}
    	}
    	
    	return count;
    }
	
	/**
	 * binarize bitmap.
	 * @param final_bitmap
	 * @return
	 */
    public  Bitmap binarizeBitmap(Bitmap bitmap){
    	int height = bitmap.getHeight();
    	  int width = bitmap.getWidth();
    	  int[] pix = new int[width * height];
    	  bitmap.getPixels(pix, 0, width, 0, 0, width, height);
    	  int R, G, B;
    	  for (int y = 0; y < height; y++)
    	   for (int x = 0; x < width; x++) {
    	    int index = y * width + x;
    	    int r = (pix[index] >> 16) & 0xff;
    	    int g = (pix[index] >> 8) & 0xff;
    	    int b = pix[index] & 0xff;
    	    // binarize
    	    if((r>127)&&(g>127)&&(g>127)){
    	    	R = 255;
    	    	G = 255;
    	    	B = 255;
    	    }else{
    	    	R = 0;
    	    	G = 0;
    	    	B = 0;
    	    }
    	    pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;
    	   }
    	  Bitmap tempBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    	  tempBitmap.setPixels(pix, 0, width, 0, 0, width, height);
    	  bitmap.recycle();
    	  return tempBitmap;
    }
}

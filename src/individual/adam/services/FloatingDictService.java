package individual.adam.services;

import individual.adam.R;
import individual.adam.exception.BaseExecption;
import individual.adam.util.ImageUtil;
import individual.adam.util.TranslatorHelper;
import individual.adam.util.WordItem;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import pl.polidea.asl.IScreenshotProvider;
import pl.polidea.asl.ScreenshotService;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.googlecode.leptonica.android.Pix;
import com.googlecode.leptonica.android.ReadFile;
import com.googlecode.tesseract.android.TessBaseAPI;

public class FloatingDictService extends Service {
	
	private ViewFlipper flipper;
	private LinearLayout barLayout;
	private WindowManager.LayoutParams params = new WindowManager.LayoutParams();
	private View view;
	private WindowManager wm;
	private ImageView miniImage;
	private int width;
	private int height;
	private EditText keyEditText;
	private ImageView translateView;
	private TextView resultTextView;
	private ImageView moveImageView;
	private SurfaceView surfaceView;
	private ImageView showSurfaceImage;
	private float startX;
	private float startY;
	private long timeInBetween;
	private long actionDownTime;
	private Handler handler = new Handler();
	private String pron;
	private String translation;
	private String keyWord;
	//used for surfaceView
	private float startPointX = -1;
	private float startPointY = -1;
	private float endPointX = -1;
	private float endPointY = -1;
	private float oldX;
	private float oldY;
	//private Bitmap screenShot;
	private SurfaceHolder sfh;
	private Canvas canvas;
	private Paint mPaint;
	
	private ImageUtil imageUtil;
	
	private static final String BITMAP_PATH="/sdcard/screens/binarizedBitmap.png";
	private static final String CUTTED_BITMAP_PATH="/sdcard/screens/cuttedScreenshot.jpg";
	
	private FutureTask<Bitmap> futureTask;
	//asl screenshot for this application
	private IScreenshotProvider aslProvider = null;
	private ServiceConnection aslServiceConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			aslProvider = IScreenshotProvider.Stub.asInterface(service);
		}
	};
	
	/**OCR engine*/
	private TessBaseAPI api;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate() {
		Log.e("adam: ", "onCreate");
		// connect to ASL service
		Intent intent = new Intent();
		intent.setClass(this, ScreenshotService.class);
		// intent.addCategory(Intent.ACTION_DEFAULT);
		bindService(intent, aslServiceConn, Context.BIND_AUTO_CREATE);
		
		wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
		initParams();
		view = populateLayoutFromXml(R.layout.floatingdict);
		
		findAllView(view);
		//view.setBackgroundDrawable(new BitmapDrawable());
		view.getBackground().setAlpha(0);
		//below transpareny has wasted me 4 hours, fuck!;
		//flipper.setBackgroundDrawable(new BitmapDrawable());
		flipper.getBackground().setAlpha(0);
		resultTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
		resultTextView.setMaxWidth(barLayout.getMeasuredWidth());
		miniImage.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec.makeMeasureSpec(0,
				MeasureSpec.UNSPECIFIED));
		Log.e("adam", "miniImage: " + miniImage.getMeasuredWidth());
		Log.e("adam", "miniImage: " + miniImage.getMeasuredHeight());
		params.width = miniImage.getMeasuredWidth();
		params.height = miniImage.getMeasuredHeight();
		//adam test
		Log.e("adam: ", "isShow:" + view.isShown());
		//OCR 
		api = new TessBaseAPI();
		api.init("/mnt/sdcard/translatordata/", "eng");
		//image Util
		imageUtil = new ImageUtil();
		
		miniImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (timeInBetween < 200) {
					barLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec
							.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
					resultTextView.setWidth(barLayout.getMeasuredWidth());
					resultTextView.setHeight(80);
					//meizu
					//resultTextView.setHeight(180);
					flipper.showNext();
					params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;// |
																		// LayoutParams.FLAG_NOT_FOCUSABLE;
					if (width == 0) {
						barLayout.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec
								.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
						width = barLayout.getMeasuredWidth();
						height = barLayout.getMeasuredHeight();
					}
					params.width = width;
					params.height = height;
					resultTextView.postInvalidate();
					wm.updateViewLayout(view, params);
					
				}

			}
		});
		
		moveImageView.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (timeInBetween < 200) {
					flipper.showNext();
					params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
					params.width = miniImage.getWidth();
					params.height = miniImage.getHeight();
					Log.e("adam", "barLayout.width: " + barLayout.getMeasuredWidth());
					Log.e("adam", "barLayout.hight: " + barLayout.getMeasuredHeight());
					Log.e("adam", "barLayoutwidth: " + params.width);
					Log.e("adam", "barLayoutheight: " + params.height);
					wm.updateViewLayout(FloatingDictService.this.view, params);
				}

			}
		});
		
		view.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				return true;
			}
		});
		moveImageView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float x = event.getRawX();
				float y = event.getRawY();

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					actionDownTime = new Date().getTime();
					startX = event.getX();
					Log.e("adam", "startX: " + startX);
					startY = event.getY();
					Log.e("adam", "startY: " + startY);
					break;
				case MotionEvent.ACTION_MOVE:
					params.x = (int) (x - startX - view.getMeasuredWidth() + moveImageView.getWidth());
					params.y = (int) (y - startY - moveImageView.getHeight() / 2);
					//params.x = (int) (startX-x + wm.getDefaultDisplay().getWidth() - moveImageView.getWidth());
					wm.updateViewLayout(view, params);
					Log.e("adam", "params.y" + params.y);
					Log.e("adam", "getMeasuredHeight" + view.getMeasuredHeight());
					break;
				case MotionEvent.ACTION_UP:
					wm.updateViewLayout(view, params);
					startX = startY = 0;
					timeInBetween = new Date().getTime() - actionDownTime;
					break;
				}
				// must return true;
				return false;
			}
		});

		miniImage.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				float x = event.getRawX();
				float y = event.getRawY();
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					actionDownTime = new Date().getTime();
					startX = event.getX();
					Log.e("adam", "startX: " + startX);

					startY = event.getY();
					
					Log.e("adam", "startY: " + startY);
					break;
				case MotionEvent.ACTION_MOVE:
					params.x = (int) (x - startX - view.getMeasuredWidth() + miniImage.getWidth());
					params.y = (int) (y - startY - miniImage.getHeight() / 2);
					//params.x = (int) (startX-x + wm.getDefaultDisplay().getWidth()- miniImage.getWidth());
					//params.y = (int) (y-startY - miniImage.getHeight() / 2);
					wm.updateViewLayout(view, params);
					Log.e("adam", "params.x:" + params.x);
					Log.e("adam", "params.y:" + params.y);
					break;
				case MotionEvent.ACTION_UP:
					startX = startY = 0;
					timeInBetween = new Date().getTime() - actionDownTime;
					break;
				}
				// must return false, else ....
				return false;
			}
		});
		translateView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				keyWord = keyEditText.getText().toString();
				resultTextView.setBackgroundResource(R.drawable.tabgrey);
				//resultTextView.setBackgroundColor(Color.GRAY);
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							translation = null;
							//WordItem item = TranslatorHelper.translator(keyWord);
							WordItem item = TranslatorHelper.queryByStep(keyWord);
							ArrayList<String> translations = item.getTranslations();
							//translation = item.getTranslation();
							Log.e("translateError","error"+item.getErrorMsg());
							Log.e("translateError","translations: "+translations);
							if(translations != null){
								translation="";
								for(String temp : translations){
									if(temp != null)
										translation += (temp +"\n");
								}
							}
							
							pron = "pron: ["+item.getPron()+"]";
							Log.e("adam","translation: "+translation);
							Log.e("adam","pron: "+pron);
							//resultTextView.setText(item.getTranslation());
							handler.post(new Runnable(){
		    					public void run(){
		    						if(translation == null){
		    							resultTextView.setText("Sorry, no result!");
		    							resultTextView.setBackgroundResource(R.drawable.tab);
		    							//resultTextView.setBackgroundColor(Color.WHITE);
		    						}else{
		    							String result = pron+"\n"+translation;
			    						resultTextView.setText(result);
			    						resultTextView.setBackgroundResource(R.drawable.tab);
			    						//resultTextView.setBackgroundColor(Color.WHITE);
			    						resultTextView.postInvalidate();
		    						}
		    						
		    					}
		    				});
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		});
		showSurfaceImage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				flipper.setVisibility(View.GONE);
				//flipper.setVisibility(View.INVISIBLE);
				surfaceView.setVisibility(View.VISIBLE);
				// set transparency
				surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
				//surfaceView.setBackgroundDrawable(new BitmapDrawable());
				surfaceView.getBackground().setAlpha(0);
				Point defaultWindowXY = new Point();
				FloatingDictService.this.wm.getDefaultDisplay().getSize(defaultWindowXY);
				int windowWidth = defaultWindowXY.x;
				int windowHeight = defaultWindowXY.y;
/*				int windowWidth = FloatingDictService.this.wm.getDefaultDisplay().getWidth();  //depreciate
				int windowHeight = FloatingDictService.this.wm.getDefaultDisplay().getHeight();
*/				
				Log.e("adam", "window width:"+windowWidth);
				LayoutParams layoutParams = new LayoutParams();
				layoutParams.width = windowWidth;
				layoutParams.height = windowHeight;
				layoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
				layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
				
				params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL;
				layoutParams.alpha = 90;
				layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
				layoutParams.x = params.x;
				layoutParams.y = params.y;
				
				/*layoutParams.x = (int) flipper.getX();
				layoutParams.y = (int) flipper.getY();*/
				
				//layoutParams.format=PixelFormat.RGBA_8888;
				layoutParams.format=PixelFormat.TRANSLUCENT;
				//surfaceView.setLayoutParams(layoutParams);
				view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec
						.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
				wm.updateViewLayout(view, layoutParams);
				view.getScaleX();
				Log.e("adam", "surfaceViewWidth:"+view.getScaleX());
				Log.e("adam", "surfaceViewX:"+view.getX());
				Log.e("adam", "surfaceViewY:"+view.getY());
				Log.e("adam", "surfaceViewXlayoutParams.X:"+layoutParams.y);
				Log.e("adam", "surfaceViewXlayoutParamsY:"+layoutParams.y);
				surfaceView.postInvalidate();
				
				Log.e("adam", "surfaceViewWidth:"+surfaceView.getWidth());
				Log.e("adam", "surfaceViewHithg:"+surfaceView.getHeight());
				
				//screenShot = getScreenShot();
				Callable<Bitmap> sreenshotCallable = new Callable<Bitmap>() {
					
					@Override
					public Bitmap call() throws Exception {
						Bitmap screenShot = null;
						try {
							Thread.sleep(500);
							screenShot = getScreenShot();
						} catch (Exception e) {
							e.printStackTrace();
							handler.post(new Runnable(){

								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), "Take screenshot failed!", Toast.LENGTH_SHORT).show();
								}
								
							});
							Log.e("adam", "Take screenshot failed!");
							throw new BaseExecption(BaseExecption.USER_DEFINED_ERROR, e.getMessage());
						}
						return screenShot;
					}
				};
				futureTask = new FutureTask<Bitmap>(sreenshotCallable);
				new Thread(futureTask).start();
			}
		});
		
		surfaceView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event){
				Log.e("adam", "enent:"+event.getAction());
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					Log.e("adam", "action_down");
					//screenShot = getScreenShot();
					startPointX = event.getRawX();
					Log.e("adam", "event.getRowX: "+event.getRawX());
					Log.e("adam", "event.getRowY: "+event.getRawY());
					startPointY = event.getRawY();
					oldX = startPointX;
					oldY = event.getY();
					sfh = surfaceView.getHolder();
			        //canvas = sfh.lockCanvas(new Rect(0, 0, surfaceView.getWidth(),surfaceView.getHeight()));
			        mPaint = new Paint();
			        mPaint.setColor(Color.GREEN);
			        mPaint.setStrokeWidth(4);
			        
					break;
				case MotionEvent.ACTION_MOVE:
					Log.e("adam", "action_move");
					float x = event.getRawX();
					//float y = event.getRawY();
					float y = event.getY();
					//canvas = sfh.lockCanvas(new Rect(0, 0, surfaceView.getWidth(),surfaceView.getHeight()));
					
					Point defaultScreenXY = new Point();
					wm.getDefaultDisplay().getSize(defaultScreenXY);
					
					Log.e("adam", "surfaceViewWidth: "+surfaceView.getWidth()+" and surfaceView.getHeight: "+surfaceView.getHeight());
					Log.e("adam", "defaultDisplayWidth: "+defaultScreenXY.x+"defaultDisplayHight: "+defaultScreenXY.y);
					
					canvas = sfh.lockCanvas(new Rect(0, 0, defaultScreenXY.x,defaultScreenXY.y));
					//canvas = sfh.lockCanvas(null);
					canvas.drawLine(oldX, oldY, x, y, mPaint);
					oldX = x;
					oldY = y;
			        sfh.unlockCanvasAndPost(canvas);// unlock canvas
					break;
				case MotionEvent.ACTION_UP:
					Log.e("adam", "ACTION_UP");
					canvas = sfh.lockCanvas(null);
			    	Log.e("adam", "canvas: "+canvas);
			    	canvas.drawColor(Color.TRANSPARENT);
			        sfh.unlockCanvasAndPost(canvas);
					endPointX = event.getRawX();
					endPointY = event.getRawY();
					flipper.setVisibility(View.VISIBLE);
					wm.updateViewLayout(view, params);
					surfaceView.setVisibility(View.INVISIBLE);
					flipper.postInvalidate();
					surfaceView.postInvalidate();
					Log.e("adam", "surfaceViewStartPointX: "+startPointX);
					Log.e("adam", "surfaceViewStartPointY: "+startPointY);
					Log.e("adam", "surfaceViewEndPointX: "+endPointX);
					Log.e("adam", "surfaceViewEndPointY: "+endPointY);
					
					Bitmap screenShot = null;
					try {
						screenShot = futureTask.get();
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					} catch (ExecutionException e1) {
						e1.printStackTrace();
					}
					if(screenShot == null){
						break;
					}
					Log.e("adam", "screenShot is :"+screenShot);
					screenShot = imageUtil.binarizeBitmap(screenShot);
					Log.e("adam", "after binarizeBitmap");
					FileOutputStream fos;
					try {
						fos = new FileOutputStream(BITMAP_PATH);
						Log.e("adam", "after binarizeBitmap");
						screenShot.compress(CompressFormat.PNG, 100, fos);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					
					int bottomOffset = imageUtil.getBottomOffset(screenShot, (int)startPointX, (int)startPointY, (int)(endPointX-startPointX));
					Log.e("adam", "after getBottomOffset");
					int topOffset = imageUtil.getTopOffset(screenShot, (int)startPointX, (int)startPointY, (int)(endPointX-startPointX));
					Log.e("adam", "startPointPixel: x:"+screenShot.getPixel((int)startPointX, (int)startPointY));
					Log.e("adam", "bottomOffset: "+bottomOffset);
					Log.e("adam", "topOffset: "+topOffset);
					if(bottomOffset+topOffset ==0){
						break;
					}
					screenShot = imageUtil.cutScreenshot(screenShot, (int)startPointX, (int)startPointY-topOffset, (int)(endPointX-startPointX), bottomOffset+topOffset);
					
					//output cutted screenshot
					try {
						fos = new FileOutputStream(CUTTED_BITMAP_PATH);
						screenShot.compress(CompressFormat.JPEG, 100, fos);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					
					Pix pix;
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					screenShot.compress(Bitmap.CompressFormat.JPEG, 100, baos);
					byte[] data = baos.toByteArray();
					pix = ReadFile.readMem(data);
					api.setImage(pix);
					String text = api.getUTF8Text();
					Log.e("adam", "OCR text: "+text);
					keyEditText.setText(text);
					screenShot.recycle();
					System.gc();
					SharedPreferences perferences = getSharedPreferences("App_Info", Context.MODE_PRIVATE);
					int appRunTimes = perferences.getInt("RunTimes", 0);
					Log.e("adam2", "appRuntimes:"+appRunTimes);
					if(appRunTimes!=0 && appRunTimes%10 ==0){
						Log.e("adam2", "appRunTimes%10:"+appRunTimes);
						//String[] killService =  {"kill -9 `ps aux|grep asl-native |awk '{print $1}'`"};
						//String[] rRebootService =  {"/data/local/asl-native /data/local/asl-native.log"};
						/*try {
							runSu(killService);
							runSu(rRebootService);
						} catch (BaseExecption e) {
							e.printStackTrace();
						}*/
						/*new Thread(new Runnable(){

							@Override
							public void run() {
								try {
									try {
										Thread.sleep(2000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									runSu(rRebootService);
								} catch (BaseExecption e) {
									e.printStackTrace();
								}
							}
							
						}).start();*/
						
					}
					Editor edit = perferences.edit();
					edit.putInt("RunTimes", ++appRunTimes);
					edit.commit();
					
					break;
				}
				
				return true;
			}
		});
		//wm.addView(view, params);
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		wm.addView(view, params);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void initParams() {
		params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
		params.type = WindowManager.LayoutParams.TYPE_PHONE;
		params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
		params.width = WindowManager.LayoutParams.WRAP_CONTENT;
		params.height = WindowManager.LayoutParams.WRAP_CONTENT;
		params.alpha = 80;
		params.gravity = Gravity.RIGHT | Gravity.TOP;
		params.gravity = Gravity.LEFT | Gravity.TOP;
		params.x = 100;
		params.y = 100;
		params.format=PixelFormat.RGBA_8888;
	}

	public View populateLayoutFromXml(int id) {
		LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = (View) inflater.inflate(id, null);
		return view;
	}

	private void findAllView(View view) {
		flipper = (ViewFlipper) view.findViewById(R.id.flipper);
		translateView = (ImageView) view.findViewById(R.id.translateBtn);
		resultTextView = (TextView) view.findViewById(R.id.result);
		miniImage = (ImageView) view.findViewById(R.id.miniImage);
		moveImageView = (ImageView) view.findViewById(R.id.moveImage);
		barLayout = (LinearLayout) view.findViewById(R.id.resultBar);
		keyEditText = (EditText) view.findViewById(R.id.key);
		surfaceView = (SurfaceView) view.findViewById(R.id.surfaceView);
		showSurfaceImage = (ImageView) view.findViewById(R.id.showSurfaceImage);
	}
	
	private Bitmap getScreenShot() throws BaseExecption{
		Bitmap screenShot = null;
		try {
			if (aslProvider == null){
				throw new BaseExecption(BaseExecption.USER_DEFINED_ERROR, "Screenshot service is not up!");
			}
			else if (!aslProvider.isAvailable()){
				throw new BaseExecption(BaseExecption.USER_DEFINED_ERROR, "Screenshot service is not up!");
			}
			else {
				String file = aslProvider.takeScreenshot();
				if (file != null){
					screenShot = BitmapFactory.decodeFile(file);
				}
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
			throw new BaseExecption(BaseExecption.USER_DEFINED_ERROR, "File Not Found!");
		} catch (RemoteException e) {
			e.printStackTrace();
			throw new BaseExecption(BaseExecption.USER_DEFINED_ERROR, "Screenshot service is not up!");
		}
		return screenShot;
	}
	
	@Override
	public void onDestroy() {
		wm.removeView(view);
		unbindService(aslServiceConn);
		super.onDestroy();
	}
}

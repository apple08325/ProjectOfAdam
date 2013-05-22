package individual.adam.activity;
import individual.adam.R;
import individual.adam.exception.BaseExecption;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class FloatingDictActivity extends Activity {
	private Intent intent;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Button showTopView = (Button)findViewById(R.id.showFloatingDict);
		intent = new Intent("individual.adam.services.FloatingDictService");
		try {
			putOCRDataToSD();
		} catch (IOException e1) {
			e1.printStackTrace();
			Toast.makeText(this, "OCR data not installed", Toast.LENGTH_SHORT);
		}
		//asl configration
		//individual.adam
		SharedPreferences perferences = getSharedPreferences("App_Info", Context.MODE_PRIVATE);
		int appRunTimes = perferences.getInt("RunTimes", 0);
		Log.e("adam", "appRuntimes: "+appRunTimes);
		if(appRunTimes == 0){
			//install asl service
			try {
				runSu(new String[] {
						"busybox unzip -o /data/app/individual.adam* *asl-native -d /data/local/tmp",
						//"gzip -dv /data/app/individual.adam* *asl-native -d /data/local/tmp",//gzip /data/app/individual.adam* /data/local/tmp/individual
						"mv /data/local/tmp/assets/asl-native /data/local/asl-native",
						"chmod 0777 /data/local/asl-native",
						"/data/local/asl-native /data/local/asl-native.log" });
			} catch (BaseExecption e) {
				Toast.makeText(this, "please root this phone!",  Toast.LENGTH_SHORT);
			}
		}else{
			Editor edit = perferences.edit();
			edit.putInt("RunTimes", ++appRunTimes);
			edit.commit();
		}
		
		showTopView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences perferences = getSharedPreferences("Dict_Is_Show", Context.MODE_PRIVATE);
				Boolean dictIsShow = perferences.getBoolean("DictIsShow", false);
				Log.e("adam", "dictIsShow:"+dictIsShow);
				if(dictIsShow == false){
					startService(intent);
					Editor edit = perferences.edit();
					edit.putBoolean("DictIsShow", true);
					edit.commit();
					Log.e("adam: ", "showFloatingDict");
				}else{
					stopService(intent);
					Editor edit = perferences.edit();
					edit.putBoolean("DictIsShow", false);
					edit.commit();
					Log.e("adam", "removeFloatingDict");
				}
			}
		});
	}
	private void runSu(String cmd[]) throws BaseExecption {
		String cmdBecomeSu = "su";

		ProcessBuilder builder = new ProcessBuilder(cmdBecomeSu);

		builder.redirectErrorStream(true);
		Process runsum = null;
		try {
			runsum = builder.start();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			int exitVal = 0;
			final OutputStreamWriter out = new OutputStreamWriter(runsum
					.getOutputStream());
			for (int i = 0; i < cmd.length; i++) {
				out.write(cmd[i]);
				out.write("\n");
				out.flush();
				Log.e("adam", "cmd complete!" + i);
			}
			//exitVal = runsum.waitFor();
			Log.e("adam", "exitVal: " + exitVal);
			if (exitVal == 0) {
				Log.e("Debug", "Successfully to su");
			}
		} catch (Exception e) {
			Log.e("Debug", "Failed to su");
			e.printStackTrace();
			throw new BaseExecption(BaseExecption.SYSTEM_ERROR,"Failed to su.");
		}/*finally{
			if(runsum != null)runsum.destroy(); 
		}*/

	}
	private void putOCRDataToSD() throws IOException{
		String path = "/sdcard/translatordata/tessdata";
		String name = "eng.traineddata";
		File rawData = new File(path+"/"+name);
		if(rawData.exists())return;
		File fileDir = new File(path);
		if(!fileDir.exists()){
			boolean dirCreated = fileDir.mkdirs();
			if(dirCreated){
				byte[] buf = new byte[1024];
				InputStream is =  getResources().openRawResource(R.raw.eng);
				OutputStream os = new BufferedOutputStream(new FileOutputStream(path+"/"+name));
				int readLen = 0;
				while ((readLen = is.read(buf, 0, 1024)) != -1) {
					os.write(buf, 0, readLen);
				}
				is.close();
				os.close();
			}
		}
		
		
		
	}
}
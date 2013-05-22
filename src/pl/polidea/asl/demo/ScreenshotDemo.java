package pl.polidea.asl.demo;

import individual.adam.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import pl.polidea.asl.IScreenshotProvider;
import pl.polidea.asl.ScreenshotService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class ScreenshotDemo extends Activity {

	/*
	 * The ImageView used to display taken screenshots.
	 */
	private ImageView imgScreen;

	private ServiceConnection aslServiceConn = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			aslProvider = IScreenshotProvider.Stub.asInterface(service);
		}
	};
	private IScreenshotProvider aslProvider = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		/*
		 * runSu("unzip -o /data/app/pl.polidea.asl.demo* *asl-native -d /data/local/tmp"
		 * );
		 * runSu("mv /data/local/tmp/assets/asl-native /data/local/asl-native");
		 * runSu("chmod 0777 /data/local/asl-native");
		 */
		runSu(new String[] {
				"unzip -o /data/app/pl.polidea.asl.demo* *asl-native -d /data/local/tmp",
				"mv /data/local/tmp/assets/asl-native /data/local/asl-native",
				"chmod 0777 /data/local/asl-native",
				"/data/local/asl-native /data/local/asl-native.log" });

		Log.e("adam", "unzip");
		imgScreen = (ImageView) findViewById(R.id.showSurfaceImage);
		Button btn = (Button) findViewById(R.id.showSurfaceImage);
		btn.setOnClickListener(btnTakeScreenshot_onClick);

		// connect to ASL service
		// Intent intent = new Intent(ScreenshotService.class.getName());
		Intent intent = new Intent();
		intent.setClass(this, ScreenshotService.class);
		// intent.addCategory(Intent.ACTION_DEFAULT);
		bindService(intent, aslServiceConn, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onDestroy() {
		unbindService(aslServiceConn);
		super.onDestroy();
	}

	private View.OnClickListener btnTakeScreenshot_onClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				if (aslProvider == null)
					Toast.makeText(ScreenshotDemo.this, R.string.app_name,
							Toast.LENGTH_SHORT).show();
				else if (!aslProvider.isAvailable())
					Toast.makeText(ScreenshotDemo.this, R.string.app_name,
							Toast.LENGTH_SHORT).show();
				else {
					String file = aslProvider.takeScreenshot();
					if (file == null)
						Toast.makeText(ScreenshotDemo.this,
								R.string.app_name, Toast.LENGTH_SHORT)
								.show();
					else {
						Toast.makeText(ScreenshotDemo.this,
								R.string.app_name, Toast.LENGTH_SHORT)
								.show();
						Bitmap screen = BitmapFactory.decodeFile(file);
						imgScreen.setImageBitmap(screen);

					}
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (RemoteException e) {
				// squelch
			}

		}
	};

	public void execCommand(String command) throws IOException {

		// start the ls command running
		// String[] args = new String[]{"sh", "-c", command};
		Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec(command); // 这句话就是shell与高级语言间的调用
		// Process proc = runtime.exec(args); //如果有参数的话可以用另外一个被重载的exec方法
		// runtime.
		// 实际上这样执行时启动了一个子进程,它没有父进程的控制台
		// 也就看不到输出,所以我们需要用输出流来得到shell执行后的输出
		InputStream inputstream = proc.getInputStream();
		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
		// read the ls output

		String line = "";
		StringBuilder sb = new StringBuilder(line);
		while ((line = bufferedreader.readLine()) != null) {
			// System.out.println(line);
			sb.append(line);
			sb.append('\n');
		}
		Log.e("adam", "exec return: " + sb);
		// 使用exec执行不会等执行成功以后才返回,它会立即返回
		// 所以在某些情况下是很要命的(比如复制文件的时候)
		// 使用wairFor()可以等待命令执行完成以后才返回
		try {
			if (proc.waitFor() != 0) {
				Log.e("adam", "exception3");
				Log.e("adam", "exit value = " + proc.exitValue());
				System.err.println("exit value = " + proc.exitValue());
			}
		} catch (InterruptedException e) {
			Log.e("adam", "exception2");
			System.err.println(e);
		}
	}

	public String execute(String[] cmmand, String directory) throws IOException {
		String result = "";
		try {
			ProcessBuilder builder = new ProcessBuilder(cmmand);

			if (directory != null)
				builder.directory(new File(directory));
			builder.redirectErrorStream(true);
			Process process = builder.start();

			// 得到命令执行后的结果
			InputStream is = process.getInputStream();
			byte[] buffer = new byte[1024];
			while (is.read(buffer) != -1) {
				result = result + new String(buffer);
				Log.e("adam1", "shell result: " + result);
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	public void runSu(String cmd[]) {
		Runtime ex = Runtime.getRuntime();
		String cmdBecomeSu = "su";

		ProcessBuilder builder = new ProcessBuilder(cmdBecomeSu);

		builder.redirectErrorStream(true);
		Process runsum = null;
		try {
			runsum = builder.start();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		/*
		 * //得到命令执行后的结果 InputStream is = runsum.getInputStream ( ) ;
		 */
		try {
			// java.lang.Process runsum = ex.exec(cmdBecomeSu);
			int exitVal = 0;
			// execute(new String[]{"sh","-c","ls -l /data/local/"},null);
			final OutputStreamWriter out = new OutputStreamWriter(runsum
					.getOutputStream());
			for (int i = 0; i < cmd.length; i++) {
				out.write(cmd[i]);
				out.write("\n");
				out.flush();
				Log.e("adam", "cmd complete!" + i);
			}

			Log.e("adam", "cmd complete!");
			/*
			 * out.write("echo -e '\003'"); out.write("\n"); out.flush();
			 */
			// runsum.destroy();
			Log.e("adam", "destroy");
			// exitVal = runsum.waitFor();
			Log.e("adam", "exitVal: " + exitVal);
			if (exitVal == 0) {
				Log.e("Debug", "Successfully to su");
			}
		} catch (Exception e) {
			Log.e("Debug", "Fails to su");
		}

	}
}
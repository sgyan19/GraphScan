package com.example.sun.graphscan;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.sun.bitmap.PlanarYUVLuminanceSource;

import java.io.File;
import java.io.FileOutputStream;

final class DecodeHandler extends Handler {
	private final static String TAG = "DecodeHandler";

	GraphScanActivity activity = null;

	private int testIndex = 1;
	DecodeHandler(GraphScanActivity activity) {
		this.activity = activity;
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
		case R.id.decode:
			decode((byte[]) message.obj, message.arg1, message.arg2);
			break;
		case R.id.quit:
			Looper.myLooper().quit();
			break;
		}
	}

	private void decode(byte[] data, int width, int height) {
		byte[] rotatedData = new byte[data.length];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++)
				rotatedData[x * height + height - y - 1] = data[x + y * width];
		}
		int tmp = width;// Here we are swapping, that's the difference to #11
		width = height;
		height = tmp;
		//// TODO: 2017/2/19 decode graph
		/* decode Graph data
		ZbarManager manager = new ZbarManager();
		String result = manager.decode(rotatedData, width, height, true, activity.getX(), activity.getY(), activity.getCropWidth(),
				activity.getCropHeight());
		/* end */
		String result = null;
		Log.d(TAG, "decode data");

		//region test model
		if(testIndex ++ % 20 == 0){
			result = "this is test result!";
		}else{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//endregion

		if (result != null) {
			if (activity.isNeedCapture()) {
				// 生成bitmap
				PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(rotatedData, width, height, activity.getX(), activity.getY(),
						activity.getCropWidth(), activity.getCropHeight(), false);
				int[] pixels = source.renderThumbnail();
				int w = source.getThumbnailWidth();
				int h = source.getThumbnailHeight();
				Bitmap bitmap = Bitmap.createBitmap(pixels, 0, w, w, h, Bitmap.Config.ARGB_8888);
				try {
					String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Qrcode/";
					File root = new File(rootPath);
					if (!root.exists()) {
						root.mkdirs();
					}
					File f = new File(rootPath + "Qrcode.jpg");
					if (f.exists()) {
						f.delete();
					}
					f.createNewFile();

					FileOutputStream out = new FileOutputStream(f);
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
					out.flush();
					out.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			if (null != activity.getHandler()) {
				Message msg = new Message();
				msg.obj = result;
				msg.what = R.id.decode_succeeded;
				activity.getHandler().sendMessage(msg);
			}
		} else {
			if (null != activity.getHandler()) {
				activity.getHandler().sendEmptyMessage(R.id.decode_failed);
			}
		}
	}

}

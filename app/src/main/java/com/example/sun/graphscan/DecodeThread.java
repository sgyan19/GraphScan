package com.example.sun.graphscan;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.CountDownLatch;

final class DecodeThread extends Thread {

	GraphScanActivity activity;
	private Handler handler;
	private final CountDownLatch handlerInitLatch;

	DecodeThread(GraphScanActivity activity) {
		this.activity = activity;
		handlerInitLatch = new CountDownLatch(1);
	}

	Handler getHandler() {
		try {
			handlerInitLatch.await();
		} catch (InterruptedException ie) {
			// continue?
		}
		return handler;
	}

	@Override
	public void run() {
		Looper.prepare();
		handler = new DecodeHandler(activity);
		handlerInitLatch.countDown();
		Looper.loop();
	}

}

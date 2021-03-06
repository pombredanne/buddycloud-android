package com.buddycloud;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.app.IntentService;
import android.content.Intent;

import com.buddycloud.model.PostsModel;

public class PendingPostsService extends IntentService {

	public PendingPostsService() {
		super("PendingPostsService");
	}

	private static Executor EXECUTOR = Executors.newFixedThreadPool(1);
	private boolean running;
	
	public void stop() {
		this.running = false;
		stopSelf();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		this.running = true;
		EXECUTOR.execute(new Runnable() {
			@Override
			public void run() {
				while (running) {
					PostsModel.getInstance().savePendingPosts(
							getApplicationContext(), PendingPostsService.this);
					try {
						Thread.sleep(30000);
					} catch (InterruptedException e) {}
				}
			}
		});
	}
}

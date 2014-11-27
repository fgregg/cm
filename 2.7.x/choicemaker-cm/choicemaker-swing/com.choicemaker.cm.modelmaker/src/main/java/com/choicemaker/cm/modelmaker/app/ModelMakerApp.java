package com.choicemaker.cm.modelmaker.app;

import com.choicemaker.e2.CMPlatform;
import com.choicemaker.e2.CMPlatformRunnable;
import com.choicemaker.e2.platform.InstallablePlatform;

public class ModelMakerApp {

	public static void main(String[] args) {
		CMPlatform cmp = InstallablePlatform.getInstance();
		final String extensionId = "com.choicemaker.cm.modelmaker.ModelMaker";
		CMPlatformRunnable runnable = cmp.loaderGetRunnable(extensionId);
		try {
			runnable.run(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

package r01f.concurrent;

import r01f.types.TimeLapse;

public class Threads {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static void safeSleep(final long milis) {
		try {
			Thread.sleep(milis);
		} catch (InterruptedException ignore) {
			ignore.printStackTrace();
		}
	}
	public static void safeSleep(final TimeLapse lapse) {
		Threads.safeSleep(lapse.asMilis());
	}
}

package com.cam001.widget.timer;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

public class TimeSchedlue {
	protected Timer mtimer = null;// ��ʱ��
	protected UserTimerTask mtask = null;// ��ʱ��
	protected static int DEFALUTPERTIME = 500;// Ĭ�ϼ�ʱ��ÿ������ʱ��
	private List<ITimeListener> timeListeners = null;
	private int perTime = DEFALUTPERTIME;

	public TimeSchedlue() {
		timeListeners = new ArrayList<ITimeListener>();
	}

	public void addListener(ITimeListener listener) {
		if (listener == null) {
			return;
		}
		if (timeListeners != null) {
			timeListeners.add(listener);
		}
	}

	public void removeListener(ITimeListener listener) {
		if (listener == null) {
			return;
		}
		if (timeListeners != null) {
			timeListeners.remove(listener);
		}
	}

	public void clearAllListener() {
		if (timeListeners != null) {
			timeListeners.clear();
		}
	}

	/**
	 * ��ʼ��ʱ������
	 * 
	 * @param timeOut
	 *            ��ʱʱ��
	 */
	public void startTimer() {
		startTimer(DEFALUTPERTIME);
	}

	/**
	 * ��ʼ��ʱ������
	 */
	public void startTimer(int perTime) {
		this.perTime = perTime;
		if(mtimer != null && mtask!= null)
			return;
		
		if (mtimer == null) {
			mtimer = new Timer();
		}
		if (mtask == null) {
			mtask = new UserTimerTask();
		}
		mtimer.schedule(mtask, 0, this.perTime);
	}

	/**
	 * ֹͣ��ʱ������
	 */
	public void stopTimer() {
		if (null != mtimer) {
			mtimer.cancel();
			mtask.cancel();
			mtask = null;
			mtimer = null;
		}
	}

	private class UserTimerTask extends TimerTask {

		@Override
		public void run() {
			 
			synchronized (timeListeners) {
				if (timeListeners != null && timeListeners.size() > 0) {
					for (int i = timeListeners.size() - 1; i >= 0; i--) {
						timeListeners.get(i).onTime(System.currentTimeMillis());
					}
				}
			}
			 
		}
	};

}

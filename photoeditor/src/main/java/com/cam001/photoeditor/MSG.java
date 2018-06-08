package com.cam001.photoeditor;

public class MSG {

	private MSG() {
	}

	public static final int ACTIVITY_START = 0x00001001;
	public static final int ACTIVITY_FINISH = 0x00001002;
	public static final int ACTIVITY_CLEAR_SCREENON = 0x00001003;
	public static final int ACTIVITY_SHOW_TOAST = 0x00001004;
	public static final int ACTIVITY_ASYNC_JOB = 0x00001005;
	public static final int ACTIVITY_SYNC_JOB = 0x00001006;
	public static final int ACTIVITY_FACEFLUSH_JOB = 0x00001007;

	public static final int CAMERA_SHUTTER = 0x00002001;
	public static final int CAMERA_START_PREVIEW = 0x00002002;
	public static final int CAMERA_STOP_PREVIEW = 0x00002003;
	public static final int CAMERA_FOCUS_CAPTURE = 0x00002004;
	public static final int CAMERA_START_DETECT = 0x00002005;
	public static final int CAMERA_HIDE_WHEEL = 0x00002006;
	public static final int CAMERA_HIDE_SLIDING = 0x00002007;

	public static final int EDITOR_LOAD = 0x00003001;
	public static final int EDITOR_SAVE = 0x00003002;
	public static final int EDITOR_SWITCH_MODE = 0x00003003;
	public static final int EDITOR_REFRESH_MAKEUP = 0x00003004;
	public static final int EDIT_MORE = 0x00003005;
	
	// public static final int EDITOR_ENSUREFACE_DIALOG = 0x00003005;
	public static final int EDITOR_CANCEL = 0x00003006;
	public static final int SELFIE_ICON = 0x00004001;
	public static final int SELFIE_TITLE = 0x00004002;
	public static final int SELFIE_LOADURL = 0x00004003;
	
	public static final int STORE_DOWNLOAD_FINISH = 0x00006001;
	public static final int STORE_DOWNLOAD_ERROR = 0x00006002;
	public static final int STORE_DOWNLOAD_PROGRESS = 0x00006003;
	public static final int STORE_DOWNLOAD_START = 0x00006004;
	
	public static final int HIDE_PERCENT_TXT = 0x00007001;
}

/**
 *   Copyright (C) 2010,2013 Thundersoft Corporation
 *   All rights Reserved
 */
package com.cam001.share;

import com.cam001.photoeditor.R;

public enum ShareItem {
//	QQAvatar(ShareItemIDs.QQAvatar, R.drawable.share_qqavatar_select),
	QQ 		(ShareItemIDs.QQ, 		R.drawable.share_qq_select,R.string.qq),
	WECHAT	(ShareItemIDs.WECHAT,	R.drawable.share_wechat_select,R.string.sns_wechat),
	WECHATGP(ShareItemIDs.WECHATGP,	R.drawable.share_quan_select,R.string.wechatfg), 
	SINA	(ShareItemIDs.SINA,		R.drawable.share_sina_select,R.string.sina),
//	TCWEIBO	(ShareItemIDs.TCWEIBO, 	R.drawable.share_tengxun_select),
//	QZONE	(ShareItemIDs.QZONE,	R.drawable.share_qqzone_select),
//	RENREN	(ShareItemIDs.RENREN, 	R.drawable.share_renren_select),
	OTHER	(ShareItemIDs.OTHER,	R.drawable.share_more_select,R.string.sns_label_more),
	FACEBOOK(ShareItemIDs.FACEBOOK,	R.drawable.share_facebook_select,R.string.sns_label_facebook), 
	TWITTER	(ShareItemIDs.TWITTER, 	R.drawable.share_twitter_select,R.string.sns_label_twitter),
	PINTEREST	(ShareItemIDs.PINTEREST, 	R.drawable.share_pinterest_select,R.string.sns_label_pinterest),
	INSTAGRAM(ShareItemIDs.INSTAGRAM,R.drawable.share_instagram_select,R.string.sns_label_instagram);
//	EVENT	(SnsItemIDs.EVENT, 		R.drawable.gold, 				R.string.share_sina_event);
	private final int mId;
	private final int mIcon;
	private final int mName;
	private ShareItem(int id, int icon,int name){
		mId = id;
		mIcon = icon;
		mName=name;
	}

	public int getId() {
		return this.mId;
	}

	public int getIcon() {
		return this.mIcon;
	}
	
	public int getName(){
		return this.mName;
	}

	public static ShareItem[] sortedValues() {
		return defaultSortedValues();
	}

	private static ShareItem[] defaultSortedValues() {
		if(Util.isChinese())
			return new ShareItem[] { WECHAT, WECHATGP, SINA, OTHER };
		return new ShareItem[]{FACEBOOK, TWITTER, INSTAGRAM,PINTEREST, OTHER};
	}
}

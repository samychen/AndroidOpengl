package com.cam001.photoeditor.makeup.editor;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.cam001.photoeditor.MakeupActivity;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.makeup.engine.ResDataStyle;
import com.cam001.photoeditor.makeup.engine.TsMakeuprtEngine;
import com.cam001.photoeditor.makeup.struct.ContactStruct;

public class EditorViewContact extends EditorViewCateBase {
	private EditorContactItemAdapter editorContactItemAdapter = null;
	private View[] mViewGoup;
	private MakeupActivity mActivity = null;
	private ResDataStyle mCurrStyle = null;
	private ContactStruct[] mContacts = null;
	private TsMakeuprtEngine mEngine = TsMakeuprtEngine.getInstance();

	public EditorViewContact(Context context, ResDataStyle currStyle,
			ContactStruct[] contacts, int index) {
		super(context);
		this.mCurrStyle = currStyle;
		this.mContacts = contacts;
		this.mSel = index;
		initData(context);
	}

	protected void initData(Context context) {
		if(mSel == 0){
			mBlShowText = false;
			mSeekBar.setVisibility(View.GONE);
		}
		mSeekBar.setProgress(mCurrStyle.cratio);
		mActivity = ((MakeupActivity) context);
		int itemWidth = mActivity.mConfig.screenWidth / 6;
		int width = itemWidth * mContacts.length;
		mGridView.setNumColumns(mContacts.length);
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mGridView
				.getLayoutParams();
		params.width = width;
		mGridView.setLayoutParams(params);
		SmoothScroolTo(itemWidth*(mSel+1-6));
		editorContactItemAdapter = new EditorContactItemAdapter();
		mGridView.setAdapter(editorContactItemAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("bug", "position=" + position);
				if (position == 0) {
					mBlShowText = false;
					mSeekBar.setVisibility(View.GONE);
				} else {
					mBlShowText = true;
					mSeekBar.setVisibility(View.VISIBLE);
				}
				if (mSel != position) {
					if (mSel != DefaultSelect && mViewGoup[mSel] != null) {
						((ViewHolder) mViewGoup[mSel].getTag()).select
								.setSelected(false);
					}
					((ViewHolder) mViewGoup[position].getTag()).select
							.setSelected(true);
					mSel = position;
					ContactStruct contactStruct = mContacts[mSel];
					mSeekBar.setProgress(contactStruct.cratio);
					showTextView();
					mCurrStyle.cratio = contactStruct.cratio;
					mCurrStyle.ctemp = contactStruct.ctemp;
					mEngine.loadResource(mCurrStyle, ResDataStyle.CONTACT, true);
					if (mActivity != null){
						mActivity.setmIndexContact(mSel);
						mActivity.doMakeuprt();
					}
				}
			}
		});
		mSeekBar.setOnSeekBarChangeListener(this);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mCurrStyle.cratio = mContacts[mSel].cratio = seekBar.getProgress();
		mEngine.loadResource(mCurrStyle, ResDataStyle.CONTACT, true);
		if (mActivity != null)
			mActivity.doMakeuprt();
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {

	}

	public class EditorContactItemAdapter extends BaseAdapter {

		private EditorContactItemAdapter() {
			if (mViewGoup == null) {
				mViewGoup = new View[mContacts.length];
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mContacts.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ViewHolder viewHolder = null;
			if (mViewGoup[position] == null) {
				viewHolder = new ViewHolder();
				convertView = LayoutInflater.from(mContext).inflate(
						R.layout.layout_shadow_item, null);
				viewHolder.imageview = (ImageView) convertView
						.findViewById(R.id.imageview);
				viewHolder.select = (ImageView) convertView
						.findViewById(R.id.select);
				convertView.setTag(viewHolder);
				mViewGoup[position] = convertView;
			} else {
				convertView = mViewGoup[position];
				viewHolder = (ViewHolder) mViewGoup[position].getTag();
			}
			viewHolder.imageview.setImageBitmap(mContacts[position]
					.getThumbnail());
			if (mSel == position) {
				viewHolder.select.setSelected(true);
			} else {
				viewHolder.select.setSelected(false);
			}
			return convertView;
		}
	}

	private class ViewHolder {
		ImageView imageview;
		ImageView select;
	}

}

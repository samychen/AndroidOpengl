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
import android.widget.TextView;

import com.cam001.photoeditor.MakeupActivity;
import com.cam001.photoeditor.R;
import com.cam001.photoeditor.makeup.struct.ModuleStruct;


public class EditorViewModule extends EditorViewCateBase {
	public final int[] mRes_str = {R.string.none, R.string.cateyes, R.string.lavender,
			R.string.princess, R.string.refresh, R.string.silk, R.string.smoky_eyes,
			R.string.ol_luozhuang, R.string.guodongfenchun, R.string.xiariqinxin};
	private EditorModuleCateAdapter editorModuleCateAdapter = null;
	private View[] mViewGoup;
	private MakeupActivity mActivity = null;
	private ModuleStruct[] mStyles = null;
	
	public EditorViewModule(Context context, ModuleStruct[] styles, int index) {
		super(context);
		this.mStyles = styles;
		this.mSel = index;
		initData(context);
	}

	protected void initData(Context context) {
		mSeekBar.setVisibility(View.GONE);
		mActivity = ((MakeupActivity) context);
		int itemWidth = mActivity.mConfig.screenWidth / 6;
		int width = itemWidth * mStyles.length;
		mGridView.setNumColumns(mStyles.length);
		mGridView.setLayoutParams(new LinearLayout.LayoutParams(width,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		SmoothScroolTo(itemWidth*(mSel+1-6));
		editorModuleCateAdapter = new EditorModuleCateAdapter();
		mGridView.setAdapter(editorModuleCateAdapter);
		mGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d("bug", "position=" + position);
				if (mSel != position) {
					// if(editorModuleCateAdapter != null)
					// editorModuleCateAdapter.notifyDataSetChanged();
					if (mViewGoup[mSel] != null) {
						ViewHolder viewHolder = (ViewHolder) mViewGoup[mSel]
								.getTag();
						viewHolder.imageSel.setSelected(false);
						viewHolder = (ViewHolder) view.getTag();
						viewHolder.imageSel.setSelected(true);
					}
					mSel = position;
					if(mActivity != null){
						mActivity.setIndexModule(mSel);
						mActivity.changeMode(mSel);
					}
				}
			}
		});
	}

	public class EditorModuleCateAdapter extends BaseAdapter {

		private EditorModuleCateAdapter() {
			if (mViewGoup == null) {
				mViewGoup = new View[mStyles.length];
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mStyles.length;
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
						R.layout.layout_mode_item, null);
				viewHolder.imageSrc = (ImageView) convertView
						.findViewById(R.id.imagesrc);
				viewHolder.imageSel = (ImageView) convertView
						.findViewById(R.id.imageselect);
				viewHolder.textview = (TextView) convertView
						.findViewById(R.id.textview);
				convertView.setTag(viewHolder);
				mViewGoup[position] = convertView;
			} else {
				convertView = mViewGoup[position];
				viewHolder = (ViewHolder) mViewGoup[position].getTag();
			}

//			viewHolder.imageSrc.setImageResource(mRes_thumb[position]);
			viewHolder.imageSrc.setImageBitmap(mStyles[position].getThumbnail());
			viewHolder.textview.setText(mRes_str[position]);
			if (mSel == position) {
				viewHolder.imageSel.setSelected(true);
			} else
				viewHolder.imageSel.setSelected(false);
			return convertView;
		}
	}

	private class ViewHolder {
		ImageView imageSrc;
		ImageView imageSel;
		TextView textview;

	}
}

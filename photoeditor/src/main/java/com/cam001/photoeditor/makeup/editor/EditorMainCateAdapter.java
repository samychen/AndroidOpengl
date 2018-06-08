package com.cam001.photoeditor.makeup.editor;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cam001.photoeditor.R;
import com.cam001.stat.StatApi;
import com.cam001.util.DensityUtil;
import com.cam001.util.Util;

public class EditorMainCateAdapter extends BaseAdapter {
	public static final int DefaultSelect = 0;
	private int[] mThumb = { R.drawable.onekey_select,
			R.drawable.yanying_select, R.drawable.yanxian_select,
			R.drawable.meitong_select, R.drawable.jiemao_select,
			R.drawable.saihong_select, R.drawable.chuncai_select,
			R.drawable.more_select };
	private int mSel = DefaultSelect;
	private int[] mStr = { R.string.onekey, R.string.shadow, R.string.eyeline,
			R.string.contact, R.string.eyelash, R.string.blush,
			R.string.lipstick, R.string.sns_label_more };
	private Context mContext = null;
	private EditorMainItemListener mainCateClickListener = null;

	public interface EditorMainItemListener {
		public void onMainItemClick(int position);
	}

	public EditorMainCateAdapter(Context context, GridView gridview) {
		this.mContext = context;
		if (gridview != null) {
			gridview.setNumColumns(mStr.length);
			int width = DensityUtil.dip2px(context, 65.0f) * mStr.length;
			LayoutParams params = (LayoutParams) gridview
					.getLayoutParams();
			params.width = width;
			params.gravity = Gravity.CENTER;
			gridview.setGravity(Gravity.CENTER);
			gridview.setLayoutParams(params);
		}
		mainCateClickListener = (EditorMainItemListener) context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mStr.length;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.layout_maincate_item, null);
			viewHolder.sel = (ImageView) convertView.findViewById(R.id.sel);
			viewHolder.iv = (ImageView) convertView.findViewById(R.id.iv);
			viewHolder.tv = (TextView) convertView.findViewById(R.id.textview);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		viewHolder.tv.setText(mStr[position]);
		// Drawable top = mContext.getResources().getDrawable(mThumb[position]);
		// viewHolder.button.setCompoundDrawablesWithIntrinsicBounds(null, top,
		// null, null);
		// viewHolder.button.setBackgroundResource(R.drawable.maincate_select);
		viewHolder.iv.setImageResource(mThumb[position]);
		if (mSel == position) {
			viewHolder.sel.setSelected(true);
			viewHolder.iv.setSelected(true);
			viewHolder.tv.setSelected(true);
		} else {
			viewHolder.sel.setSelected(false);
			viewHolder.iv.setSelected(false);
			viewHolder.tv.setSelected(false);
		}
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(position == (mThumb.length-1)){
					SharedPreferences sp = mContext.getSharedPreferences(StatApi.APPACKAGE_NAME, Activity.MODE_PRIVATE);
					String appackage = sp.getString(StatApi.APPACKAGE_NAME, null);
					if(appackage != null){
						Util.goToAppStore(mContext, appackage);
					}else
						Util.goToAppStore(mContext, "com.ucamera.ucam");
					return;
				}
				if (mSel != position) {
					mSel = position;
					if (mainCateClickListener != null) {
						mainCateClickListener.onMainItemClick(mSel);
					}
					notifyDataSetChanged();
				}
			}
		});
		return convertView;
	}

	public void setHighlight(int pos) {
		mSel = pos;
		notifyDataSetChanged();
	}

	class ViewHolder {
		ImageView iv, sel;
		TextView tv;
	}
}

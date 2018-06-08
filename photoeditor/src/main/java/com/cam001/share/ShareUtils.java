/*
 * Copyright (C) 2011,2013 Thundersoft Corporation
 * All rights Reserved
 */

package com.cam001.share;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.cam001.photoeditor.R;
import com.cam001.util.ToastUtil;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.Toast;

public class ShareUtils {
    public static void shareImage(Context context, Uri uri) {
//        if(Build.SNS_SHARE_ON) {
            snsShare(context, uri);
//        } else {
//            otherShare(context, uri, null, Bitmap.CompressFormat.JPEG);
//        }
    }

    private static void snsShare(Context context, Uri uri) {
        Intent intent = new Intent();
        intent.setClassName(context, "com.ucamera.ucomm.sns.ShareActivity");
        intent.setAction("android.intent.action.UGALLERY_SHARE");
        intent.setDataAndType(uri, "image/*");
        try {
            context.startActivity(intent);
        } catch(ActivityNotFoundException e) {
        	ToastUtil.showToast(context,
                    Toast.LENGTH_LONG, R.string.edit_operation_failure_tip);
        }
    }

    public static void otherShare(Context context, Uri uri, Bitmap bitmap, Bitmap.CompressFormat outputFormat) {
        try {
            /*
             * filter the activity GifPlayActivity and
             * GIFEditActivity from the application list
             */
            String [] filterClasses = new String[3];
            filterClasses[0] = "com.ucamera.ucam.modules.ugif.GifPlayActivity";
            filterClasses[1] = "com.ucamera.ucam.modules.ugif.edit.GIFEditActivity";
            filterClasses[2] = "com.ucamera.uphoto.ImageEditControlActivity";
            showShareDialog(uri, filterClasses, context);
        } catch (Exception e) {
        	ToastUtil.showToast(context,
					Toast.LENGTH_LONG, R.string.edit_operation_failure_tip);
        }
    }

    private static AlertDialog mShareDialog = null;   // the dialog to show the application list
    /**show the application list dialog which support the type "image/*".
     * @param uri which needed to open.
     * @param classNameString the class name to avoid appearing in the list
     * @param context context is the current activity.
     */
    private static void showShareDialog(Uri uri,String[] classNameString,final Context context) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        final List<ShareInfoItem> lists=new ArrayList<ShareInfoItem>();
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent, 0);
        Collections.sort(activities, new ResolveInfo.DisplayNameComparator(packageManager));
        int numActivities = activities.size();
        for (int i = 0; i != numActivities; ++i) {
            final ResolveInfo info = activities.get(i);
            ActivityInfo ai = info.activityInfo;
            String className = ai.name;
            int numOfClasses = classNameString.length;
            boolean bContinue = false;
            /*
             * filter the classes from the application list
             */
            for (int j = 0; j < numOfClasses; j++) {
                if (className != null && className.equals(classNameString[j])) {
                    bContinue = true;
                }
            }
            if(bContinue) {
                continue;
            }
            Drawable iconId = info.loadIcon(packageManager);
            if(iconId == null) {
                iconId = ai.applicationInfo.loadIcon(packageManager);
            }
            String textId = info.loadLabel(packageManager).toString();

            ShareInfoItem shareInfoItem = new ShareInfoItem();
            shareInfoItem.iconId = iconId;
            shareInfoItem.textId = textId;
            shareInfoItem.activityInfo = ai;

            lists.add(shareInfoItem);
        }
        ShareBaseAdapter shareBaseAdapter = new ShareBaseAdapter(context, R.layout.resolve_list_item, lists);

        if(mShareDialog != null && mShareDialog.isShowing()) {
            mShareDialog.dismiss();
            mShareDialog = null;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(R.string.text_edit_share_title);
            builder.setAdapter(shareBaseAdapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ShareInfoItem shareItem = lists.get(which);
                    ActivityInfo info = shareItem.activityInfo;
                    final Intent resolvedIntent = new Intent(intent);
                    resolvedIntent.setComponent(new ComponentName(info.applicationInfo.packageName, info.name));
                    try {
                        context.startActivity(resolvedIntent);
                    } catch (ActivityNotFoundException ex) {
                    	ToastUtil.showToast(context,
								Toast.LENGTH_SHORT, R.string.edit_operation_failure_tip);
                    } finally {
                        mShareDialog.dismiss();
                        mShareDialog = null;
                    }
                }
            });
            mShareDialog = builder.create();
            mShareDialog.show();
        }
    }
}

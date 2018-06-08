package com.cam001.widget;

import android.content.Context;
import android.media.ExifInterface;

import com.cam001.photoeditor.AppConfig;
import com.cam001.photoeditor.R;

import java.util.ArrayList;

/**
 * Created by dell on 15-6-9.
 */
public class PhotoImage {

    private String photo_aperture;
    private String photo_datetime;
    private String photo_exposure_time;
    private String photo_flash;
    private String photo_focal_length;
    private String photo_image_width;
    private String photo_image_length;
    private String photo_iso;
    private String photo_marker;
    private String photo_model;
    private String photo_orientation;
    private String photo_white_balance;
    private String photo_name;
    private String photo_path;
    public ArrayList<String> mDetailList=new ArrayList<String>();
    private Context mContext;
    private String photo_size;

    public PhotoImage(){
        mContext=AppConfig.getInstance().appContext;
    }

    public String getPath() {
        return photo_path;
    }

    public void setPath(String path) {
        if (path!=null) {
            this.photo_path = mContext.getString(R.string.photo_path) + path;
            mDetailList.add(photo_path);
        }
    }

    public String getPhotoAperture() {
        return photo_aperture;
    }

    public void setPhotoAperture(String aperture) {
        if (aperture!=null){
            this.photo_aperture = mContext.getString(R.string.photo_aperture) +aperture;
            mDetailList.add(photo_aperture);
        }
    }

    public String getPhotoDatetime() {
        return photo_datetime;
    }

    public void setPhotoDatetime(String datetime) {
        if (datetime!=null){
            this.photo_datetime = mContext.getString(R.string.photo_datetime)
                    +datetime;
            mDetailList.add(photo_datetime);
        }
    }

    public String getPhotoExposureTime() {
        return photo_exposure_time;
    }

    public void setPhotoExposureTime(String exposure_time) {
        if (exposure_time!=null){
            this.photo_exposure_time =  mContext.getString(R.string.photo_exposure_time)
                    +exposure_time;
            mDetailList.add(photo_exposure_time);
        }

    }

    public String getPhotoFlash() {
        return photo_flash;
    }

    public void setPhotoFlash(String flash) {
        if (flash!=null){
            if (flash.equals("0")){
                this.photo_flash = mContext.getString(R.string.photo_flash)
                        +mContext.getString(R.string.switch_close);
            }else {
                this.photo_flash = mContext.getString(R.string.photo_flash)
                        +mContext.getString(R.string.switch_open);
            }
            mDetailList.add(photo_flash);
        }
    }

    public String getPhotoFocalLength() {
        return photo_focal_length;
    }

    public void setPhotoFocalLength(Double focal_length) {
        if (focal_length!=-1){
            this.photo_focal_length =mContext.getString(R.string.photo_focal_length)
                    + (String.valueOf(focal_length) + "MM");
            mDetailList.add(photo_focal_length);
        }

    }

    public String getPhotoImageWidth() {
        return photo_image_width;
    }

    public void setPhotoImageWidth(String image_width) {
        if (image_width!=null){
            this.photo_image_width =mContext.getString(R.string.photo_image_width)
                    +  image_width;
            mDetailList.add(photo_image_width);
        }

    }

    public String getPhotoImageLength() {
        return photo_image_length;
    }

    public void setPhotoImageLength(String image_length) {
        if (image_length!=null){
            this.photo_image_length = mContext.getString(R.string.photo_image_length)
                    +  image_length;
            mDetailList.add(photo_image_length);
        }
    }

    public String getPhotoIso() {
        return photo_iso;
    }

    public void setPhotoIso(String iso) {
        if (iso!=null){
            this.photo_iso =mContext.getString(R.string.photo_iso)
                    +  iso;
            mDetailList.add(photo_iso);
        }

    }

    public String getPhotoMarker() {
        return photo_marker;
    }

    public void setPhotoMarker(String marker) {
        if (marker!=null){
            this.photo_marker =mContext.getString(R.string.photo_marker)
                    +   marker;
            mDetailList.add(photo_marker);
        }
    }

    public String getPhotoModel() {
        return photo_model;
    }

    public void setPhotoModel(String model) {
        if (model!=null){
            this.photo_model =mContext.getString(R.string.photo_model)
                    +  model;
            mDetailList.add(photo_model);
        }

    }

    public String getPhoto_orientation() {
        return photo_orientation;
    }

    public void setPhotoOrientation(int orientation) {
        if (orientation!=-1){
            int degree=0;
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
            if(degree!=0){
                this.photo_orientation =mContext.getString(R.string.photo_orientation)
                        +   degree;
                mDetailList.add(photo_orientation);
            }
        }
    }

    public String getPhotoWhiteBalance() {
        return photo_white_balance;
    }

    public void setPhotoWhiteBalance(int white_balance) {
        if (white_balance!=-1){
            String balance=null;
            switch (white_balance) {
                case ExifInterface.WHITEBALANCE_AUTO:
                    balance= mContext.getString(R.string.balance_auto);
                    break;
                case ExifInterface.WHITEBALANCE_MANUAL:
                    balance= mContext.getString(R.string.balance_manual);
                    break;
                default:
                    balance=null;
                    break;
            }
            if (balance!=null){
                this.photo_white_balance = mContext.getString(R.string.photo_white_balance)
                        + balance;
                mDetailList.add(photo_white_balance);
            }
        }

    }

    public String getName() {
        return photo_name;
    }

    public void setName(String name) {
        if (name!=null){
            this.photo_name = mContext.getString(R.string.photo_name)
                    +  name;
            mDetailList.add(photo_name);
        }
    }

    public void setSize(String size) {
        if (size!=null){
            this.photo_size = mContext.getString(R.string.photo_image_size)
                    +  size;
            mDetailList.add(photo_size);
        }

    }
}

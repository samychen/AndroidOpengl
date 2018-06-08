package com.cam001.photoeditor.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

import com.cam001.util.CipherUtil;
import com.cam001.util.Util;

public class TemplateEncrypt extends Template{

	private CipherUtil mCipherUtil = null;
	
	public TemplateEncrypt(String path) {
		super(path);
		mCipherUtil = new CipherUtil();
	}
	
	@Override
	protected InputStream openFileInputStream(String path){
//		if(!new File(path).exists()) return null;
		final InputStream fis = super.openFileInputStream(path);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			mCipherUtil.decrypt(fis, out, "thunders");
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Util.closeSilently(out);
		Util.closeSilently(fis);
		return new ByteArrayInputStream(out.toByteArray());
	}
	
}

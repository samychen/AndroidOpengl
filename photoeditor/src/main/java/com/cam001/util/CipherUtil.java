package com.cam001.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CipherUtil {
	
	private static byte[] iv = {1,2,3,4,5,6,7,8};
	/**
	 * 把文件srcFile加密后存储为destFile
	 * 
	 * @param srcFile
	 *            加密前的文件
	 * @param destFile
	 *            加密后的文件
	 * @param privateKey
	 *            密钥
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public void encrypt(String srcFile, String destFile, String privateKey)
			throws GeneralSecurityException, IOException {
		Key key = getKey(privateKey);
		Cipher cipher = Cipher.getInstance(type+"/CBC/PKCS5Padding");
		IvParameterSpec zeroIv = new IvParameterSpec(iv); 
		cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
		File srcDir = new File(srcFile);
		dgencrypt(srcDir, cipher, destFile);
	}

	private void dgencrypt(File file, Cipher cipher, String destFile)throws GeneralSecurityException, IOException {
		if (file.isDirectory()) {
			for (File src : file.listFiles()) {
				dgencrypt(src,cipher,destFile+"/"+src.getName());
				
			}
		} else {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(file);
				fos = new FileOutputStream(mkdirFiles(destFile));

				crypt(fis, fos, cipher);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fis != null) {
					fis.close();
				}
				if (fos != null) {
					fos.close();
				}
			}
		}
	}

	private static String type = "DES";

	/**
	 * 把文件srcFile解密后存储为destFile
	 * 
	 * @param srcFile
	 *            解密前的文件
	 * @param destFile
	 *            解密后的文件
	 * @param privateKey
	 *            密钥
	 * @throws GeneralSecurityException
	 * @throws IOException
	 */
	public void decrypt(String srcFile, String destFile, String privateKey)
			throws GeneralSecurityException, IOException {
		Key key = getKey(privateKey);
		Cipher cipher = Cipher.getInstance(type+"/CBC/PKCS5Padding");
		IvParameterSpec zeroIv = new IvParameterSpec(iv); 
		cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
		File srcDir = new File(srcFile);
		dgdecrypt(srcDir, cipher, destFile);
	}
	
	public void decrypt(InputStream in, OutputStream out, String privateKey)
			throws GeneralSecurityException, IOException {
		Key key = getKey(privateKey);
		Cipher cipher = Cipher.getInstance(type+"/CBC/PKCS5Padding");
		IvParameterSpec zeroIv = new IvParameterSpec(iv); 
		cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
		crypt(in, out, cipher);
	}

	private void dgdecrypt(File file, Cipher cipher, String destFile)throws GeneralSecurityException, IOException {
		if (file.isDirectory()) {
			for (File src : file.listFiles()) {
				dgdecrypt(src,cipher,destFile+"/"+src.getName());
				
			}
		} else {
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try {
				fis = new FileInputStream(file);
				fos = new FileOutputStream(mkdirFiles(destFile));

				crypt(fis, fos, cipher);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fis != null) {
					fis.close();
				}
				if (fos != null) {
					fos.close();
				}
			}
		}
	}
	
	/**
	 * 加密解密流
	 * 
	 * @param in
	 *            加密解密前的流
	 * @param out
	 *            加密解密后的流
	 * @param cipher
	 *            加密解密
	 * @throws IOException
	 * @throws GeneralSecurityException
	 */
	private static void crypt(InputStream in, OutputStream out, Cipher cipher)
			throws IOException, GeneralSecurityException {
		int blockSize = cipher.getBlockSize() * 1000;
		int outputSize = cipher.getOutputSize(blockSize);

		byte[] inBytes = new byte[blockSize];
		byte[] outBytes = new byte[outputSize];

		int inLength = 0;
		boolean more = true;
		while (more) {
			inLength = in.read(inBytes);
			if (inLength == blockSize) {
				int outLength = cipher.update(inBytes, 0, blockSize, outBytes);
				out.write(outBytes, 0, outLength);
			} else {
				more = false;
			}
		}
		if (inLength > 0)
			outBytes = cipher.doFinal(inBytes, 0, inLength);
		else
			outBytes = cipher.doFinal();
		out.write(outBytes);
	}

	/**
	 * 根据filePath创建相应的目录
	 * 
	 * @param filePath
	 *            要创建的文件路经
	 * @return file 文件
	 * @throws IOException
	 */
	private File mkdirFiles(String filePath) throws IOException {
		File file = new File(filePath);
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		file.createNewFile();

		return file;
	}

	/**
	 * 生成指定字符串的密钥
	 * 
	 * @param secret
	 *            要生成密钥的字符串
	 * @return secretKey 生成后的密钥
	 * @throws GeneralSecurityException
	 */
	private static Key getKey(String secret) throws GeneralSecurityException {
//		KeyGenerator kgen = KeyGenerator.getInstance(type);
//		// kgen.init(128, new SecureRandom(secret.getBytes()));
//
//		SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
//		sr.setSeed(secret.getBytes());
//		kgen.init(56, sr); // 192 and 256 bits may not be available
//		SecretKey secretKey = kgen.generateKey();
//		return secretKey;
		return new SecretKeySpec(secret.getBytes(), type);
	}
}

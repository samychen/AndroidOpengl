package com.cam001.photoeditor.makeup.engine;

import android.util.Log;

import com.cam001.photoeditor.makeup.struct.BlushStruct;
import com.cam001.photoeditor.makeup.struct.ContactStruct;
import com.cam001.photoeditor.makeup.struct.LashStruct;
import com.cam001.photoeditor.makeup.struct.LineStruct;
import com.cam001.photoeditor.makeup.struct.LipstickStruct;
import com.cam001.photoeditor.makeup.struct.ShadowStruct;


public class ResDataStyle {
	public static final int FULL = -1;
	public static final int BLUSH = 0;
	public static final int LIPSTICK = 1;
	public static final int EYELASH = 2;
	public static final int EYELINE = 3;
	public static final int EYESHADOW = 4;
	public static final int CONTACT = 5;
	
	public int bratio;//blush ���
	public String bcolor;
	public String btemp;
	
	public int lratio;//lipstick �ں�
	public int lgloss;
	public String lcolor;
	public String ltemp;
	
	public int elashratio;//eyelash �۽�ë
	public String elashcolor;
	public String elashupper;
	public String elashlower;
	
	public int elineratio;//eyaline ����
	public String elinercolor;
	public String elinerupper;
	public String elinerlower;
	
	public int eshaderatio;//eyeshadow ��Ӱ
	public int eshaderfratio;
	public String eshadercolor;
	public String eshadertemp;
	
	public int cratio;//��ͫ ContactLen
	public String ctemp;
	
	public void show() {
		Log.d("bug", "blush="+bratio+","+bcolor+","+btemp);
		Log.d("bug", "lipstick="+lratio+","+lgloss+","+lcolor+","+ltemp);
		Log.d("bug", "elash="+elashratio+","+elashcolor+","+elashupper+","+elashlower);
		Log.d("bug", "eliner="+elineratio+","+elinercolor+","+elinerupper+","+elinerlower);
		Log.d("bug", "eshader="+eshaderatio+","+eshaderfratio+","+eshadercolor+","+eshadertemp);
		Log.d("bug", "ContactLen="+cratio+","+ctemp);
	}
	
	public void setStyle(ShadowStruct shadowStruct, LineStruct lineStruct,
			ContactStruct contactStruct, LashStruct lashStruct,
			BlushStruct blushStruct, LipstickStruct lipstickStruct) {
		this.bratio = blushStruct.bratio;// blush ���
		this.bcolor = blushStruct.bcolor;
		this.btemp = blushStruct.btemp;

		this.lratio = lipstickStruct.lratio;// lipstick �ں�
		this.lgloss = lipstickStruct.lgloss;
		this.lcolor = lipstickStruct.lcolor;
		this.ltemp = lipstickStruct.ltemp;

		this.elashratio = lashStruct.elashratio;// eyelash �۽�ë
		this.elashcolor = lashStruct.elashcolor;
		this.elashupper = lashStruct.elashupper;
		this.elashlower = lashStruct.elashlower;

		this.elineratio = lineStruct.elineratio;// eyaline ����
		this.elinercolor = lineStruct.elinercolor;
		this.elinerupper = lineStruct.elinerupper;
		this.elinerlower = lineStruct.elinerlower;

		this.eshaderatio = shadowStruct.eshaderatio;// eyeshadow ��Ӱ
		this.eshaderfratio = shadowStruct.eshaderfratio;
		this.eshadercolor = shadowStruct.eshadercolor;
		this.eshadertemp = shadowStruct.eshadertemp;

		this.cratio = contactStruct.cratio;// ��ͫ ContactLen
		this.ctemp = contactStruct.ctemp;
	}
}

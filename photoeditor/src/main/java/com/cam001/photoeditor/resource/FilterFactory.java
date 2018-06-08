package com.cam001.photoeditor.resource;

import java.util.ArrayList;
import java.util.Random;

public class FilterFactory {
	
	private static final String PATH_FILTE_ROOT = "filters/";
	private static final String PATH_CATE_LEICA = PATH_FILTE_ROOT+"Leica";
	private static final String PATH_CATE_LIGHT = PATH_FILTE_ROOT+"Light";
	
	private static final FilterCategory[] FILTER_CATES = new FilterCategory[]{
			new FilterCategory(PATH_CATE_LIGHT),
			new FilterCategory(PATH_CATE_LEICA)
	};
	
	public static FilterCategory[] createFilters() {
		return FILTER_CATES;
	}
	
	public static Filter[] createRandomFilters(int count) {
		Filter[] res = new Filter[count];
		int index = 0;
		Random r = new Random(System.currentTimeMillis());
		while(index<count) {
			int cateIndex = (int)(r.nextFloat()*FILTER_CATES.length);
			FilterCategory cate = FILTER_CATES[cateIndex];
			ArrayList<Filter> filters = cate.getFilters();
			int filterIndex = (int)(r.nextFloat()*filters.size());
			Filter filter = filters.get(filterIndex);
			boolean isDup = false;
			for(Filter f: res) {
				if(filter.equals(f)) {
					isDup = true;
					break;
				}
			}
			if(!isDup) {
				res[index] = filter;
				index ++;
			}
		}
		return res;
	}
}

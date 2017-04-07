package org.platform.modules.mapreduce.clean.ly.cleanTool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.platform.modules.mapreduce.clean.util.CleanUtil;

public class StudentTool {
	// 需要跳过的特殊字段的集合
	static List<String> limitList = new ArrayList<String>();
	static {
		limitList.add("updateTime");
		limitList.add("sourceFile");
		limitList.add("examId");
		limitList.add("insertTime");
		limitList.add("checkNumber");
		limitList.add("schoolNumber");
		limitList.add("formalities");
		limitList.add("examineeNumber");
		limitList.add("studentCode");
		limitList.add("applyNumber");
		limitList.add("poorlyNumber");
		limitList.add("areaCode");
		limitList.add("applyPlaceNumber");
		limitList.add("_id");
		limitList.add("email");
		limitList.add("qqNum");
	}

	// 寻找符合指定字段的值
	public static List<String> findMatch(Map<String, Object> map, String param) {
		Iterator<Entry<String, Object>> newMap = map.entrySet().iterator();
		List<String> list = new ArrayList<String>();
		while (newMap.hasNext()) {
			Entry<String, Object> entry = newMap.next();
			// 获取当前遍历的key和value值
			String key = entry.getKey();
			String value = entry.getValue().toString();
			// 特殊字段跳过
			if (limitList.contains(key)) {
				continue;
			}
			if (("idCard".equals(param) && !"lgIdCard".equals(key))
					|| ("lgIdCard".equals(param) && !"idCard".equals(key))
					|| ("phone".equals(param) && !"lgphone".equals(key)
							&& !"call".equals(key) && !"lgIdCard".equals(key) && !"idCard"
								.equals(key))
					|| ("lgphone".equals(param) && !"phone".equals(key)
							&& !"call".equals(key) && !"lgIdCard".equals(key) && !"idCard"
								.equals(key))
					|| ("call".equals(param) && !"phone".equals(key)
							&& !"lgphone".equals(key)
							&& !"lgIdCard".equals(key) && !"idCard".equals(key))) {
				// 若在其他字段找到匹配的值，则取出匹配到的值
				List<String> list2 = findOther(param,value);
				if (list2!=null && list2.size()>0) {
					list.add(list2.get(0));
					list.add(key);
					list.add(list2.get(1));
					break;
				}
			}
		}
		return list;
	}

	/**
	 * 将找出匹配值得字段的其他值，放入cnote字段
	 * 
	 * @param map
	 *            原有数据的集合
	 * @param param
	 *            字段名
	 * @param value
	 *            需要添加到address或linkAddress的值
	 * @return
	 */
	public static Map<String, Object> addCnote(Map<String, Object> map,String value) {
			if (map.containsKey("cnote")) {
				String cnoteValue = map.get("cnote").toString();
				if(!"".equals(cnoteValue) && !"NA".equals(cnoteValue)){
					StringBuffer newValue = new StringBuffer();
					newValue.append(map.get("cnote") + "," + value);
					map.put("cnote", newValue);
				}
			}else {
				if (!"".equals(value)) {
					map.put("cnote", value);
				}
			}
		return map;
	}

	/**
	 * 判断字段中是否还有其他值
	 * 
	 * @param param
	 *            字段名
	 * @param value
	 *            字段的值
	 * @return
	 */
	public static List<String> findOther(String param, String value) {
		String phoneRex = CleanUtil.phoneRex;
		String idCardRex = CleanUtil.idCardRex;
		String callRex = CleanUtil.callRex;
		List<String> list = new ArrayList<String>();
		if ("idCard".equals(param) || "lgIdCard".equals(param)) {
			Pattern p = Pattern.compile(idCardRex);
			String correct = null;
			Matcher m = p.matcher(value);
			if (m.find()) {
				correct = value.replace(m.group(), "");
				list.add(m.group());
				list.add(correct);
				return list;
			}
		}
		// 判断座机中是否有 非座机内容（必须先判断是否含有手机号码，并且部分座机有括号）
		if ("call".equals(param) || "lgphone".equals(param)
				|| "phone".equals(param)) {
			Pattern p = Pattern.compile(phoneRex);
			Matcher m = p.matcher(value);
			Pattern pCall = Pattern.compile(callRex);
			StringBuffer str = new StringBuffer();
			String newValue = null;

			if (CleanUtil.matchPhone(value)) {
				while (m.find()) {
					if (str.length() == 0) {
						str.append(m.group());
						newValue = value.replace(m.group(), "");
					} else {
						str.append("," + m.group());
					}
					newValue = newValue.replace(m.group(), "");
				}
				if (newValue != null) {
					Matcher mCall = pCall.matcher(newValue);
					while (mCall.find()) {
						str.append("," + mCall.group());
						newValue = newValue.replace(mCall.group(), "");
					}
					if (str.length() != 0) {
						list.add(str.toString());
						list.add(newValue);
						return list;
					}
				} else {
					Matcher mCall = pCall.matcher(value);
					while (mCall.find()) {
						if (str.length() == 0) {
							str.append(mCall.group());
							newValue = value.replace(mCall.group(), "");
						} else {
							str.append("," + mCall.group());
							newValue = newValue.replace(mCall.group(), "");
						}
					}
					if (str.length() != 0) {
						list.add(str.toString());
						list.add(newValue);
						return list;
					}
				}
			}
			if (CleanUtil.matchCall(value)) {
				Matcher mCall = pCall.matcher(value);
				while (mCall.find()) {
					if (str.length() == 0) {
						str.append(mCall.group());
						newValue = value.replace(mCall.group(), "");
					} else {
						str.append("," + mCall.group());
					}
					newValue = newValue.replace(mCall.group(), "");
				}
				if (str.length() != 0) {
					list.add(str.toString());
					list.add(newValue);
					return list;
				}
			}
		}
		return null;
	}
	
	/**
	 * 检查是否同时存在姓名和地址
	 * 注：姓名和地址有可能有多个
	 */
	public static boolean togetherMatch(Map<String,Object> map){
		boolean nameFlag = false;
		boolean addressFlag = false;
		//学生姓名和监护人姓名判断
		if(!CleanUtil.matchNum((String)map.get("name")) || !CleanUtil.matchNum((String)map.get("legalGuardian"))){
			nameFlag =true;
		}
		if(nameFlag && (CleanUtil.matchChinese((String)map.get("address")) || CleanUtil.matchChinese((String)map.get("presentAddress")))){
			addressFlag =true;
		}
		if((!CleanUtil.matchNum((String)map.get("consignee"))) && CleanUtil.matchChinese((String)map.get("mailingAddress"))){
			nameFlag =true;
			addressFlag =true;
		}
		if(nameFlag && addressFlag){
			return true;
		}else{
			return false;
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 添加别名以及更换key
	 * @param map
	 * @return
	 */
	public static Map<String,Object> replaceKey(Map<String,Object> map){
		
		//添加别名
		if(map.containsKey("name")){
			map.put("nameAlias", map.get("name"));
		}
		if(map.containsKey("legalGuardian")){
			map.put("legalGuardianName", map.get("legalGuardian"));
			map.put("legalGuardianNameAlias", map.get("legalGuardian"));
			map.remove("legalGuardian");
		}
		if(map.containsKey("consignee")){
			map.put("consigneeName", map.get("consignee"));
			map.put("consigneeNameAlias", map.get("consignee"));
			map.remove("consignee");
		}
		
		//更换key
		if(map.containsKey("birthday")){
			map.put("birthDay", map.get("birthday"));
			map.remove("birthday");
		}
		if(map.containsKey("phone")){
			map.put("mobilePhone", map.get("phone"));
			map.remove("phone");
		}
		if(map.containsKey("nationality")){
			map.put("country", map.get("nationality"));
			map.remove("nationality");
		}
		if(map.containsKey("nation")){
			map.put("nationality", map.get("nation"));
			map.remove("nation");
		}
		if(map.containsKey("call")){
			map.put("telePhone", map.get("call"));
			map.remove("call");
		}
		if(map.containsKey("school")){
			map.put("university", map.get("school"));
			map.remove("school");
		}
		if(map.containsKey("nativePlace")){
			map.put("birthPlace", map.get("nativePlace"));
			map.remove("nativePlace");
		}
		if(map.containsKey("qq")){
			map.put("qqNum", map.get("qq"));
			map.remove("qq");
		}
		if(map.containsKey("notes")){
			map.put("note", map.get("notes"));
			map.remove("notes");
		}
		if(map.containsKey("lgphone")){
			map.put("lgMobilePhone", map.get("lgphone"));
			map.remove("lgphone");
		}
		if(map.containsKey("lgWork")){
			map.put("lgCompany", map.get("lgWork"));
			map.remove("lgWork");
		}
		return map;
	}
	
	
	
}

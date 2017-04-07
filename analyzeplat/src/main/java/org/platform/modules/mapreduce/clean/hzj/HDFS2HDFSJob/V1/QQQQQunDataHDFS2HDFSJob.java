package org.platform.modules.mapreduce.clean.hzj.HDFS2HDFSJob.V1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.util.ToolRunner;
import org.platform.modules.mapreduce.base.BaseHDFS2HDFSJob;
import org.platform.modules.mapreduce.base.BaseHDFS2HDFSV1Mapper;
import org.platform.modules.mapreduce.clean.util.CleanUtil;

public class QQQQQunDataHDFS2HDFSJob extends BaseHDFS2HDFSJob  {

	@Override
	public Class<? extends BaseHDFS2HDFSV1Mapper> getMapperClass() {
		// TODO Auto-generated method stub
		return QQQunDataHDFS2HDFSMapper.class;
	}
	public static void main(String[] args) {
		try {
			int exitCode = 0;
			String n;

			for (int j = 1; j <= 1; j++) {
				for (int i = 0; i <= 11; i++) {
					if (i < 10) {
						n = "0" + i;
					} else {
						n = "" + i;
					}
					args = new String[] {"qqqundata",
							"hdfs://192.168.0.10:9000/elasticsearch_original/qq/qqqundata/20/records-" +j 
							+ "-m-000" + n,
							"hdfs://192.168.0.10:9000/elasticsearch_clean_1/qq/qqqundata/20/records-" + j 
							+ "-m-000" + n };
					exitCode = ToolRunner.run(new QQQQQunDataHDFS2HDFSJob(), args);
				}
			}
			/*args = new String[] {"qqqundata",
					"hdfs://192.168.0.115:9000/elasticsearch/qq/qqqundata/105/records-2-m-00000",
					"hdfs://192.168.0.115:9000/elasticsearch_clean/qq/qqqundata/105/records-2-m-00000"};
			exitCode = ToolRunner.run(new QQQunDataHDFS2HDFSJob(), args);*/
			
			
			
			System.exit(exitCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
class QQQunDataHDFS2HDFSMapper extends BaseHDFS2HDFSV1Mapper {

	@Override
	public void handle(Map<String, Object> original, Map<String, Object> correct, Map<String, Object> incorrect) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.putAll(original);
		original = CleanUtil.replaceSpace(original);
		int size = 0;
		boolean flag = false;
		/* 对QQ群号进行验证 */
		if (original.containsKey("qunNum")) {
			String qunNum = (String) original.get("qunNum");
			if (CleanUtil.matchNum(qunNum)) {
				cleanFiled(original, qunNum, CleanUtil.numRex, "qunNum");
				flag = true;
				size++;
			}else{
					judge(qunNum, "qunNum", original);
			}
		}
		
		 if (original.containsKey("title")) {
				String title = (String) original.get("title");
				if(StringUtils.isNotBlank(title) || !"NA".equals(title)|| !"NULL".equals(title)){
					size++;
				}
		 }

		for (Entry<String, Object> entry : original.entrySet()) {
			if("_id".equals(entry.getKey())||"insertTime".equals(entry.getKey())
					||"updateTime".equals(entry.getKey())||"sourceFile".equals(entry.getKey())){
				flag = true;
			}
		}
		if (original.size()<size+1) {
			flag = false;
		}

		 // 有正确的主要字段，且包含其它字段时为正确数据
		if (flag) {
			if(!"NA".equals((String)original.get("qunNum"))&&original.containsKey("qunNum")){
				correct.putAll(original);
			} else {
				incorrect.putAll(map);
			}
	   }
	}

	/**
	 * 清洗群账号
	 */
	public void cleanFiled(Map<String, Object> original, 
			String Filed, String Match, String FiledName) {
		Pattern pat = Pattern.compile(Match);
		Matcher M = pat.matcher(Filed);
		List<Object> listFiled = new ArrayList<Object>();
		while (M.find()) {
			listFiled.add(M.group()); // 将正确的数据放入集合里面
		}
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < listFiled.size(); i++) {
			if (i == 0) {
				buffer.append(listFiled.get(i)); // 将正确的数据放到一个字符串中
			} else {
				buffer.append("," + listFiled.get(i));
			}
		}
		String toBuffer = buffer.toString();
		String supersession = Filed.replaceAll(Match, ""); // 将不匹配的元素取出来
		if (StringUtils.isNotBlank(supersession)&&!"NA".equals(supersession)
				&&!"NULL".equals(supersession)) {
			if (original.containsKey("cnote")) {
				String cnote = ((String) original.get("cnote")).equals("NA") 
						? "" : (String) original.get("cnote");
				original.put("cnote", cnote + supersession);
			} else {
				original.put("cnote", supersession);
			}
		}
		original.put(FiledName, toBuffer);
	}

	/**
	 * 将废值替换成NA
	 */
	public void judge(String field, String fieldName, Map<String, Object> original) {
		if (!"NA".equals(field) && StringUtils.isNotBlank(field)&&!"NULL".equals(field)) {
			if (original.containsKey("cnote")) {
				// 如果cnote中的值为"NA"，则要将其换位空字符串
				String address = ((String) original.get("cnote")).equals("NA") 
						? "" : (String) original.get("cnote");
				original.put("cnote", address + field);
			} else {
				original.put("cnote", field);
			}
		}
		original.put(fieldName, "NA");
	}
}
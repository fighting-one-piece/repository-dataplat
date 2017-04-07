package org.platform.modules.mapreduce.clean.ly.hdfs2es.v1;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.platform.modules.mapreduce.base.BaseHDFS2ESV1Job;
import org.platform.modules.mapreduce.clean.util.HDFSUtils;

public class WorkStudentHDFS2ESV1Job  extends BaseHDFS2ESV1Job{
	public static void main(String[] args) {
		if(args.length!=2){
			System.out.println("请指定输入和输出路径");
			System.exit(0);
		}
		try {
			List<String> filePathList = new ArrayList<String>();
			String regex = "^(correct)+.*$";
			Path inputPath = new Path(args[0]);
			//遍历给定文件
			HDFSUtils.readAllFiles(inputPath, regex, filePathList);
			StringBuilder sb = new StringBuilder();
			for (int i = 0, len = filePathList.size(); i < len; i++) {
				sb.append(filePathList.get(i)).append(",");
				if (i>0 && (i % 5 == 0 || i == (len - 1))) {
					sb.deleteCharAt(sb.length() - 1);
					String[]  args1= new String[] { "work", "student", "cisiondata", 
							"192.168.0.10,192.168.0.12", "8000",sb.toString(), args[1] + i};
					System.out.println("下标： "+i);
					System.out.println("传入的路径--------->>>"+sb.toString());
					System.out.println("文件输出路径------->>>"+args[1]+i);
					ToolRunner.run(new WorkStudentHDFS2ESV1Job(),args1);
					sb = new StringBuilder();
				}
			}
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
	
	


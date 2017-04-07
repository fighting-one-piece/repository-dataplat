package org.platform.modules.mapreduce.clean.ly.hdfs2hdfs.v2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.util.ToolRunner;
import org.platform.modules.mapreduce.base.BaseHDFS2HDFSJob;
import org.platform.modules.mapreduce.base.BaseHDFS2HDFSMapper;
import org.platform.modules.mapreduce.base.BaseHDFS2HDFSV2Mapper;

public class AccountjdHDFS2HDFSV2 extends BaseHDFS2HDFSJob {

	@Override
	public Class<? extends BaseHDFS2HDFSMapper> getMapperClass() {
		return AccountjdHDFS2HDFSV2Mapper.class;
	}

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		if (args.length != 2) {
			System.out.println("参数不正确");
			System.exit(0);
		}
		String fss = args[0];
		Configuration conf = new Configuration();
		try {
			FileSystem hdfs = FileSystem.get(URI.create(fss), conf);
			FileStatus[] fs = hdfs.listStatus(new Path(fss));
			Path[] listPath = FileUtil.stat2Paths(fs);
			int exitCode = 0;
			// metadata属性
			String line = null;
			for (Path p : listPath) {
				if (!p.getName().equals("_SUCCESS")
						&& !p.getName().equals("part-r-00000")) {
					System.out.println("传入的路径:" + p);
					line = readFirstLine(hdfs, p);
					if (line != null) {
						System.out.println("----------------->>>>>metadata属性："
								+ line);
						String[] args1 = new String[] { line, p.toString(),
								args[1] + "/" + p.getName() };
						System.out.println("执行文件--------------->>>>>>>>>>>>>"
								+ p.getName());
						exitCode = ToolRunner.run(
								new AccountjdHDFS2HDFSV2(), args1);
					}
				}
			}
			long endTime = System.currentTimeMillis();
			SimpleDateFormat formatter = new SimpleDateFormat("mm分ss秒");
			Date date = new Date(endTime - startTime);
			System.out.println("用时--------->>>" + formatter.format(date));
			System.exit(exitCode);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 根据传入的路径读取hadoop文件的第一行，并指定分隔符,返回一个字符串
	public static String readFirstLine(FileSystem hdfs, Path path)
			throws IOException {
		BufferedReader br = null;
		String line = null;
		try {
			FSDataInputStream fin = hdfs.open(path);
			br = new BufferedReader(new InputStreamReader(fin, "UTF-8"));
			if ((line = br.readLine()) != null) {
				return line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				br.close();
			}
		}
		return null;
	}

}

class AccountjdHDFS2HDFSV2Mapper extends BaseHDFS2HDFSV2Mapper {

	@Override
	public void handle(Map<String, Object> original,
			Map<String, Object> correct, Map<String, Object> incorrect) {
		correct.putAll(original);
	}
}
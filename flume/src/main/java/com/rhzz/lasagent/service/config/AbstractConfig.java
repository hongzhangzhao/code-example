package com.rhzz.lasagent.service.config;

import java.io.*;
import java.util.Properties;

/**
 * @author WangAo 加载 Flume 配置，用于运行时修改配置。
 */
public abstract class AbstractConfig {

	// confPath : 配置文件路径
	protected static String confPath;
	// properties : 配置文件导出的属性
	protected Properties properties = new Properties();

	public AbstractConfig(String confPath) {
		AbstractConfig.confPath = confPath;
		try (FileInputStream fis = new FileInputStream(new File(confPath));
				InputStreamReader isr = new InputStreamReader(fis, "utf-8");
				BufferedReader br = new BufferedReader(isr);) {
			this.properties.load(br);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * @Title: updateKey
	 * @Description: 属性更新某个 key 的 value
	 * @param key
	 *            被更新的 key
	 * @param value
	 *            被更新的 key 的 value
	 * @return: void
	 */
	public void updateKey(String key, String value) {
		this.properties.setProperty(key, value);
	}

	/**
	 * @Title: appendKey
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @return: void
	 */
	public void appendKey(String key, String value) {
		this.properties.setProperty(key, value + " " + this.properties.getProperty(key));
	}

	/**
	 * @Title: store
	 * @Description: 把配置属性加载到指定文件里
	 * @param path
	 *            指定文件路径
	 * @return: void
	 */
	public void store(String path) {
		try (FileOutputStream fos = new FileOutputStream(new File(path));
				OutputStreamWriter osw = new OutputStreamWriter(fos, "utf-8");
				BufferedWriter bw = new BufferedWriter(osw);) {
			this.properties.store(bw, "");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @Title: reload
	 * @Description: TODO
	 * @return: void
	 */
	public abstract void reload();

}

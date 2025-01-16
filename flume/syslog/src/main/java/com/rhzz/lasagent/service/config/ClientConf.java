package com.rhzz.lasagent.service.config;

import java.util.Map;

public class ClientConf extends AbstractConfig {
	public ClientConf(String confPath) {
		super(confPath);
	}

	/**
	 * @Title: modify
	 * @Description: 更改 flume 配置文件
	 * @param key
	 * @param value
	 * @param append
	 * @return: void
	 */
	public void modify(String key, String value, String append) {
		for (Map.Entry<Object, Object> entry : properties.entrySet()) {
			String field = (String) entry.getKey();
			if (field.contains(key)) {
				if (append.equals("false"))
					updateKey(field, value);
				else
					appendKey(field, value);
				return;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.rhzz.lasagent.service.config.AbstractConfig#reload()
	 */
	@Override
	public void reload() {
		// TODO Auto-generated method stub
		store(confPath);
	}
}

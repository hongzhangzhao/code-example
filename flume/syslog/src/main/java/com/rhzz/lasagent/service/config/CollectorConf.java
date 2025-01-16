package com.rhzz.lasagent.service.config;

/**
 * @author WangAo 需要动态改变的属性： collector.sources.avroSrc.port
 *         collector.sources.avroSrc.keystore
 *         collector.sources.avroSrc.keystore-password
 *         collector.sinks.EsSink.hostNames
 *         collector.sinks.CollectorSink.truststore
 *         collector.sinks.CollectorSink.truststore-password
 *         collector.sources.avroSrc.clients
 */
public class CollectorConf extends AbstractConfig {

	private final static String confPath = CollectorConf.class.getClassLoader().getResource("TailAvro.conf").getPath();

	public CollectorConf(String confPath) {
		// TODO Auto-generated constructor stub
		super(confPath);
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		ClientConf clientConf = new ClientConf(confPath);
		clientConf.modify("filename", "Linux-Operation.xml", "true");
		clientConf.reload();
	}

	@Override
	public void reload() {
		// TODO Auto-generated method stub

	}

}

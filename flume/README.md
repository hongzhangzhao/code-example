# code-example

- 启动 Flume
    - /opt/flume/bin/flume-ng agent -c conf -f /opt/flume/conf/customSyslog.conf -n collector -Dflume.root.logger=DEBUG,console
    - /opt/flume/bin/flume-ng agent -c conf -f /opt/flume/conf/customSyslog.conf -n collector -Dflume.root.logger=INFO,LOGFILE
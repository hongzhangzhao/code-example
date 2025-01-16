package com.rhzz.lasagent.util;

import com.sun.management.OperatingSystemMXBean;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.util.Properties;

public class SystemInfo {
	private static SystemInfo instance = new SystemInfo();

	public static SystemInfo getInstance() {
		return instance;
	}
	/**
	 * @Title: getsysinfo
	 * @Description: 获取系统信息并返回
	 * @param String, boolean
	 * @return: String
	 */
	public String getSysInfo(String addr, boolean suspend) {
		String info = new String();
		Properties props = System.getProperties();

        String osName = props.getProperty("os.name");
        String state;
        if (suspend) state = "suspending";
        else state = "running";
        info = "clientIp&port:" + addr + "\nSourceState:" + state + "\nosName:" + osName + "\nosArchitecture:"+ props.getProperty("os.arch");
    	if (osName.toLowerCase().contains("windows") || osName.toLowerCase().contains("win")) info = info + "\n" + getWindowsInfo();
    	else info = info + "\n" + getLinuxInfo();
		return info;
	}

	private String getLinuxInfo() {
		return getLinuxMemory() + getFlumeMemory() + getLinuxCpuRatio();
	}

	//获取linux系统中flume的内存占用率
	private String getFlumeMemory() {
		String str = new String();
		String cmd = "ps -aux | grep flume | grep -v grep | awk '{print $4}'";
		Process process;
		try {
			process = Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", cmd});
			LineNumberReader br = new LineNumberReader(new InputStreamReader(process.getInputStream()));
			String line;
			line = br.readLine();
			str = "FlumeMemoryUsage:" + line + "%\n";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return str;
	}
	//获取linux系统的内存信息
	private String getLinuxMemory() {
        Process pro = null;
        Runtime r = Runtime.getRuntime();
        String str = new String();
        try {
            String command = "cat /proc/meminfo";
            pro = r.exec(command);
            BufferedReader in = new BufferedReader(new InputStreamReader(pro.getInputStream()));
            String line = null;
            int count = 0;
            long totalMem = 0, freeMem = 0;
            while((line=in.readLine()) != null){
                String[] memInfo = line.split("\\s+");
                if(memInfo[0].startsWith("MemTotal")){
                    totalMem = Long.parseLong(memInfo[1]);
                }
                if(memInfo[0].startsWith("MemFree")){
                    freeMem = Long.parseLong(memInfo[1]);
                }
                if(++count == 2){
                    break;
                }
            }
            Double memUsage = (Double)(1- freeMem *1.0 /totalMem) * 100;
            float remains = (float)Math.round((float)(freeMem)/1024/1024 * 100)/100;
            str = "ResidualPhysicalMemory:" + remains + " GB\n"+ "MemoryUsage:" + memUsage.intValue() + "%\n";
            in.close();
            pro.destroy();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
        }
        return str;
	}
	//获取linux系统的CPU占用率
	private String getLinuxCpuRatio() {
		float cpuUsage = 0;
        Process pro1,pro2;
        Runtime r = Runtime.getRuntime();
        String str = new String();
        try {
            String command = "cat /proc/stat";
            //第一次采集CPU时间
            pro1 = r.exec(command);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(pro1.getInputStream()));
            String line = null;
            long idleCpuTime1 = 0, totalCpuTime1 = 0;   //分别为系统启动后空闲的CPU时间和总的CPU时间
            while((line=in1.readLine()) != null){
                if(line.startsWith("cpu")){
                    line = line.trim();
                    String[] temp = line.split("\\s+");
                    idleCpuTime1 = Long.parseLong(temp[4]);
                    for(String s : temp){
                        if(!s.equals("cpu")){
                            totalCpuTime1 += Long.parseLong(s);
                        }
                    }
                    break;
                }
            }
            in1.close();
            pro1.destroy();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
            }
            //第二次采集CPU时间
            pro2 = r.exec(command);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(pro2.getInputStream()));
            long idleCpuTime2 = 0, totalCpuTime2 = 0;   //分别为系统启动后空闲的CPU时间和总的CPU时间
            while((line=in2.readLine()) != null){
                if(line.startsWith("cpu")){
                    line = line.trim();
                    String[] temp = line.split("\\s+");
                    idleCpuTime2 = Long.parseLong(temp[4]);
                    for(String s : temp){
                        if(!s.equals("cpu")){
                            totalCpuTime2 += Long.parseLong(s);
                        }
                    }
                    break;
                }
            }
            if(idleCpuTime1 != 0 && totalCpuTime1 !=0 && idleCpuTime2 != 0 && totalCpuTime2 !=0){
                cpuUsage = (1 - (float)(idleCpuTime2 - idleCpuTime1)/(float)(totalCpuTime2 - totalCpuTime1))*100;
                str = "CPU_usage:" + cpuUsage + "%\n";
            }else str = "CPU_usage:" + 0 + "%\n";
            in2.close();
            pro2.destroy();
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            str = "CPU_usage:" + 0 + "%\n";
        }
        return str;
	}

	private String getWindowsInfo() {
		return getWindowsMemery() + getWindowsCpuRatio();
	}
	//获取windows系统的内存占用率
	private String getWindowsMemery() {
        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long totalvirtualMemory = osmxb.getTotalSwapSpaceSize();// 总的物理内存+虚拟内存
        long freePhysicalMemorySize = osmxb.getFreePhysicalMemorySize();    // 剩余的物理内存
        Double compare = (Double) (1 - freePhysicalMemorySize * 1.0 / totalvirtualMemory) * 100;
        float remains = (float)Math.round((float)(freePhysicalMemorySize)/1024/1024/1024 * 100)/100;
        String str = "ResidualPhysicalMemory:" + remains + " GB\n"+ "MemoryUsage:" + compare.intValue() + "%\n";
        return str;
    }
	//获取windows系统的CPU占用率
	private String getWindowsCpuRatio() {
        try {
            String procCmd = System.getenv("windir") + "//system32//wbem//wmic.exe process get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";
            long[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));    // 取进程信息
            Thread.sleep(200);
            long[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                long idletime = c1[0] - c0[0];
                long busytime = c1[1] - c0[1];
                return "CPU_usage:"+ Double.valueOf(100 * (busytime) * 1.0 / (busytime + idletime)).intValue() + "%\n";
            } else {
                return "CPU_usage:" + 0 + "%\n";
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "CPU_usage:" + 0 + "%\n";
        }
    }
    private long[] readCpu(final Process proc) {
        long[] retn = new long[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < 10) {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            long idletime = 0;
            long kneltime = 0;
            long usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                String caption = substring(line, capidx, cmdidx - 1).trim();
                String cmd = substring(line, cmdidx, kmtidx - 1).trim();
                if (cmd.indexOf("wmic.exe") >= 0) {
                    continue;
                }
                String s1 = substring(line, kmtidx, rocidx - 1).trim();
                String s2 = substring(line, umtidx, wocidx - 1).trim();
                if (caption.equals("System Idle Process") || caption.equals("System")) {
                    if (s1.length() > 0)
                        idletime += Long.valueOf(s1).longValue();
                    if (s2.length() > 0)
                        idletime += Long.valueOf(s2).longValue();
                    continue;
                }
                if (s1.length() > 0)
                    kneltime += Long.valueOf(s1).longValue();
                if (s2.length() > 0)
                    usertime += Long.valueOf(s2).longValue();
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private String substring(String src, int start_idx, int end_idx) {
        byte[] b = src.getBytes();
        String tgt = "";
        for (int i = start_idx; i <= end_idx; i++) {
            tgt += (char) b[i];
        }
        return tgt;
    }
}

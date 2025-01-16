package com.rhzz.lasagent.service.config;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class DateForm {
		private static DateForm dateFormat =new DateForm();
		public  SimpleDateFormat sdf1;
		public  SimpleDateFormat sdf2;
		public  SimpleDateFormat sdf3;
		private DateForm()
		{
			sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			sdf1.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
			sdf2 = new SimpleDateFormat("yyyyMMdd");
			sdf2.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
			sdf3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf3.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
		}
		public static DateForm getInstance()
		{
			return dateFormat;
		}

}

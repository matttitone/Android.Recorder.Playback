package com.teamsix.recorddemo.recorddemo;

import com.qiniu.api.auth.AuthException;
import com.qiniu.api.auth.digest.Mac;
import com.qiniu.api.rs.PutPolicy;

import org.json.JSONException;

/**
 * Created by lixiaodaoaaa on 14/10/12.
 */
public final class QiNiuConfig {
	public static final String token = getToken();
	public static final String QINIU_AK = "bQyZA5cS3wzOBEUG-i-ELgXO7X88MsNnw2lgsJKH";
	public static final String QINIU_SK = "p5RdjIFr5D4Io-GW0LJMAlMGKWOGZEhGrU3IYzIS";
	public static final String QINIU_BUCKNAME = "recordbackup";

	public static String getToken() {

		Mac mac = new Mac(QiNiuConfig.QINIU_AK, QiNiuConfig.QINIU_SK);
		PutPolicy putPolicy = new PutPolicy(QiNiuConfig.QINIU_BUCKNAME);
		putPolicy.returnBody = "{\"name\": $(fname),\"size\": \"$(fsize)\",\"w\": \"$(imageInfo.width)\",\"h\": \"$(imageInfo.height)\",\"key\":$(etag)}";
		try {
			String uptoken = putPolicy.token(mac);
			System.out.println("debug:uptoken = " + uptoken);
			return uptoken;
		} catch (AuthException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}

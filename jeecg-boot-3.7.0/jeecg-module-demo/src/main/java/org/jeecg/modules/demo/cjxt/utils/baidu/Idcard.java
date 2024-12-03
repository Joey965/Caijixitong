package org.jeecg.modules.demo.cjxt.utils.baidu;


import com.alibaba.fastjson.JSONObject;
import org.apache.commons.compress.utils.IOUtils;
import org.jeecg.modules.demo.cjxt.utils.log.Dg;
import org.jeewx.api.core.common.util.WeixinUtil;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 身份证识别
 */
public class Idcard {

	/**
	 * 重要提示代码中所需工具类
	 * FileUtil,Base64Util,HttpUtil,GsonUtils请从
	 * https://ai.baidu.com/file/658A35ABAB2D404FBF903F64D47C1F72
	 * https://ai.baidu.com/file/C8D81F3301E24D2892968F09AE1AD6E2
	 * https://ai.baidu.com/file/544D677F5D4E4F17B4122FBD60DB82B3
	 * https://ai.baidu.com/file/470B3ACCA3FE43788B5A963BF0B625F3
	 * 下载
	 */
	public static String idcard() {
		// 请求url
		String url = "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard";
		try {
			// 本地文件路径
			String filePath = "F:\\temp\\tp\\rzbd\\Front.bmp";
			byte[] imgData = FileUtil.readFileByBytes(filePath);
			String imgStr = Base64Util.encode(imgData);
			String imgParam = URLEncoder.encode(imgStr, "UTF-8");

			String param = "id_card_side=" + "front" + "&image=" + imgParam;

			// 注意这里仅为了简化编码每一次请求都去获取access_token，线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
			String accessToken = AuthService.getAuth();

			String result = HttpUtil.post(url, accessToken, param);
			System.out.println(result);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Map<String, String> jxSfzUrl(String sfzImgUrl) {
		Map<String, String> map = new HashMap<>();
		// 身份证识别url
		String idcardIdentificate = "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard";
		try {
			URL url = new URL(sfzImgUrl);
			URLConnection connection = url.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream in = connection.getInputStream();
			byte[] imgData = IOUtils.toByteArray(in);
			String imgStr = Base64Util.encode(imgData);
			long startTime = System.currentTimeMillis();
			long sqlCostTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();
			// 识别身份证正面id_card_side=front;识别身份证背面id_card_side=back;
			String params = "id_card_side=front&" + URLEncoder.encode("image", "UTF-8") + "="
					+ URLEncoder.encode(imgStr, "UTF-8");
			/**
			 * 线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
			 */
			String accessToken = AuthService.getAuth();//#####调用鉴权接口获取的token#####
			String value = HttpUtil.post(idcardIdentificate, accessToken, params);
			JSONObject jsonObject = JSONObject.parseObject(value);
			JSONObject words_result = jsonObject.getJSONObject("words_result");
			if (words_result != null) {
				for (String key : words_result.keySet()) {
					JSONObject result = words_result.getJSONObject(key);
					String info = result.getString("words");
					switch (key) {
						case "姓名":
							map.put("xm", info);
							break;
						case "公民身份号码":
							map.put("sfzh", info);
							break;
						case "性别":
							map.put("sex", info);
							break;
						case "民族":
							map.put("nation", info);
							break;
						case "出生":
							map.put("birthday", info);
							break;
						case "住址":
							map.put("address", info);
							break;
						case "签发机关":
							map.put("issuedOrganization", info);
							break;
						case "签发日期":
							map.put("issuedAt", info);
							break;
						case "失效日期":
							map.put("expiredAt", info);
							break;
					}
				}
				endTime = System.currentTimeMillis();
				sqlCostTime = endTime - startTime;
				Dg.writeContent("baidu_suc", "开始时间：" + startTime + ",结束时间：" + endTime + ",共消耗：" + sqlCostTime + "开始百度ocr识别，百度成功了| "+map.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}


	public static Map<String, String> jxSfzPath(String sfzImgPath) {
		Map<String, String> map = new HashMap<>();
		// 身份证识别url
		String idcardIdentificate = "https://aip.baidubce.com/rest/2.0/ocr/v1/idcard";
		// 本地图片路径
		String filePath = sfzImgPath;//#####本地文件路径#####
		try {
			byte[] imgData = FileUtil.readFileByBytes(filePath);
			String imgStr = Base64Util.encode(imgData);
			long startTime = System.currentTimeMillis();
			long sqlCostTime = System.currentTimeMillis();
			long endTime = System.currentTimeMillis();
				// 识别身份证正面id_card_side=front;识别身份证背面id_card_side=back;
				String params = "id_card_side=front&" + URLEncoder.encode("image", "UTF-8") + "="
						+ URLEncoder.encode(imgStr, "UTF-8");
				/**
				 * 线上环境access_token有过期时间， 客户端可自行缓存，过期后重新获取。
				 */
				String accessToken = AuthService.getAuth();//#####调用鉴权接口获取的token#####
				String value = HttpUtil.post(idcardIdentificate, accessToken, params);
				JSONObject jsonObject = JSONObject.parseObject(value);
				JSONObject words_result = jsonObject.getJSONObject("words_result");
				if (words_result != null) {
					for (String key : words_result.keySet()) {
						JSONObject result = words_result.getJSONObject(key);
						String info = result.getString("words");
						switch (key) {
							case "姓名":
								map.put("xm", info);
								break;
							case "公民身份号码":
								map.put("sfzh", info);
								break;
							case "性别":
								map.put("sex", info);
								break;
							case "民族":
								map.put("nation", info);
								break;
							case "出生":
								map.put("birthday", info);
								break;
							case "住址":
								map.put("address", info);
								break;
							case "签发机关":
								map.put("issuedOrganization", info);
								break;
							case "签发日期":
								map.put("issuedAt", info);
								break;
							case "失效日期":
								map.put("expiredAt", info);
								break;
						}
						//System.out.println(info);
					}
					endTime = System.currentTimeMillis();
					sqlCostTime = endTime - startTime;
 					Dg.writeContent("baidu_suc", "开始时间：" + startTime + ",结束时间：" + endTime + ",共消耗：" + sqlCostTime + "开始百度ocr识别，百度成功了| "+map.toString());
				}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}

	public static void main(String[] args) {
//		Idcard.jxSfzPath("F:\\temp\\tp\\rzbd\\sfz.jpg");
		String url = "https://fj.snby.net/cjxtdata/ocr/tmp_509bffaf36f16e7dfecab3417d572ec8_1728377271175.jpg&tplx=1&uploadT=1";
		url = url.split("&")[0];
		System.out.println("输出url"+url);
		Idcard.jxSfzUrl(url);
	}
}

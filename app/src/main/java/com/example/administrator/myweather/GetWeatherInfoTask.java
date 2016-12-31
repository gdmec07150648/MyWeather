package com.example.administrator.myweather;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/12.
 */

public class GetWeatherInfoTask extends AsyncTask<String, Void, List<Map<String, Object>>>{
    // activity的上下文
    private Activity context;
    // 加载提示窗口
    private ProgressDialog progressDialog;
    // 错误信息
    private String errorMsg = "网络错误!!";
    // 天气信息列表
    private ListView weather_info;
    // 网络请求的基础URL
    private static String BASE_URL = "http://v.juhe.cn/weather/index?format=2&cityname=";
    private static String key = "&key=69181425665c499433324d8512821edf";
    // 网络访问时的进度对话框
    public GetWeatherInfoTask(Activity context){
        this.context = context;
        // 获取天气时的提示框
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("正在获取天气，请稍后...");
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(List<Map<String, Object>> maps) {
        super.onPostExecute(maps);
        progressDialog.dismiss();
        if (maps.size() > 0) {
            weather_info = (ListView) context.findViewById(R.id.weather_info);
            // 更新天气列表
            SimpleAdapter simpleAdapter = new SimpleAdapter(context, maps,
                    R.layout.weather_item, new String[] { "temperature",
            "weather", "date", "week", "weather_icon" }, new int[] {
                    R.id.temperature, R.id.weather, R.id.date, R.id.week,
            R.id.weather_icon });
            weather_info.setAdapter(simpleAdapter);
        } else {
            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected List<Map<String, Object>> doInBackground(String... params) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        try {
            // 连接网络
            HttpClient httpClient = new DefaultHttpClient();
            // 创建访问url， 并把参数转码
            String url = BASE_URL + URLEncoder.encode(params[0], "utf-8") + key;
            // 创建HttpGet对象
            HttpGet httpGet = new HttpGet(url);
            // HttpClient执行HttpGet， 获取response
            HttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                //如果服务器设置了gzip压缩的话， 首先要设置gzip流解码
                //response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                // 获得JSON字符串
                String jsonString = EntityUtils.toString(response.getEntity(), "utf-8");
                JSONObject jsonData = new JSONObject(jsonString);
                if (jsonData.getInt("resultcode") == 200) {
                    JSONObject result = jsonData.getJSONObject("result");
                    JSONArray weatherList = result.getJSONArray("future");
                    for (int i = 0; i < 7; i++) {
                        Map<String, Object> item = new HashMap<String, Object>();
                        JSONObject weatherObject = weatherList.getJSONObject(i);
                        // 获得温度
                        item.put("temperature", weatherObject.getString("temperature"));
                        // 获得天气类型
                        item.put("weather", weatherObject.getString("weather"));
                        // 获得日期
                        item.put("date", weatherObject.getString("date"));
                        // 获得星期
                        item.put("week", weatherObject.getString("week"));
                        // 获得风向风力
                        item.put("wind", weatherObject.getString("wind"));
                        // 获得天气图标标号
                        JSONObject wid = weatherObject.getJSONObject("weather_id");
                        int weather_icon = wid.getInt("fa");
                        // 获得对应编号的天气图标
                        item.put("weather_icon", WeatherIcon.weather_icons[weather_icon]);
                        list.add(item);
                    }
                } else {
                    errorMsg = "非常抱歉, 本应用暂不支持您所请求的城市!!";
                }
            } else {
                errorMsg = "网络错误, 请检查手机是否开启了网络!!";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}

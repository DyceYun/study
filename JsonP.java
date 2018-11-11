package com;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

/**
 * <p>
 * <br>
 * @author Wanys
 * @version 1.0
 * @createTime 2018/11/11
 * @ChangeLog
 */
public class JsonP {
    private static final Logger LOG = LoggerFactory.getLogger(JsonP.class);

    public static void main(String[] args) {
        readFile();
    }

    public static void readFile() {
        JSON json1 = readJsonFile(new File(JsonP.class.getClassLoader().getResource("com/config.json").getPath()));
        JSON json2 = readJsonFile(new File(JsonP.class.getClassLoader().getResource("com/configlast.json").getPath()));

        Data data = new Data("config.json", Status.UPDATE);
        compareJson(json1, json2, "", data);
        System.out.println(data);
    }

    public static void compareJson(JSON source, JSON target, String key, Data data) {
        if (source.getClass() != target.getClass()) {
            delJson(source, key, data);
            addJson(target, key, data);
            return;
        }
        if (source instanceof JSONObject) {
            JSONObject sourceObj = (JSONObject) source;
            JSONObject targetObj = (JSONObject) target;
            //对比json对象
            sourceObj.forEach((k, v) -> {
                if (targetObj.containsKey(k)) {
                    Object value = targetObj.get(k);
                    if (!v.equals(value)) {
                        if ((v instanceof JSON) && (value instanceof JSON)) {
                            compareJson((JSON) v, (JSON) value, key + k + ".", data);
                        } else {
                            //更新元素细分
                            data.get(Status.UPDATE).put(key + k, value);
                        }
                    }
                    targetObj.remove(k);
                } else {
                    data.get(Status.DELETE).put(key + k, v);
                }
            });
            targetObj.forEach((k, v) ->
                    data.get(Status.CREATE).put(key + k, v)
            );
        } else {
            String localKey = key.endsWith(".") ? key.substring(0, key.length() - 1) : key;
            JSONArray sourceArr = (JSONArray) source;
            JSONArray targetArr = (JSONArray) target;

            //去除容易判断的不变修改项
            Iterator<Object> iterator = sourceArr.iterator();
            while (iterator.hasNext()) {
                Object o = iterator.next();
                if (targetArr.contains(o)) {
                    //优化判断
                    iterator.remove();
                    targetArr.remove(o);
                    continue;
                }

            }

            //无法判断的修改均视为新增删除
            data.get(Status.DELETE).put(localKey, sourceArr);
            data.get(Status.CREATE).put(localKey, targetArr);
        }

    }

    private static void addJson(JSON target, String key, Data data) {
    }

    private static void delJson(JSON source, String key, Data data) {
    }


    public static JSON readJsonFile(File file) {
        JSON json = null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuffer sb = new StringBuffer();
            String str;
            while ((str = reader.readLine()) != null) {
                sb.append(str);
            }
            json = (JSON) JSON.parse(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("读取文件失败，路径： {}", file.getPath());
        }
        return json;
    }
}

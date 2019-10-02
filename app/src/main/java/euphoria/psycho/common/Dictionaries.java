package euphoria.psycho.common;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
public class Dictionaries extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static Dictionaries sDictionaries;
    public Dictionaries(Context context, String name) {
        super(context, name, null, DATABASE_VERSION);
    }
    private void insert(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", key);
        contentValues.put("word", value);
        getWritableDatabase().insert("dic", null, contentValues);
    }
    private void insertEnglish(String key, String value) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("key", key);
        contentValues.put("word", value);
        getWritableDatabase().insertWithOnConflict("eng_dic", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
    }
    private String query(String key) {
        if (key == null) return null;
        Cursor cursor = getReadableDatabase().rawQuery("select word from dic where key = ?", new String[]{key});
        String result = null;
        if (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }
    public String queryCollegiate(String word) {
        String value = queryEnglish(word);
        if (value == null) {
            value = collegiate(word);
        }
        if (value != null) {
            insertEnglish(word, value);
        }
        return value;
    }
    public String queryDictionary(String word) {
        String youdao = queryYouDao(word);
//        String collegiate = queryCollegiate(word);
//        if (collegiate != null) {
//            youdao = youdao.concat("\n").concat(collegiate);
//
//        }
        return youdao;
    }
    private String queryEnglish(String key) {
        if (key == null) return null;
        Cursor cursor = getReadableDatabase().rawQuery("select word from eng_dic where key = ?", new String[]{key});
        String result = null;
        if (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        cursor.close();
        return result;
    }
    public String queryYouDao(String word) {
        String value = query(word);
        if (value == null) {
            try {
                value = youdao(word);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (value != null) {
            insert(word, value);
        }
        return value;
    }
    private static String collegiate(String word) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(
                    String.format("http://www.dictionaryapi.com/api/v1/references/collegiate/xml/%s?key=2cdcdd90-9e02-426d-ae02-ef2cbbef0b02",
                            word));
            connection = (HttpURLConnection) url.openConnection();
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null)
                sb.append(line).append('\n');
            reader.close();
            Matcher m = Pattern.compile("(?<=<dt>)[\\s\\S]*?(?=</dt>)").matcher(sb.toString());
            sb.setLength(0);
            while (m.find()) {
                sb.append(
                        m.group().replaceAll("<.*?>", " ")
                                .replaceAll("\\s{2,}", " ")
                                .replaceFirst(":\\s+", ":")
                                .trim()).append('\n');
            }
            return sb.toString();
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (connection != null)
                connection.disconnect();
        }
        return null;
    }
    private static String youdao(String word) throws JSONException {
        String json = touch(generateRequestUrl(word), "GET", null, null);
        return extractJSON(new JSONObject(json), word, false);
    }
    public static String extractJSON(JSONObject js, String word, boolean onlyTranslation) {
        StringBuilder builder = new StringBuilder();
        if (js.has("basic") && !onlyTranslation) {
            try {
                JSONObject basic = js.getJSONObject("basic");
                if (basic.has("us-phonetic")) {
                    builder.append('/').append(basic.getString("us-phonetic")).append('/');
                } else if (basic.has("phonetic")) {
                    builder.append('/').append(basic.getString("phonetic")).append('/');
                }
                if (basic.has("explains")) {
                    JSONArray explains = basic.getJSONArray("explains");
                    for (int i = 0; i < explains.length(); i++) {
                        builder.append(explains.getString(i)).append(';');
                    }
                    builder.append('\n');
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (js.has("translation")) {
            try {
                JSONArray translation = js.getJSONArray("translation");
                if (translation.length() == 1 && translation.getString(0).equalsIgnoreCase(word)) {
                } else {
                    for (int i = 0; i < translation.length(); i++) {
                        builder.append(translation.getString(i));
                    }
                    builder.append('\n');
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (js.has("web") && !onlyTranslation) {
            try {
                JSONArray web = js.getJSONArray("web");
                for (int i = 0; i < web.length(); i++) {
                    JSONObject j = web.getJSONObject(i);
                    builder.append(j.getString("key").toLowerCase()).append(":");
                    JSONArray ja = j.getJSONArray("value");
                    for (int k = 0; k < ja.length(); k++) {
                        builder.append(ja.getString(k)).append(';');
                    }
                    builder.append('\n');
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return builder.toString();
    }
    private static String encode(String input) {
        if (input == null) {
            return "";
        }
        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return input;
    }
    public static String generateRequestUrl(String query) {
//        final Pattern pattern = Pattern.compile("[\u4E00-\u9FA5]");
//        Matcher matcher = pattern.matcher(query);
        String appKey = "1f5687b5a6b94361";
        String salt = String.valueOf(System.currentTimeMillis());
        String from = "";
        from = "EN";
        String to = "";
        to = "zh-CHS";
        String sign = md5(appKey + query + salt + "2433z6GPFslGhUuQltdWP7CPlbk8NZC0");
        Map<String, String> params = new HashMap<>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);
        params.put("sign", sign);
        params.put("salt", salt);
        params.put("appKey", appKey);
        return getUrlWithQueryString("http://openapi.youdao.com/api", params);
    }
    private static String getUrlWithQueryString(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }
        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }
        int i = 0;
        for (String key : params.keySet()) {
            String value = params.get(key);
            if (value == null) { // 过滤空的key
                continue;
            }
            if (i != 0) {
                builder.append('&');
            }
            builder.append(key);
            builder.append('=');
            builder.append(encode(value));
            i++;
        }
        return builder.toString();
    }
    public static Dictionaries instance() {
        return sDictionaries;
    }
    private static String md5(String string) {
        if (string == null) {
            return null;
        }
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        try {
            byte[] btInput = string.getBytes("utf-8");
            /** 获得MD5摘要算法的 MessageDigest 对象 */
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            /** 使用指定的字节更新摘要 */
            mdInst.update(btInput);
            /** 获得密文 */
            byte[] md = mdInst.digest();
            /** 把密文转换成十六进制的字符串形式 */
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return null;
        }
    }
    public static Dictionaries newInstance(Context context, String name) {
        if (sDictionaries == null) {
            sDictionaries = new Dictionaries(context, name);
        }
        return sDictionaries;
    }
    public static String touch(String url, String method, String accessToken, String jsonBody) {
        HttpURLConnection connection = null;
        URL u;
        try {
            u = new URL(url);
        } catch (MalformedURLException e) {
            return e.getMessage();
        }
        try {
            connection = (HttpURLConnection) u.openConnection();
            if (jsonBody != null) {
                connection.setRequestMethod("POST");
            } else {
                connection.setRequestMethod(method);
            }
            if (accessToken != null)
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
            connection.setConnectTimeout(5 * 1000);
            connection.setReadTimeout(5 * 1000);
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
            connection.setRequestProperty("Cache-Control", "max-age=0");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.56 Mobile Safari/537.36");
            if (jsonBody != null) {
                connection.addRequestProperty("Content-Type", "application/json");
                connection.addRequestProperty("Content-Encoding", "gzip");
                GZIPOutputStream outGZIP;
                outGZIP = new GZIPOutputStream(connection.getOutputStream());
                byte[] body = jsonBody.getBytes("utf-8");
                outGZIP.write(body, 0, body.length);
                outGZIP.close();
            }
            int code = connection.getResponseCode();
            StringBuilder sb = new StringBuilder();
//            sb.append("ResponseCode: ").append(code).append("\r\n");
//
//
//            Set<String> keys = connection.getHeaderFields().keySet();
//            for (String key : keys) {
//                sb.append(key).append(": ").append(connection.getHeaderField(key)).append("\r\n");
//            }
            if (code < 400 && code >= 200) {
                //sb.append("\r\n\r\n");
                InputStream in;
                String contentEncoding = connection.getHeaderField("Content-Encoding");
                if (contentEncoding != null && contentEncoding.equals("gzip")) {
                    in = new GZIPInputStream(connection.getInputStream());
                } else {
                    in = connection.getInputStream();
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(in, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\r\n");
                }
                reader.close();
            } else {
            }
            return sb.toString();
        } catch (IOException e) {
            return e.getMessage();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    // 如果数据库已存在
    // 不会调用此方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE \"eng_dic\" ( \"key\" varchar , \"word\" varchar, \"learned\" INTEGER)");
        db.execSQL("CREATE TABLE \"dic\" ( \"key\" varchar , \"word\" varchar, \"learned\" INTEGER)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `dic_key_UNIQUE` ON `dic` (`key` ASC)");
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `eng_dic_key_UNIQUE` ON `eng_dic` (`key` ASC)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
package cn.kerison.kcache;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import cn.kerison.cache.KFiles;
import cn.kerison.cache.KPrefs;
import cn.kerison.kit.log.KL;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        KPrefs.init(this);
        Set<String> data = new HashSet<>();
        data.add("item1");
        data.add("item2");
        KPrefs.getEditor()
                .putBoolean("yn", true)
                .putString("text","Hello World!")
                .putStringSet("textSet",data)
                .commit();

        KL.i("KPrefs: %s - %s - %s", KPrefs.getBoolean("yn"), KPrefs.getString("text"), KPrefs.getStringSet("textSet").toString());


        KFiles.init(this, "KCaches");
        JSONObject json = new JSONObject();
        try {
            json.put("name", "Hehe");
            json.put("age", 1000);
            json.put("date", new Date());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        KFiles.putJSONObject("json", json, 5);
        KL.i("KFiles: %s", KFiles.getJSONObject("json")+"");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        KL.i("KFiles: %s", KFiles.getJSONObject("json")+"");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        KL.i("KFiles: %s", KFiles.getJSONObject("json")+"");
    }
}

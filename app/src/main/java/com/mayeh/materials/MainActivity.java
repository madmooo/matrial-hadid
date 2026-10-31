package com.mayeh.materials;
import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.Cursor;
import android.graphics.Color;
import android.webkit.*;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
public class MainActivity extends Activity {
 private WebView web; private SQLiteDatabase db; private String pending;
 @Override public void onCreate(Bundle state){
  super.onCreate(state); db=openOrCreateDatabase("mayeh.db",MODE_PRIVATE,null);
  db.execSQL("CREATE TABLE IF NOT EXISTS state(id INTEGER PRIMARY KEY CHECK(id=1), json TEXT NOT NULL)");
  getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);
  web=new WebView(this);web.setBackgroundColor(Color.BLACK);
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(false);s.setAllowFileAccess(false);s.setAllowContentAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){WebResourceResponse out=loader.shouldInterceptRequest(r.getUrl());return out!=null?out:new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return true;}
  });
  web.addJavascriptInterface(new LocalStore(),"MayehNative");
  android.widget.FrameLayout frame=new android.widget.FrameLayout(this);frame.setBackgroundColor(Color.BLACK);frame.addView(web);
  frame.setOnApplyWindowInsetsListener((v,insets)->{
   v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();
  });
  setContentView(frame);web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }
 private synchronized String readState(){try(Cursor c=db.rawQuery("SELECT json FROM state WHERE id=1",null)){return c.moveToFirst()?c.getString(0):"{\"version\":1,\"items\":[],\"prices\":[]}";}}
 private synchronized void writeState(String json)throws Exception{if(json.length()>5000000)throw new Exception();JSONObject o=new JSONObject(json);if(o.getInt("version")!=1)throw new Exception();o.getJSONArray("items");o.getJSONArray("prices");db.execSQL("INSERT OR REPLACE INTO state(id,json) VALUES(1,?)",new Object[]{json});}
 public class LocalStore {
  @JavascriptInterface public String read(){return readState();}
  @JavascriptInterface public String save(String json){try{writeState(json);return "ok";}catch(Exception e){return "Zakhire anjam nashod.";}}
  @JavascriptInterface public void exportBackup(){runOnUiThread(()->{pending=readState();Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.setType("application/json");i.addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_TITLE,"mayeh-backup.json");startActivityForResult(i,10);});}
  @JavascriptInterface public void importBackup(){runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,11);});}
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK||data==null||data.getData()==null)return;try{
  if(request==10){if(pending==null)throw new IOException();try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(pending.getBytes(StandardCharsets.UTF_8));}pending=null;Toast.makeText(this,"Backup zakhire shod.",Toast.LENGTH_SHORT).show();}
  else if(request==11){ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream in=getContentResolver().openInputStream(data.getData())){byte[] buf=new byte[8192];int n;while((n=in.read(buf))!=-1){bytes.write(buf,0,n);if(bytes.size()>5000000)throw new IOException();}}web.evaluateJavascript("window.restoreBackup("+JSONObject.quote(new String(bytes.toByteArray(),StandardCharsets.UTF_8))+")",null);}
 }catch(Exception e){Toast.makeText(this,"Amaliat backup anjam nashod.",Toast.LENGTH_LONG).show();}}
 @Override public void onBackPressed(){web.evaluateJavascript("window.closeTopDialog()",v->{if(!"true".equals(v))finish();});}
 @Override protected void onDestroy(){if(web!=null){web.removeJavascriptInterface("MayehNative");web.destroy();}if(db!=null)db.close();super.onDestroy();}
}

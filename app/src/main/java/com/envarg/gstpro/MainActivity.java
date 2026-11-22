package com.envarg.gstpro;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import android.content.*;
import android.net.Uri;
import android.util.Base64;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.*;
public class MainActivity extends Activity {
 WebView web; ValueCallback<Uri[]> fileCallback; byte[] pendingPdf; String pendingName; String pendingMime;
 static final int PICK_FILE=1001, SAVE_PDF=1002;
 @Override public void onCreate(Bundle b){super.onCreate(b);
 web=new WebView(this);setContentView(web);
 WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
 web.setWebViewClient(new WebViewClient());
 web.addJavascriptInterface(new Object(){
 @JavascriptInterface public void saveFile(String base64,String filename,String mime){
  try{byte[] bytes=Base64.decode(base64,Base64.DEFAULT);if(bytes.length>20*1024*1024)throw new Exception("File too large");
   String name=filename.replaceAll("[^a-zA-Z0-9_.-]","_");
   String safeMime="application/json".equals(mime)?"application/json":"application/octet-stream";
   runOnUiThread(()->saveDocument(bytes,name,safeMime));
  }catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"Backup error: "+e.getMessage(),Toast.LENGTH_LONG).show());}
 }
 @JavascriptInterface public void pdf(String base64,String filename,boolean share){
  try{
   byte[] data=Base64.decode(base64,Base64.DEFAULT);
   if(data.length<8||data.length>20*1024*1024)throw new Exception("Invalid PDF size");
   String name=filename.replaceAll("[^a-zA-Z0-9_.-]","_");
   if(!name.endsWith(".pdf"))name+=".pdf";
   final byte[] bytes=data;final String fileName=name;
   runOnUiThread(()->{if(share)sharePdf(bytes,fileName);else savePdf(bytes,fileName);});
  }catch(Exception e){runOnUiThread(()->Toast.makeText(MainActivity.this,"PDF error: "+e.getMessage(),Toast.LENGTH_LONG).show());}
 }
 },"EnvargAndroid");
 web.setWebChromeClient(new WebChromeClient(){
 @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams params){
 if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=cb;
 try{startActivityForResult(params.createIntent(),PICK_FILE);return true;}catch(Exception e){fileCallback=null;return false;}
 }});
 web.loadUrl("https://envarg-gst-pro.hatchable.site");
 }
 void savePdf(byte[] bytes,String name){saveDocument(bytes,name,"application/pdf");}
 void saveDocument(byte[] bytes,String name,String mime){
 try{pendingPdf=bytes;pendingName=name;pendingMime=mime;Intent intent=new Intent(Intent.ACTION_CREATE_DOCUMENT);
 intent.addCategory(Intent.CATEGORY_OPENABLE);intent.setType(mime);intent.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(intent,SAVE_PDF);
 }catch(Exception e){Toast.makeText(this,"Cannot open PDF save: "+e.getMessage(),Toast.LENGTH_LONG).show();}
 }
 void sharePdf(byte[] bytes,String name){
 try{
 File folder=new File(getCacheDir(),"share");folder.mkdirs();File file=new File(folder,name);
 try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);}
 Uri uri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",file);
 Intent send=new Intent(Intent.ACTION_SEND);send.setType("application/pdf");send.putExtra(Intent.EXTRA_STREAM,uri);
 send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);send.setClipData(ClipData.newRawUri(name,uri));
 startActivity(Intent.createChooser(send,"Share GST PDF"));
 }catch(Exception e){Toast.makeText(this,"Share error: "+e.getMessage(),Toast.LENGTH_LONG).show();}
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);
 if(request==PICK_FILE&&fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}
 if(request==SAVE_PDF){if(result==RESULT_OK&&data!=null&&pendingPdf!=null){
 try(OutputStream out=getContentResolver().openOutputStream(data.getData())){out.write(pendingPdf);Toast.makeText(this,"PDF saved",Toast.LENGTH_SHORT).show();}
 catch(Exception e){Toast.makeText(this,"PDF save failed: "+e.getMessage(),Toast.LENGTH_LONG).show();}
 }pendingPdf=null;pendingName=null;pendingMime=null;}
 }
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}
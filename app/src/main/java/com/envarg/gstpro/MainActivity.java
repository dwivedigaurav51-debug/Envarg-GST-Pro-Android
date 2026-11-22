package com.envarg.gstpro;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.ValueCallback;
import android.content.Intent;
import android.net.Uri;
public class MainActivity extends Activity {
 WebView web; ValueCallback<Uri[]> fileCallback; static final int PICK_FILE=1001;
 @Override public void onCreate(Bundle b) {super.onCreate(b); web=new WebView(this);setContentView(web);
 WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
 web.setWebViewClient(new WebViewClient());web.setWebChromeClient(new WebChromeClient(){
 @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> callback,FileChooserParams params){
 if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=callback;
 try{startActivityForResult(params.createIntent(),PICK_FILE);return true;}catch(Exception e){fileCallback=null;return false;}
 }});web.loadUrl("https://envarg-gst-pro.hatchable.site");}
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request==PICK_FILE&&fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}}
 @Override public void onBackPressed(){if(web.canGoBack())web.goBack();else super.onBackPressed();}
}
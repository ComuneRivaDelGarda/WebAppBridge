package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.webkit.QWebSettings;

/**
 * Created by tiziano on 14/02/17.
 */
public class WebAppBridgeBuilder {

    private String url=null;
    private Boolean downloadEnabled = Boolean.FALSE;
    private String downloadPath = null;
    private String[] downloadContentTypes = {};
    private Boolean javaScriptEnabled = Boolean.FALSE;
    private Boolean javaScriptCanOpenWindows = Boolean.FALSE;
    private Boolean javaScriptCanCloseWindows = Boolean.FALSE;
    private Boolean javaScriptCanAccessClipboard = Boolean.FALSE;
    private Boolean developerExtrasEnabled = Boolean.FALSE;
    private Boolean cookieJarEnabled = Boolean.FALSE;
    private Object loadFinishedCallbackReceiver = null;
    private String loadFinishedCallback = null;

    public static WebAppBridgeBuilder create() {
        return new WebAppBridgeBuilder();
    }

    public WebAppBridgeBuilder url(String url){
        this.url = url;
        return this;
    }

    public WebAppBridgeBuilder downloadEnabled(Boolean enabled){
        downloadEnabled = enabled;
        return this;
    }

    public WebAppBridgeBuilder downloadPath(String path){
        downloadPath = path;
        return this;
    }

    public WebAppBridgeBuilder downloadContentTypes(String[] types){
        downloadContentTypes = types;
        return this;
    }

    public WebAppBridgeBuilder javaScriptEnabled(Boolean enabled){
        javaScriptEnabled = enabled;
        return this;
    }

    public WebAppBridgeBuilder javaScriptCanOpenWindows(Boolean enabled){
        javaScriptCanOpenWindows = enabled;
        return this;
    }

    public WebAppBridgeBuilder javaScriptCanCloseWindows(Boolean enabled){
        javaScriptCanCloseWindows = enabled;
        return this;
    }

    public WebAppBridgeBuilder javaScriptCanAccessClipboard(Boolean enabled){
        javaScriptCanAccessClipboard = enabled;
        return this;
    }

    public WebAppBridgeBuilder developerExtrasEnabled(Boolean enabled){
        developerExtrasEnabled = enabled;
        return this;
    }

    public WebAppBridgeBuilder cookieJarEnabled(Boolean enabled){
        cookieJarEnabled = enabled;
        return this;
    }

    public WebAppBridgeBuilder loadFinishedCallback(Object receiver, String callback){
        loadFinishedCallbackReceiver = receiver;
        loadFinishedCallback = callback;
        return this;
    }

    public WebAppBridge build() {
        return this.build(null);
    }

    public WebAppBridge build(QWidget parent){
        WebAppBridge bridge = new WebAppBridge(parent);
        if( bridge!=null ){
            bridge.loadPage(url);
        }
        if( javaScriptEnabled ){
            bridge.setAttribute(QWebSettings.WebAttribute.JavascriptEnabled, true);
        }
        if( javaScriptCanOpenWindows ){
            bridge.setAttribute(QWebSettings.WebAttribute.JavascriptEnabled, true);
            bridge.setAttribute(QWebSettings.WebAttribute.JavascriptCanOpenWindows, true);
        }
        if( javaScriptCanCloseWindows ){
            bridge.setAttribute(QWebSettings.WebAttribute.JavascriptEnabled, true);
            bridge.setAttribute(QWebSettings.WebAttribute.JavascriptCanOpenWindows, true);
        }
        if( javaScriptCanCloseWindows ){
            bridge.setAttribute(QWebSettings.WebAttribute.JavascriptEnabled, true);
            bridge.setAttribute(QWebSettings.WebAttribute.JavascriptCanAccessClipboard, true);
        }
        if( developerExtrasEnabled ){
            bridge.setAttribute(QWebSettings.WebAttribute.DeveloperExtrasEnabled, true);
        }
        if( cookieJarEnabled ){
            bridge.enableCookieJar();
        }
        if( downloadEnabled ){
            bridge.enableDownload(downloadContentTypes, downloadPath);
        }
        if (loadFinishedCallbackReceiver != null) {
            bridge.loadFinishedCallback(loadFinishedCallbackReceiver, loadFinishedCallback);
        }
        return bridge;
    }

}

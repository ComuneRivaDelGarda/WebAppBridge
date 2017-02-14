package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.webkit.QWebSettings;

/**
 * Created by tiziano on 14/02/17.
 */
public class WebAppBridge extends QDialog {

    private final WebView webView;

    public WebAppBridge() {
        this(null);
    }

    public WebAppBridge(QWidget parent) {
        super(parent);

        webView = new WebView();
        QVBoxLayout vLayout = new QVBoxLayout(this);
        vLayout.addWidget(webView);

        webView.settings().setAttribute(QWebSettings.WebAttribute.JavascriptEnabled, true);
        webView.settings().setAttribute(QWebSettings.WebAttribute.JavascriptCanOpenWindows, true);
        webView.settings().setAttribute(QWebSettings.WebAttribute.JavascriptCanAccessClipboard, true);

    }

    public void loadPage(String url){
        webView.load(new QUrl(url));
    }

}

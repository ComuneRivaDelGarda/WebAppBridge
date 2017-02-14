package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.network.QNetworkAccessManager;
import com.trolltech.qt.network.QNetworkCookieJar;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.network.QNetworkRequest;
import com.trolltech.qt.webkit.QWebPage;
import com.trolltech.qt.webkit.QWebSettings;
import com.trolltech.qt.webkit.QWebView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by tiziano on 16/12/16.
 */
public class WebView extends QWebView {

    private QNetworkCookieJar cookieJar;

    public WebView() {
        this(null);
    }

    public WebView(QWidget parent) {
        super(parent);
        cookieJar = new QNetworkCookieJar();
        QWebPage page = page();
        page.networkAccessManager().setCookieJar(cookieJar);
        page.loadFinished.connect(this, "loadFinished()");
        page.windowCloseRequested.connect(this, "close()");
        page.setLinkDelegationPolicy(QWebPage.LinkDelegationPolicy.DelegateAllLinks);
        page.linkClicked.connect(this, "linkFilter(QUrl)");

        settings().globalSettings().setAttribute(QWebSettings.WebAttribute.DeveloperExtrasEnabled, true);
    }

    @Override
    protected QWebView createWindow(QWebPage.WebWindowType type) {
        WebView view = new WebView();
        view.page().networkAccessManager().setCookieJar(cookieJar);
        view.show();
        return view;
    }

    private void linkFilter(QUrl url){

        QNetworkAccessManager manager = page().networkAccessManager();
        QNetworkRequest request = new QNetworkRequest(url);

        QNetworkReply headerReply = manager.head(request);
        headerReply.finished.connect(this, "checkHeaders()");
    }

    private void checkHeaders(){
        QNetworkReply headerReply = (QNetworkReply) signalSender();
        QUrl url = headerReply.url();
        String contentType = (String) headerReply.header(QNetworkRequest.KnownHeaders.ContentTypeHeader);
        System.out.println("Content-Type: '" + contentType + "'");
        if( "application/pdf".equals(contentType) ){
            QNetworkAccessManager manager = headerReply.manager();
            QNetworkRequest request = new QNetworkRequest(url);
            QNetworkReply reply = manager.get(request);
            reply.finished.connect(this, "downloadFile()");
        } else {
            setUrl(url);
        }
    }

    private void downloadFile(){
        System.out.println("Stai scaricando un pdf!!");
        QNetworkReply reply = (QNetworkReply) signalSender();
        byte[] bytes = reply.readAll().toByteArray();
        System.out.println("Size: " + bytes.length);
        String fileName = QFileDialog.getSaveFileName(this, "Save file");
        saveFile(fileName , bytes);
    }

    private void saveFile(String fileName, byte[] content) {
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            out.write(content);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFinished(){
        // page loaded
    }

}

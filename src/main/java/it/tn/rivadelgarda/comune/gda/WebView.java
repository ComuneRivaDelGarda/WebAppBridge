package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.network.QNetworkAccessManager;
import com.trolltech.qt.network.QNetworkCookieJar;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.network.QNetworkRequest;
import com.trolltech.qt.webkit.QWebPage;
import com.trolltech.qt.webkit.QWebView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by tiziano on 16/12/16.
 */
public class WebView extends QWebView {

    private QNetworkCookieJar cookieJar=null;
    private QWebPage page;
    private String downloadPath=null;
    private String[] downloadContentTypes={};

    public WebView() {
        this(null);
    }

    public WebView(QWidget parent) {
        super(parent);
        page = page();
    }

    public void enableCoockieJar(){
        cookieJar = new QNetworkCookieJar();
        page.networkAccessManager().setCookieJar(cookieJar);
    }

    public void enableDownload(String[] types){
        enableDownload(types, null);
    }

    public void enableDownload(String[] types, String path){
        downloadContentTypes = types;
        page.downloadRequested.connect(this, "downloadRequest(QNetworkRequest)");
        if( downloadContentTypes.length>0 ) {
            page.setLinkDelegationPolicy(QWebPage.LinkDelegationPolicy.DelegateAllLinks);
            page.linkClicked.connect(this, "linkFilter(QUrl)");
        } else {
            page.setForwardUnsupportedContent(true);
            page.unsupportedContent.connect(this, "unsupportedContent(QNetworkReply)");
        }
        if( path!=null ){
            downloadPath = path;
        }
    }

    private void unsupportedContent(QNetworkReply reply){
        downloadFile(reply);
    }

    private void downloadRequest(QNetworkRequest request){
        if( downloadContentTypes.length>0 ){
            QNetworkAccessManager manager = page().networkAccessManager();
            QNetworkReply headerReply = manager.head(request);
            headerReply.finished.connect(this, "checkHeaders()");
        } else {
            QNetworkAccessManager manager = page().networkAccessManager();
            QNetworkReply reply = manager.get(request);
            downloadFile(reply);
        }
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
        if( Arrays.asList(downloadContentTypes).contains(contentType) ){
            QNetworkAccessManager manager = headerReply.manager();
            QNetworkRequest request = new QNetworkRequest(url);
            QNetworkReply reply = manager.get(request);
            reply.finished.connect(this, "downloadFileSlot()");
        } else {
            setUrl(url);
        }
    }

    private void downloadFileSlot(){
        QNetworkReply reply = (QNetworkReply) signalSender();
        downloadFile(reply);
    }

    private void downloadFile(QNetworkReply reply){
        if( downloadContentTypes.length>0 ){

        }
        byte[] bytes = reply.readAll().toByteArray();
        String folderPath=null;
        if( downloadPath!=null ) {
            folderPath = QFileDialog.getExistingDirectory(this, "Save file", downloadPath, QFileDialog.Option.ShowDirsOnly);
        } else {
            folderPath = QFileDialog.getExistingDirectory(this, "Save file", null, QFileDialog.Option.ShowDirsOnly);
        }
        if( folderPath!=null ) {
            // XXX: to catch the correct file name
            saveFile(folderPath + "/out.pdf", bytes);
        } else {
            QMessageBox.critical(this, "Alert", "File not saved");
        }
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

    @Override
    protected QWebView createWindow(QWebPage.WebWindowType type) {
        if( cookieJar==null ) {
            return super.createWindow(type);
        }
        WebView view = new WebView();
        view.page().networkAccessManager().setCookieJar(cookieJar);
        view.show();
        return view;
    }


}

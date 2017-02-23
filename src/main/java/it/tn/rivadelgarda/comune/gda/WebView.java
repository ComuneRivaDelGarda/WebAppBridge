package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.core.QByteArray;
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
import java.util.List;

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
        QNetworkAccessManager manager = page().networkAccessManager();
        if( downloadContentTypes.length>0 ){
            QNetworkReply headerReply = manager.head(request);
            headerReply.finished.connect(this, "checkHeaders()");
        } else {
            QNetworkReply reply = manager.get(request);
            reply.finished.connect(this, "downloadFileSlot()");
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
        //
        byte[] bytes = reply.readAll().toByteArray();

        String fileName=null;
        // filename: strategia uno: content-disposition
        QByteArray contentDisposition = reply.rawHeader(new QByteArray("content-disposition"));
        String[] split1 = contentDisposition.toString().split(";");
        for( String tkn: split1 ){
            String[] split = tkn.split("=");
            if( "filename".equals(split[0]) ){
                fileName = split[1].substring(1, split[1].length()-1);
            }
        }
        // filename: strategia due: url
        if( fileName==null ) {
            String[] split = reply.url().toString().split("/");
            fileName = split[split.length - 1];
        }

        String folderPath=null;
        if( downloadPath!=null ) {
            folderPath = QFileDialog.getExistingDirectory(this, "Save file", downloadPath, QFileDialog.Option.ShowDirsOnly);
        } else {
            folderPath = QFileDialog.getExistingDirectory(this, "Save file", null, QFileDialog.Option.ShowDirsOnly);
        }
        if( folderPath!=null ) {
            saveFile(folderPath + "/" + fileName, bytes);
        } else {
            QMessageBox.critical(this, "Alert", "File not saved");
        }
    }

    private void saveFile(String fileName, byte[] content) {
        try {
            FileOutputStream out = new FileOutputStream(fileName);
            out.write(content);
            out.flush();
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

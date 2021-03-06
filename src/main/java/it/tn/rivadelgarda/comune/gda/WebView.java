package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;
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
        QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
        reply.finished.connect(this, "downloadFileSlot()");
    }

    private void downloadRequest(QNetworkRequest request){
        QNetworkAccessManager manager = page().networkAccessManager();
        if( downloadContentTypes.length>0 ){
            QNetworkReply headerReply = manager.head(request);
            QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
            headerReply.finished.connect(this, "checkHeaders()");
        } else {
            QNetworkReply reply = manager.get(request);
            QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
            reply.finished.connect(this, "downloadFileSlot()");
        }
    }

    private void linkFilter(QUrl url){
        QNetworkAccessManager manager = page().networkAccessManager();
        QNetworkRequest request = new QNetworkRequest(url);
        QNetworkReply headerReply = manager.head(request);
        QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
        headerReply.finished.connect(this, "checkHeaders()");
    }

    private void checkHeaders(){
        QApplication.restoreOverrideCursor();
        QNetworkReply headerReply = (QNetworkReply) signalSender();
        QUrl url = headerReply.url();
        String contentType = (String) headerReply.header(QNetworkRequest.KnownHeaders.ContentTypeHeader);
        if( Arrays.asList(downloadContentTypes).contains(contentType) ){
            QNetworkAccessManager manager = headerReply.manager();
            QNetworkRequest request = new QNetworkRequest(url);
            QNetworkReply reply = manager.get(request);
            QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
            reply.finished.connect(this, "downloadFileSlot()");
        } else {
            setUrl(url);
        }
    }

    private void downloadFileSlot(){
        QApplication.restoreOverrideCursor();
        QNetworkReply reply = (QNetworkReply) signalSender();
        // TODO: download or open
        QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();
        buttons.set(QMessageBox.StandardButton.Save, QMessageBox.StandardButton.Open);
        QMessageBox.StandardButton button = QMessageBox.question(this.parentWidget(), "Scarica o apri",
                "Scegli se scaricare o aprire il file", buttons);
        if( button == QMessageBox.StandardButton.Save ) {
            downloadOrOpenFile(reply, Boolean.FALSE);
        } else if( button == QMessageBox.StandardButton.Open ){
            downloadOrOpenFile(reply, Boolean.TRUE);
        }
    }


    private void downloadOrOpenFile(QNetworkReply reply, Boolean open){
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

        if( open ){
            openFile(fileName, bytes);
        } else {
            String defaultSaveFileName = QDir.cleanPath(downloadPath + QDir.separator() + fileName);
            String filePath = QFileDialog.getSaveFileName(this, "Salva file", defaultSaveFileName);
            if( filePath!=null && !"".equals(filePath.trim())){
                saveFile(filePath, bytes);
            } else {
                QMessageBox.critical(this, "Attenzione", "File non salvato");
            }
        }
    }

    private void openFile(String fileName, byte[] content){
        QTemporaryFile tmpFile = new QTemporaryFile(QDir.tempPath() + "/XXXXXX_" + fileName);
        tmpFile.open(new QFile.OpenMode(QFile.OpenModeFlag.WriteOnly, QFile.OpenModeFlag.Unbuffered));
        tmpFile.write(content);
        QFileInfo file = new QFileInfo(tmpFile);
        String xAppsName = file.dir().path() + "/u_" +  file.fileName();
        System.err.print("xAppsName=" + xAppsName + "---\n");
        tmpFile.copy(xAppsName);
        tmpFile.close();
        QFile.remove(tmpFile.fileName());
        QDesktopServices.openUrl(QUrl.fromLocalFile(xAppsName));
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

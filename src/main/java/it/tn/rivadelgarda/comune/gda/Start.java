package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.gui.QApplication;

/**
 * Created by tiziano on 14/02/17.
 */
public class Start {

    /*
     *  Load finished callback example
     */
    public static class CallbackClass {

        private static final String JSCODE = "document.getElementById(\"header\").style.display='none';";

        public void callback(){
            bridge.getWebView().page().mainFrame().evaluateJavaScript(JSCODE);
        }

    }



    private static WebAppBridge bridge;

    public static void main(String[] args) {
        QApplication.initialize(args);

        CallbackClass callbackClass = new CallbackClass();

        bridge = WebAppBridgeBuilder
                .create()
                .url("http://www.comune.rivadelgarda.tn.it/Comune/Documenti/Statuto-Comunale")

                .developerExtrasEnabled(Boolean.TRUE)                       // abilità "inspect" dal menu contestuale
                .javaScriptEnabled(Boolean.TRUE)                            // abilità l'esecuzione di JavaScript
                .javaScriptCanOpenWindows(Boolean.TRUE)                     // JS può aprire finestre in popup
                .javaScriptCanCloseWindows(Boolean.TRUE)                    // JS può chiudere finestre
                .javaScriptCanAccessClipboard(Boolean.TRUE)                 // JS legge e scrive da clipboard
                .cookieJarEnabled(Boolean.TRUE)                             // la pagina può impostare dei cookie

                .downloadEnabled(Boolean.TRUE)                              // download abilitati
                .downloadContentTypes(new String[]{"application/pdf"})      // content type permessi al download
                .downloadPath("/Users/tiziano/Downloads")                   // cartella proposta per il download

                .loadFinishedCallback(callbackClass, "callback()")          // callback da eseguire a pagina caricata

                .build();

        bridge.show();

        QApplication.instance().exec();

    }

}

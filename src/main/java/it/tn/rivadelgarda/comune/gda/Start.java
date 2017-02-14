package it.tn.rivadelgarda.comune.gda;

import com.trolltech.qt.gui.QApplication;

/**
 * Created by tiziano on 14/02/17.
 */
public class Start {

    public static void main(String[] args) {
        QApplication.initialize(args);

        WebAppBridge bridge = WebAppBridgeBuilder
                .create()
                .url("http://www.comune.rivadelgarda.tn.it/Comune/Documenti/Statuto-Comunale")

                .developerExtrasEnabled(Boolean.TRUE)                       // abilità "inspect" dal menu contestuale
                .javaScriptEnabled(Boolean.TRUE)                            // abilità l'esecuzione di JavaScript
                .javaScriptCanOpenWindows(Boolean.TRUE)                     // JS può aprire finestre in popup
                .javaScriptCanCloseWindows(Boolean.TRUE)                    // JS può chiudere finestre
                .cookieJarEnabled(Boolean.TRUE)                             // la pagina può impostare dei cookie

                .downloadEnabled(Boolean.TRUE)                              // download abilitati
                .downloadContentTypes(new String[]{"application/pdf"})      // content type permessi al download
                .downloadPath("/Users/tiziano/Downloads")                   // cartella proposta per il download

                .build();

        bridge.show();

        QApplication.instance().exec();

    }
}

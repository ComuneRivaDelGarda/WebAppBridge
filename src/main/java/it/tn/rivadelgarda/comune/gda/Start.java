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
                .build();

        bridge.show();

        QApplication.instance().exec();

    }
}

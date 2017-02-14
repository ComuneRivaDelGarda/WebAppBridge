package it.tn.rivadelgarda.comune.gda;

/**
 * Created by tiziano on 14/02/17.
 */
public class WebAppBridgeBuilder {

    private String url=null;

    public static WebAppBridgeBuilder create() {
        return new WebAppBridgeBuilder();
    }

    public WebAppBridgeBuilder url(String url){
        this.url = url;
        return this;
    }

    public WebAppBridge build(){
        WebAppBridge bridge = new WebAppBridge();
        if( bridge!=null ){
            bridge.loadPage(url);
        }
        return bridge;
    }

}

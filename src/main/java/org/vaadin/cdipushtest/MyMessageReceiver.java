package org.vaadin.cdipushtest;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import org.vaadin.msghub.AbstractMessageHub;

/**
 */
public class MyMessageReceiver extends AbstractMessageHub<MainUI> {

    public MyMessageReceiver(MainUI mainUI) {
        super(mainUI);
    }

    @Override
    protected Class<MainUI> getUiClass() {
        return MainUI.class;
    }

    @Override
    protected void handleMessage(MainUI ui, Message message) {
        TextMessage msg = (TextMessage) message;
        try {
            String text = msg.getText();
            ui.onMessage(text);
        } catch (JMSException ex) {
            Logger.getLogger(MyMessageReceiver.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected String getTopicName() {
        return Resources.TOPIC_NAME;
    }

}

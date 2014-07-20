package org.vaadin.cdipushtest;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * JMSContext (injectable in Java EE) don't allow setListener :-( Thus,
 * receiving must be implemented with jndi approach. One of these are created
 * per UI.
 * <p>
 * I have no idea whether this can work properly in e.g. clustered
 * environment, but in simple testing seems to work fine. Need to do some
 * investigation.
 */
public class MessageReceiver implements MessageListener {

    public static void registerUI(MainUI ui) {
        try {
            MessageReceiver messageReceiver = new MessageReceiver(ui);
        } catch (JMSException | NamingException ex) {
            Logger.getLogger(MessageReceiver.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

    private final MainUI ui;

    private MessageReceiver(MainUI ui) throws JMSException, NamingException {
        this.ui = ui;
        InitialContext ctx = new InitialContext();
        Topic chatTopic = (Topic) ctx.lookup(Resources.TOPIC_NAME);
        ConnectionFactory factory
                = (ConnectionFactory) ctx.lookup("ConnectionFactory");
        Connection c = factory.createConnection();

        c.createSession(false, Session.AUTO_ACKNOWLEDGE)
                .createConsumer(chatTopic)
                .setMessageListener(MessageReceiver.this);

        c.start();

        ui.addDetachListener(e -> {
            try {
                c.close();
            } catch (JMSException ex) {
                Logger.getLogger(MessageReceiver.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        });

    }

    @Override
    public void onMessage(Message message) {
        TextMessage msg = (TextMessage) message;
        try {
            String text = msg.getText();
            ui.access(() -> ui.onMessage(text));
        } catch (JMSException ex) {
            Logger.getLogger(MessageReceiver.class.getName()).
                    log(Level.SEVERE, null, ex);
        }
    }

}

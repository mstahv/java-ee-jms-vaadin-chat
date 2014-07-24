package org.vaadin.cdipushtest;

import com.vaadin.server.VaadinSession;
import com.vaadin.ui.UI;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.ConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionEvent;

/**
 * JMSContext (injectable in Java EE) don't allow setListener :-( Thus,
 * receiving must be implemented with jndi approach. One of these are created
 * per UI.
 * <p>
 * The class implements session activation listener and saves itself to session.
 * Relevant fields are transient and the class thus stores pretty much nothing
 * in the session, but they are reconnected when the session is revived on a new
 * cluster node. Note, that the current solution loses messages that are received
 * during "hibernation". To workaround this, the board state should also be stored
 * in an EJB and the initial state should be read from that when (re)activating.
 */
public class MessageReceiver implements HttpSessionActivationListener,
        MessageListener, Serializable {

    private transient MainUI ui;
    private transient JMSContext c;

    private MessageReceiver() {

    }

    public static void register(MainUI mainUI) {
        MessageReceiver messageReceiver = new MessageReceiver();
        messageReceiver.startListening(mainUI);
        mainUI.getSession().getSession().setAttribute(MessageReceiver.class.
                getName(), messageReceiver);
    }

    private void startListening(MainUI ui1) {
        try {
            this.ui = ui1;
            InitialContext ctx = new InitialContext();
            Topic chatTopic = (Topic) ctx.lookup(Resources.TOPIC_NAME);
            ConnectionFactory factory
                    = (ConnectionFactory) ctx.lookup("ConnectionFactory");
            c = factory.createContext();
            c.createConsumer(chatTopic).setMessageListener(this);
            c.start();
            ui1.addDetachListener(e -> {
                c.close();
            });

        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
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

    @Override
    public void sessionWillPassivate(HttpSessionEvent se) {
        /* cluse the JMS connection as we cannot serialize that, nor update the
         * UI while it is on disk or moved another node. */
        c.close();
    }

    @Override
    public void sessionDidActivate(HttpSessionEvent se) {
        /*
         * This is called by servlet when/if session is transferred to a different
         * node in a cluster. In these cases, we must reconnect a new JMS listener to UI.
         */
        startListening(findUI(se.getSession()));
    }

    private MainUI findUI(HttpSession session) {
        for (VaadinSession vaadinSession : VaadinSession.getAllSessions(session)) {
            for (UI ui : vaadinSession.getUIs()) {
                if (ui instanceof MainUI) {
                    return (MainUI) ui;
                }
            }
        }
        throw new RuntimeException("UI not found!");
    }

}

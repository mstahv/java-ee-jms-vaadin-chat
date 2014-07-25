package org.vaadin.cdipushtest;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import java.time.LocalTime;
import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Topic;
import org.vaadin.maddon.button.PrimaryButton;
import org.vaadin.maddon.fields.MTextField;
import org.vaadin.maddon.label.RichText;
import org.vaadin.maddon.layouts.MHorizontalLayout;
import org.vaadin.maddon.layouts.MVerticalLayout;

@CDIUI
@Push
@Theme("dawn")
public class MainUI extends UI {

    private static final int MAX_VISIBLE_MSG = 30;

    @Inject
    private JMSContext jmsContext;

    @Resource(name = Resources.TOPIC_NAME)
    private Topic topic;

    private final TextField msgInput = new MTextField().withInputPrompt(
            "Message");

    private final MVerticalLayout messages = new MVerticalLayout().withMargin(
            false);

    private String name;

    @Override
    protected void init(VaadinRequest request) {
        createName(request);

        new MyMessageReceiver(this).startListening();

        postMessage(name + " joined chat!");

        PrimaryButton send = new PrimaryButton("Send");
        send.addClickListener(e -> {
            postMessage(name + ": " + msgInput.getValue());
            msgInput.selectAll();
        });
        setContent(
            new MVerticalLayout(
                    new RichText().withMarkDownResource("/welcome.md"),
                    new MHorizontalLayout(
                            msgInput,
                            send)
                    .alignAll(Alignment.BOTTOM_LEFT),
                    messages
            )
        );
    }

    static int i = 1;

    public void createName(VaadinRequest request) {
        name = "User " + i++;
    }

    public void onMessage(String msg) {
        messages.addComponentAsFirst(new Label(LocalTime.now() + ": " + msg));
        if (messages.getComponentCount() > MAX_VISIBLE_MSG) {
            messages.removeComponent(messages.getComponent(MAX_VISIBLE_MSG - 1));
        }
    }

    private void postMessage(String string) {
        jmsContext.createProducer().send(topic, string);
    }

}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vaadin.cdipushtest;

import com.vaadin.cdi.CDIUIProvider;
import com.vaadin.server.VaadinServlet;
import javax.inject.Inject;
import javax.jms.JMSDestinationDefinition;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * Custom servlet is needed for "async=true" and introducing JMS topic. Otherwise
 * we could deal with the server presented by Vaadin CDI.
 */
@WebServlet(name = "PushCDIServlet", urlPatterns = {"/*"}, asyncSupported = true)
@JMSDestinationDefinition(name = Resources.TOPIC_NAME, interfaceName = "javax.jms.Topic", destinationName = "myTopic")
public class PushCDIServlet extends VaadinServlet {

    @Inject
    private CDIUIProvider uiProvider;

    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(e -> {
            e.getSession().addUIProvider(uiProvider);
        });
    }
}

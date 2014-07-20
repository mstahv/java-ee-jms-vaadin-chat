# Java EE + JMS + Vaadin example

This is an example and learning project for using JMS (in Java EE) with Vaadin. JMS takes care of delivering messages to all participants, Vaadin takes care of delivering changes to end users browser, naturally with  WebSocket based push.

Tested with Wildfly, but most likely works in other modern app servers as well, possibly with some different JMS settings.

To play with the project, import it to your IDE and deploy to Wildfly. Or from command line,
issue

```
mvn wildfly:run
```


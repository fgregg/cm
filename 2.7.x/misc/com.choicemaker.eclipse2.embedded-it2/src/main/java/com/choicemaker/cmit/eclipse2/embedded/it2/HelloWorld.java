package com.choicemaker.cmit.eclipse2.embedded.it2;

import javax.ejb.Remote;

@Remote
public interface HelloWorld {
    public String sayHello();
}

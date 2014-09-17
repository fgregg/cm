package com.choicemaker.cmit.eclipse2.embed.it2;

import javax.ejb.Remote;

@Remote
public interface HelloWorld {
    public String sayHello();
}

package com.choicemaker.cmit.eclipse2.embedded.it2;

import javax.ejb.Stateless;

import com.choicemaker.fake.ejb3.HelloWorld;

/**
 * Session Bean implementation class HelloWorldBean
 */
@Stateless
public class HelloWorldBean implements HelloWorld {

	public HelloWorldBean() {
	}

	public String sayHello() {
		return "Hello World !!!";
	}
}

package com.choicemaker.fake.ejb3;

import javax.ejb.Stateless;

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

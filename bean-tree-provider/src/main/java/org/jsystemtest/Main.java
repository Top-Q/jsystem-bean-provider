package org.jsystemtest;

import org.jsystemtest.bean.Book;
import org.jsystemtest.bean.Library;

public class Main {

	public static void main(String[] args) throws Exception {
		BeanTreeDialog d = new BeanTreeDialog("title");
		Book b0 = new Book();
		b0.setTitle("The title");
		b0.setAuthor("An author");
		b0.setPages(4);
		Book b1 = new Book();
		b1.setTitle("A new title");
		b1.setAuthor("Some author");
		b1.setPages(4);
		Library lib = new Library();
		lib.setBook0(b0);
		lib.setBook1(b1);
		d.buildAndShowDialog(lib);

	}

}

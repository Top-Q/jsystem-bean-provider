package org.jsystemtest;

import org.jsystemtest.bean.Book;
import org.jsystemtest.bean.Library;
import org.jsystemtest.bean.Page;

import java.io.File;

public class Main {

	public static void main(String[] args) throws Exception {
		BeanTreeDialog d = new BeanTreeDialog("title");

        Book b0 = new Book();
		b0.setTitle("The title");
		b0.setAuthor("An author");
        b0.seteBookAvailable(true);
        b0.setFile(new File("/workspace/dev/git3/jsystem-bean-provider/bean-tree-provider/src/main/java/org/jsystemtest/Main.java"));
		//b0.setPages(4);
        //b0.setPage(new Page(4));
        Page pageB0 = new Page();
        pageB0.setPageCount(4);
        b0.setPages(new Page[] { pageB0 } );
        b0.setGreetings("Thank you!");
        b0.setEditions(new int[] {1, 3, 5});

		Book b1 = new Book();
		b1.setTitle("A new title");
		b1.setAuthor("Some author");
        b1.setPublication("Harvard");
		//b1.setPages(4);
        //b1.setPage(new Page(6));

		Library lib = new Library();
		lib.setBook0(b0);
		lib.setBook1(b1);
		d.buildAndShowDialog(lib);

	}

}

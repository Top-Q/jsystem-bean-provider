package org.jsystemtest;

import com.thoughtworks.xstream.XStream;
import org.jsystemtest.bean.Author;
import org.jsystemtest.bean.Book;
import org.jsystemtest.bean.Library;
import org.jsystemtest.bean.Page;

import java.io.File;

public class Main {

	public static void main(String[] args) throws Exception {
		BeanTreeDialog d = new BeanTreeDialog("CloudBand's Inspection Tree");

        /*Library lib = new Library();

        Book b0 = new Book();
		b0.setTitle("The title");
		b0.setAuthor("An author");
        b0.seteBookAvailable(true);
        b0.setFile(new File("/workspace/dev/git3/jsystem-bean-provider/bean-tree-provider/src/main/java/org/jsystemtest/Main.java"));
		//b0.setPages(4);
        //b0.setPage(new Page(4));
        Page pageB0 = new Page();
        pageB0.setPageCount(4);
        pageB0.setType(Page.PageType.REGULAR);
        b0.setPages(new Page[]{pageB0});
        b0.setGreetings("Thank you!");
        b0.setEditions(new int[] {1, 3, 5});
        b0.setPublication("Boston");

		Book b1 = new Book();
		b1.setTitle("A new title");
		b1.setAuthor("Some author");
        b1.setPublication("Harvard");
        b1.setGlossary(new String[] {"Recognition", "Prettify"});
        b1.setWordLocations(new Integer[][][] { {{90, 88}}, {{11, 22}, {33, 66}}});
		//b1.setPages(4);
        //b1.setPage(new Page(6));

		lib.setBook0(b0);
        lib.setBook1(b1);

		//d.buildAndShowDialog(objInstance);


        Author author = new Author();
        author.setName("Obama");*/

        Integer param2 = new Integer(4);
        String[] names = new String[0];

        Object objInstance = names;

        String str = getAsString(objInstance);

        // Testing single build and double show
        d.buildDialog();

        d.initTreeTableModel(objInstance);
        d.showDialog();

        if(d.isSaveClicked()) { // User clicked Save
            objInstance = d.getRootObject();
            str = getAsString(objInstance);
            //str = getAsString(objInstance);
            //objInstance = getFromString(str);
        }

        objInstance = getFromString(str);

        d.initTreeTableModel(objInstance);
        d.showDialog();



        /*d.initTreeTableModel(objInstance);
        d.showDialog();

        System.out.println((((AbstractBeanTreeNode) (d.treeTableModel.getRoot())).getUserObject()));

        if(d.isSaveClicked()) {
            Object obj = d.getRootObject();
            d.initTreeTableModel(obj);
        } else {
            Object obj = xs.fromXML(strObj);
            d.initTreeTableModel(obj);
        }
        d.showDialog();
*/

        System.out.println((((AbstractBeanTreeNode)(d.treeTableModel.getRoot())).getUserObject()));
	}

    public static String getAsString(Object o) {
        if (o == null){
            return "";
        }
        XStream xStream = new XStream(/*new DomDriver()*/);
        String strRepresentation =  xStream.toXML(o);

        return strRepresentation;
    }

    public static Object getFromString(String stringRepresentation) throws Exception {
        XStream xStream = new XStream(/*new DomDriver()*/);
        Object obj = xStream.fromXML(stringRepresentation);

        return obj;
    }

}

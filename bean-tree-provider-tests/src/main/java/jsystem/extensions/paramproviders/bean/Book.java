package jsystem.extensions.paramproviders.bean;

import java.io.File;

public class Book {
	
	private String title = "The title";
	private String author;
    private String publication = "Harvard";
	private Page[] pages;
    private String[] glossary;
    private int[][][] wordLocations;

    private int year;
    private int[] editions;
    private Boolean eBookAvailable = false;
    private File file = new File("test");
    private String greetings = null;


    /*public File getFile() {
        return file;
    }*/

    public void setFile(File file) {
        this.file = file;
    }

    public Boolean geteBookAvailable() {
        return eBookAvailable;
    }

    public void seteBookAvailable(Boolean eBookAvailable) {
        this.eBookAvailable = eBookAvailable;
    }


	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}

    public Page[] getPages() {
        return pages;
    }

    public void setPages(Page[] pages) {
        this.pages = pages;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public String getGreetings() {
        return greetings;
    }

    public void setGreetings(String greetings) {
        this.greetings = greetings;
    }

    public int[] getEditions() {
        return editions;
    }

    public void setEditions(int[] editions) {
        this.editions = editions;
    }

    public String[] getGlossary() {
        return glossary;
    }

    public void setGlossary(String[] glossary) {
        this.glossary = glossary;
    }

    public int[][][] getWordLocations() {
        return wordLocations;
    }

    public void setWordLocations(int[][][] wordLocations) {
        this.wordLocations = wordLocations;
    }
}

package edu.uiowa.loaders;

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.zip.*;
import java.net.*;

import javax.net.ssl.HttpsURLConnection;

public class generic extends DefaultHandler {
    protected static boolean debug = true;
    protected static boolean verbose = false;
    protected static boolean terse = false;
    protected static boolean load = false;

	protected StringBuffer buffer = new StringBuffer();
    boolean wrapWithTag = false;
    String wrapperTag = null;

	protected static XMLReader xr = null;

	public static void main (String args[]) throws Exception {
		generic handler = new generic();
		handler.run(args);
	}
	
	public void run(String args[]) throws Exception {
		xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(this);
		xr.setErrorHandler(this);
		xr.setFeature("http://xml.org/sax/features/validation", false);

		if (args.length > 0) {
            if (args[0].equals("-")) {
                processStdin();
                return;
            }
			// Parse each file provided on the command line.
			for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-wrapper=")) {
                    wrapWithTag = true;
                    wrapperTag = args[i].substring(args[i].indexOf('=')+1);
                    if (debug) System.out.println("wrapper tag:" + wrapperTag);
                } else {
                    if (wrapWithTag) {
                        System.out.println("Reading with wrapper " + wrapperTag + "  " + args[i]);
                        processFileWithWrapper(args[i]);
                    } else {
                        System.out.println("Reading " + args[i]);
                        try {
                            processFile(args[i]);
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
			}
		} else {
			// read files from stdin
			BufferedReader IODesc = new BufferedReader(new InputStreamReader(System.in));
			String current = null;
			while ((current = IODesc.readLine()) != null) {
				System.out.println("processing : " + current);
				try {
                    processFile(current);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
			}
		}
    }
    
	public void run(String arg) throws Exception {
		xr = XMLReaderFactory.createXMLReader();
		xr.setContentHandler(this);
		xr.setErrorHandler(this);
		xr.setFeature("http://xml.org/sax/features/validation", false);

		if (verbose) System.out.println("Reading " + arg);
		processFile(arg);
    }
    
	public void processFile(String file) throws Exception {
		BufferedReader IODesc = null;
		if (file.indexOf("://") < 0) {
            IODesc = new BufferedReader(new FileReader(file));
        } else if (file.startsWith("https:")) {
            System.out.println("trying ssl connection...");
            System.setProperty("sslfactory", "org.postgresql.ssl.NonValidatingFactory");
            HttpsURLConnection conn = (HttpsURLConnection)(new URL(file)).openConnection();
            IODesc = new BufferedReader(new InputStreamReader(conn.getInputStream()), 200000);
        } else if (file.endsWith(".z") || file.endsWith(".gz")) {
			IODesc = new BufferedReader(new InputStreamReader(new GZIPInputStream((new URL(file)).openConnection().getInputStream())), 200000);
		} else
			IODesc = new BufferedReader(new InputStreamReader((new URL(file)).openConnection().getInputStream()), 200000);

	    xr.parse(new InputSource(IODesc));
	    IODesc.close();
    }

    public void processStdin() throws Exception {
        xr.parse(new InputSource(new BufferedReader(new InputStreamReader(System.in))));
    }

    public void processFileWithWrapper(String file) throws Exception {
       BufferedReader IODesc = new BufferedReader(new PrivateReader(file, "<"+wrapperTag+">", "</"+wrapperTag+">"), 20000);
        xr.parse(new InputSource(IODesc));
        IODesc.close();
    }

    public generic () {
		super();
    }


    ////////////////////////////////////////////////////////////////////
    // Event handlers.
    ////////////////////////////////////////////////////////////////////


    public void startDocument () {
		if (debug) System.out.println("Start document");
    }


    public void endDocument () {
		if (debug) System.out.println("End document");
    }


    public void startElement (String uri, String name, String qName, Attributes atts) {
		if (debug) {
			//if ("".equals (uri))
			    System.out.println("Start element: " + qName);
			for (int i = 0; i < atts.getLength(); i++)
				System.out.println("\tattribute " + i + ": " + atts.getQName(i) + " > " + atts.getValue(i));
			//else
			//    System.out.println("Start element: {" + uri + "}" + name + " " + atts);
		}
    }


    public void endElement (String uri, String name, String qName) {
		if (debug) {
			//if ("".equals (uri))
			    System.out.println("End element: " + qName);
			//else
			//    System.out.println("End element:   {" + uri + "}" + name);
		}
		buffer = new StringBuffer();
    }


    public void characters (char ch[], int start, int length) {
    	buffer.append(ch,start,length);
		if (debug) {
			System.out.print("Characters:    \"");
			for (int i = start; i < start + length; i++) {
			    switch (ch[i]) {
				    case '\\':
						System.out.print("\\\\");
						break;
				    case '"':
						System.out.print("\\\"");
						break;
				    case '\n':
						System.out.print("\\n");
						break;
				    case '\r':
						System.out.print("\\r");
						break;
				    case '\t':
						System.out.print("\\t");
						break;
				    default:
						System.out.print(ch[i]);
						break;
			    }
			}
			System.out.print("\"\n");
		}
    }
    
    public String getAttByName(Attributes atts, String name) {
    	String response = null;
    	
		for (int i = 0; i < atts.getLength(); i++) {
			//System.out.println("\tattribute " + i + ": " + atts.getQName(i) + " > " + atts.getValue(i));
			if (name.equals(atts.getQName(i)))
				return atts.getValue(i);
		}

		return response;
    }
    
}

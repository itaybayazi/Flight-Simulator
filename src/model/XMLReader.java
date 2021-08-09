package model;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class XMLReader extends DefaultHandler {

    boolean name;
    public int count;
    public String str;
    File Attributes;
    BufferedWriter out;

    public XMLReader() // Constructor.
    {
        this.name = false;
        this.count = 0;
        this.str = "";
        try {
            this.Attributes = new File("Attributes.txt");
            this.out = new BufferedWriter(new FileWriter(Attributes.getAbsoluteFile()));
        }
        catch(Exception ignored){};
    }


    @Override
    public void startElement(String uri,String localName, String qName, Attributes attributes){ // locate the "name" tag.
        if (qName.equalsIgnoreCase("name"))
            name = true;
    }

    @Override
    public void characters(char[] ch, int start, int length){ // read the data from the XML file.
        if (name) { // only name attributes.
            if(this.count <42) {
                this.str = new String(ch, start, length);
                WriteToFile(this.str,this.count);
            }
            this.count++;
            name = false;
        }
    }

    public void WriteToFile(String str, int index) { // Write data to text file according to the user story.
        try {
            if(str.equals("aileron") || str.equals("elevator") ||str.equals("rudder") ||str.equals("throttle") ||str.equals("latitude-deg") ||str.equals("longitude-deg") ||str.equals("roll-deg") ||str.equals("pitch-deg") ||str.equals("heading-deg") ||str.equals("side-slip-deg") ||str.equals("airspeed-kt") ||str.equals("altimeter_indicated-altitude-ft")) {
                out.write(str);
                out.write(" " + index + "\n");
            }
        }
        catch (Exception ignored){}
    }

    public void FinishWriting() throws IOException {out.close();}
}

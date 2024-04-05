package com.gopalapriyadasa.html_doc;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 * Created by Gopa702 on 7/6/2016.
 */
public class TagFileStream {

    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    private String ltype = "";
    private String lkey = "";
    private String lvalue = "";

    public TagFileStream(String filePath, boolean write) throws IOException {
        if (write) {
            writer = new BufferedWriter(new FileWriter(filePath));
        } else {
            reader = new BufferedReader(new FileReader(filePath));
        }
    }

    public TagFileStream(InputStreamReader rdr) {
        reader = new BufferedReader(rdr);
    }

    public TagFileStream(OutputStreamWriter osw) {
        writer = new BufferedWriter(osw);
    }

    public void close() {
        if (reader != null) {
            try {
                reader.close();
            } catch (Exception e) {
            }
            reader = null;
        }

        if (writer != null) {
            try {
                writer.close();
            } catch(Exception e) {

            }
            writer = null;
        }
    }

    public boolean next() {
        if (reader == null)
            return false;


        String s = null;
        do {
            try {
                s = reader.readLine();
                Log.i("Folio", s);
            } catch (Exception e) {
                s = null;
            }

            if (s == null)
                return false;

            String[] parts = s.split("\t");
            if (parts.length == 3) {
                ltype = parts[0];
                lkey = parts[1];
                lvalue = parts[2].replace("\\t", "\t").replace("\\n", "\n").replace("\\\\", "\\");
                return true;
            } else {
                s = null;
            }
        } while( s == null);

        return false;
    }

    public boolean isAttribute(String key) {
        return ltype.equals("A") && lkey.equals(key);
    }

    public boolean isTag(String key) {
        return ltype.equals("T") && lkey.equals(key);
    }

    public String getValue() {
        return lvalue;
    }

    public void writeTag(String key, String value) {
        writeLine("T", key, value);
    }

    public void writeAttribute(String key, String value) {
        writeLine("A", key, value);
    }

    private void writeLine(String tag, String key, String value) {

        if (writer == null)
            return;

        try {
            String valx = value.replace("\\", "\\\\").replace("\n", "\\n").replace("\t", "\\t");
            writer.write(String.format(Locale.getDefault(), "%s\t%s\t%s\n", tag, key, valx));
            Log.i("Folio", tag + " " + key + " " + valx);
        } catch (Exception x) {

        }
    }
}

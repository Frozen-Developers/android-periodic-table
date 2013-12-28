package com.frozendevs.periodic.table.helper;

import android.content.Context;
import android.util.Xml;

import com.frozendevs.periodic.table.R;
import com.frozendevs.periodic.table.model.ElementDetails;
import com.frozendevs.periodic.table.model.ElementListItem;
import com.frozendevs.periodic.table.model.Isotope;
import com.frozendevs.periodic.table.model.TableItem;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Database {

    private static InputStream getDatabaseFile(Context context) {
        return context.getResources().openRawResource(R.raw.elements);
    }

    private static String readTag(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, null, tag);
        String text = readText(parser);
        parser.require(XmlPullParser.END_TAG, null, tag);
        return text;
    }

    private static String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";

        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }

        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }

        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    public static List<ElementListItem> getElementListItems(Context context) {
        List<ElementListItem> items = new ArrayList<ElementListItem>();

        XmlPullParser parser = Xml.newPullParser();
        InputStream inputStream = getDatabaseFile(context);
        try {
            parser.setInput(inputStream, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, null, "elements");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String tag = parser.getName();

                    if (tag.equals("element")) {
                        parser.require(XmlPullParser.START_TAG, null, "element");

                        int atomicNumber = Integer.valueOf(parser.getAttributeValue(null, "number"));
                        String symbol = null, name = null;

                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.getEventType() == XmlPullParser.START_TAG) {

                                tag = parser.getName();

                                if(tag.equals("symbol"))
                                    symbol = readTag(parser, tag);
                                else if(tag.equals("name"))
                                    name = readTag(parser, tag);
                                else
                                    skip(parser);
                            }
                        }

                        items.add(new ElementListItem(name, symbol, atomicNumber));
                    }
                    else
                        skip(parser);
                }
            }

            inputStream.close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Collections.sort(items, new Comparator<ElementListItem>() {
            @Override
            public int compare(ElementListItem lhs, ElementListItem rhs) {
                return lhs.getAtomicNumber() - rhs.getAtomicNumber();
            }
        });

        return items;
    }

    public static List<TableItem> getTableItems(Context context) {
        List<TableItem> items = new ArrayList<TableItem>();

        XmlPullParser parser = Xml.newPullParser();
        InputStream inputStream = getDatabaseFile(context);
        try {
            parser.setInput(inputStream, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, null, "elements");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String tag = parser.getName();

                    if (tag.equals("element")) {
                        parser.require(XmlPullParser.START_TAG, null, "element");

                        int atomicNumber = Integer.valueOf(parser.getAttributeValue(null, "number"));
                        String symbol = null, name = null, weight = null, category = null;
                        int group = 0, period = 0;

                        while (parser.next() != XmlPullParser.END_TAG) {
                            if (parser.getEventType() == XmlPullParser.START_TAG) {

                                tag = parser.getName();

                                if(tag.equals("symbol"))
                                    symbol = readTag(parser, tag);
                                else if(tag.equals("name"))
                                    name = readTag(parser, tag);
                                else if(tag.equals("weight"))
                                    weight = readTag(parser, tag);
                                else if(tag.equals("group"))
                                    group = Integer.valueOf(readTag(parser, tag));
                                else if(tag.equals("period"))
                                    period = Integer.valueOf(readTag(parser, tag));
                                else if(tag.equals("category"))
                                    category = readTag(parser, tag);
                                else
                                    skip(parser);
                            }
                        }

                        items.add(new TableItem(name, symbol, atomicNumber, weight, group, period, category));
                    }
                    else
                        skip(parser);
                }
            }

            inputStream.close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return items;
    }

    public static ElementDetails getElementDetails(Context context, int atomicNumber) {
        ElementDetails details = null;

        XmlPullParser parser = Xml.newPullParser();
        InputStream inputStream = getDatabaseFile(context);
        try {
            parser.setInput(inputStream, null);
            parser.nextTag();

            parser.require(XmlPullParser.START_TAG, null, "elements");
            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    String tag = parser.getName();

                    if (tag.equals("element")) {
                        parser.require(XmlPullParser.START_TAG, null, "element");

                        if(atomicNumber == Integer.valueOf(parser.getAttributeValue(null, "number"))) {
                            String symbol = null, name = null, weight = null, category = null, wiki = null;
                            int group = 0, period = 0;

                            List<Isotope> isotopes = new ArrayList<Isotope>();

                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.getEventType() == XmlPullParser.START_TAG) {

                                    tag = parser.getName();

                                    if(tag.equals("symbol"))
                                        symbol = readTag(parser, tag);
                                    else if(tag.equals("name"))
                                        name = readTag(parser, tag);
                                    else if(tag.equals("weight"))
                                        weight = readTag(parser, tag);
                                    else if(tag.equals("group"))
                                        group = Integer.valueOf(readTag(parser, tag));
                                    else if(tag.equals("period"))
                                        period = Integer.valueOf(readTag(parser, tag));
                                    else if(tag.equals("category"))
                                        category = readTag(parser, tag);
                                    else if(tag.equals("wiki"))
                                        wiki = readTag(parser, tag);
                                    else if(tag.equals("isotopes")) {
                                        parser.require(XmlPullParser.START_TAG, null, "isotopes");
                                        while (parser.next() != XmlPullParser.END_TAG) {
                                            if (parser.getEventType() == XmlPullParser.START_TAG) {
                                                tag = parser.getName();

                                                if (tag.equals("isotope")) {
                                                    parser.require(XmlPullParser.START_TAG, null, "isotope");

                                                    String isoSymbol = parser.getAttributeValue(null, "symbol");
                                                    String halfLife = null, decayModes = null, daughterIsotopes = null,
                                                            spin = null, abundance = null;

                                                    while (parser.next() != XmlPullParser.END_TAG) {
                                                        if (parser.getEventType() == XmlPullParser.START_TAG) {

                                                            tag = parser.getName();

                                                            if(tag.equals("half-life"))
                                                                halfLife = readTag(parser, tag);
                                                            else if(tag.equals("decay-modes"))
                                                                decayModes = readTag(parser, tag);
                                                            else if(tag.equals("daughter-isotopes"))
                                                                daughterIsotopes = readTag(parser, tag);
                                                            else if(tag.equals("spin"))
                                                                spin = readTag(parser, tag);
                                                            else if(tag.equals("abundance"))
                                                                abundance = readTag(parser, tag);
                                                            else
                                                                skip(parser);
                                                        }
                                                    }

                                                    isotopes.add(new Isotope(isoSymbol, halfLife, decayModes.split("\n"),
                                                            daughterIsotopes.split("\n"), spin, abundance));
                                                }
                                            }
                                        }
                                    }
                                    else
                                        skip(parser);
                                }
                            }

                            details = new ElementDetails(name, symbol, atomicNumber, weight, group,
                                    period, category, wiki, isotopes.toArray(new Isotope[isotopes.size()]));
                        }
                        else
                            skip(parser);
                    }
                    else
                        skip(parser);
                }
            }

            inputStream.close();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return details;
    }
}

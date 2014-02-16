package com.frozendevs.periodictable.helper;

import android.content.Context;
import android.util.Xml;

import com.frozendevs.periodictable.R;
import com.frozendevs.periodictable.model.BasicElementProperties;
import com.frozendevs.periodictable.model.ElementProperties;
import com.frozendevs.periodictable.model.ElementListItem;
import com.frozendevs.periodictable.model.Isotope;
import com.frozendevs.periodictable.model.TableItem;

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

    private static String readTag(XmlPullParser parser, String tag) throws IOException,
            XmlPullParserException {
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

    public static BasicElementProperties getBasicElementProperties(Context context, int element) {
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

                        if(Integer.valueOf(parser.getAttributeValue(null, "number")) == element) {
                            String wikiLink = null, name = null;

                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.getEventType() == XmlPullParser.START_TAG) {

                                    tag = parser.getName();

                                    if(tag.equals("name"))
                                        name = readTag(parser, tag);
                                    else if(tag.equals("wiki"))
                                        wikiLink = readTag(parser, tag);
                                    else
                                        skip(parser);
                                }
                            }

                            return new BasicElementProperties(name, wikiLink);
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

        return null;
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

                        items.add(new TableItem(name, symbol, atomicNumber, weight, group, period,
                                category));
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

    public static ElementProperties getElementProperties(Context context, int atomicNumber) {
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
                            String symbol = null, name = null, weight = null, block = null,
                                    category = null, configuration = null, wiki = null,
                                    appearance = null, phase = null, density = null,
                                    liquidDensityAtMeltingPoint = null,
                                    liquidDensityAtBoilingPoint = null, meltingPoint = null,
                                    boilingPoint = null, triplePoint = null, criticalPoint = null,
                                    heatOfFusion = null, heatOfVaporization = null,
                                    molarHeatCapacacity = null;
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
                                    else if(tag.equals("block"))
                                        block = readTag(parser, tag);
                                    else if(tag.equals("category"))
                                        category = readTag(parser, tag);
                                    else if(tag.equals("configuration"))
                                        configuration = readTag(parser, tag);
                                    else if(tag.equals("wiki"))
                                        wiki = readTag(parser, tag);
                                    else if(tag.equals("appearance"))
                                        appearance = readTag(parser, tag);
                                    else if(tag.equals("phase"))
                                        phase = readTag(parser, tag);
                                    else if(tag.equals("density"))
                                        density = readTag(parser, tag);
                                    else if(tag.equals("density-at-mp"))
                                        liquidDensityAtMeltingPoint = readTag(parser, tag);
                                    else if(tag.equals("density-at-bp"))
                                        liquidDensityAtBoilingPoint = readTag(parser, tag);
                                    else if(tag.equals("melting-point"))
                                        meltingPoint = readTag(parser, tag);
                                    else if(tag.equals("boiling-point"))
                                        boilingPoint = readTag(parser, tag);
                                    else if(tag.equals("triple-point"))
                                        triplePoint = readTag(parser, tag);
                                    else if(tag.equals("critical-point"))
                                        criticalPoint = readTag(parser, tag);
                                    else if(tag.equals("heat-of-fusion"))
                                        heatOfFusion = readTag(parser, tag);
                                    else if(tag.equals("heat-of-vaporization"))
                                        heatOfVaporization = readTag(parser, tag);
                                    else if(tag.equals("molar-heat-capacity"))
                                        molarHeatCapacacity = readTag(parser, tag);
                                    else
                                        skip(parser);
                                }
                            }

                            return new ElementProperties(name, symbol, atomicNumber, weight, group,
                                    period, block, category, configuration, wiki, appearance, phase,
                                    density, liquidDensityAtMeltingPoint, liquidDensityAtBoilingPoint,
                                    meltingPoint, boilingPoint, triplePoint, criticalPoint,
                                    heatOfFusion, heatOfVaporization, molarHeatCapacacity);
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

        return null;
    }

    public static Isotope[] getIsotopes(Context context, int element) {
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

                        if(Integer.valueOf(parser.getAttributeValue(null, "number")) == element) {
                            List<Isotope> isotopes = new ArrayList<Isotope>();

                            while (parser.next() != XmlPullParser.END_TAG) {
                                if (parser.getEventType() == XmlPullParser.START_TAG) {
                                    if(parser.getName().equals("isotopes")) {
                                        parser.require(XmlPullParser.START_TAG, null, "isotopes");
                                        while (parser.next() != XmlPullParser.END_TAG) {
                                            if (parser.getEventType() == XmlPullParser.START_TAG) {
                                                if (parser.getName().equals("isotope")) {
                                                    parser.require(XmlPullParser.START_TAG, null,
                                                            "isotope");

                                                    String symbol = parser.getAttributeValue(null,
                                                            "symbol");
                                                    String halfLife = null, decayModes = null,
                                                            daughterIsotopes = null, spin = null,
                                                            abundance = null;

                                                    while (parser.next() != XmlPullParser.END_TAG) {
                                                        if (parser.getEventType() ==
                                                                XmlPullParser.START_TAG) {

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

                                                    isotopes.add(new Isotope(symbol, halfLife,
                                                            decayModes, daughterIsotopes, spin,
                                                            abundance));
                                                }
                                                else
                                                    skip(parser);
                                            }
                                        }
                                    }
                                    else
                                        skip(parser);
                                }
                            }

                            return isotopes.toArray(new Isotope[isotopes.size()]);
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

        return null;
    }
}

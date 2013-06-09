package com.example.android.networkusage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

/**
 * This class parses XML feeds from Yahoo Weather. It is a modified version the
 * code found at the following url courtesy of the Android developer's blog.
 * http://developer.android.com/training/basics/network-ops/managing.html
 */
public class YahooWeatherXmlParser {
	private static final String ns = null;

	// We don't use namespaces

	public List<Entry> parse(InputStream in) throws XmlPullParserException,
			IOException {
		try {
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);
			parser.nextTag();
			return readRSS(parser);
		} finally {
			in.close();
		}
	}

	private List<Entry> readRSS(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		List<Entry> entries = new ArrayList<Entry>();

		parser.require(XmlPullParser.START_TAG, ns, "rss");
		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();
			// Starts by looking for the channel tag
			if (name.equals("channel")) {
				entries = readChannel(parser);
			} else {
				skip(parser);
			}
		}
		return entries;
	}

	/*
	 * This class represents a forecast entry from the XML data
	 */

	public static class Entry {
		public final String day;
		public final String date;
		public final String low;
		public final String high;
		public final String text;

		private Entry(String day, String date, String low, String high,
				String text) {
			this.day = day;
			this.date = date;
			this.low = low;
			this.high = high;
			this.text = text;
		}
	}

	// Parses the contents of an entry. 
	private List<Entry> readChannel(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "channel");
		List<Entry> entries = new ArrayList<Entry>();

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();

			if (name.equals("item")) {
				entries = readItem(parser);
			} else {
				skip(parser);
			}
		}
		return entries;
	}

	// Parses the contents of an entry. 
	private List<Entry> readItem(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		parser.require(XmlPullParser.START_TAG, ns, "item");
		List<Entry> entries = new ArrayList<Entry>();
		String day = null;
		String date = null;
		String low = null;
		String high = null;
		String text = null;

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG) {
				continue;
			}
			String name = parser.getName();

			if (name.equals("yweather:forecast")) {
				day = parser.getAttributeValue(null, "day");
				date = parser.getAttributeValue(null, "date");
				low = parser.getAttributeValue(null, "low");
				high = parser.getAttributeValue(null, "high");
				text = parser.getAttributeValue(null, "text");

				entries.add(new Entry(day, date, low, high, text));

				parser.nextTag();

			} else {
				skip(parser);
			}
		}
		return entries;
	}

	private void skip(XmlPullParser parser) throws XmlPullParserException,
			IOException {
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
}

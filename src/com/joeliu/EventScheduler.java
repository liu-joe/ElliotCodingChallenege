/**
 * Elliot Technologies Coding Challenge
 * Event Scheduler
 *
 * By: Zhenyu Liu
 * Email: z324liu@uwaterloo.ca
 *
 * ==============================
 * OVERVIEW
 * ==============================
 *
 * This app takes a series of input (representing meetings), and determines the longest block of time between 8 am
 * to 10 pm in which all users are free to meet within the next 7 days
 *
 * Since we are looking for a block of time in which ALL users are free, the user id information is ignored as it
 * is irrelevant to the problem
 *
 * The algorithm involves a linked list in which each node represents a "busy zone", busy zones are time slots in which
 * meetings are not schedule-able, this includes when at least 1 user is in a meeting or when the time is between
 * 10 pm to 8 am
 */

package com.joeliu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

public class EventScheduler {
	static LinkedList<Node> nodes;
	static long startTime;
	static long endTime;

	public static void main(String[] args) {
		nodes = new LinkedList<>();
		final String filename = "calendar.csv";

		// Set the boundaries of our search, startTime is when the program is run, and endTime is 7 days from
		// the start at end-of-day
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		startTime = c.getTimeInMillis();
		c.add(Calendar.DATE, 7);
		c.set(Calendar.HOUR_OF_DAY, 22);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		endTime = c.getTimeInMillis();

		// Fill in all hours between 10pm to 8am to avoid finding meeting time out of business hours
		fillOffHours();

		// Read all data entries from csv file and process their meeting times
		BufferedReader fileReader;
		try {
			String line;
			fileReader = new BufferedReader(new FileReader(filename));

			while ((line = fileReader.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length != 3) {
					throw new RuntimeException(line + " is expected to have 3 values");
				}

				// create a node of time to represent a meeting
				Node node = createNode(getUnixTime(tokens[1]), getUnixTime(tokens[2]));
				// add the node and merge any potentially overlapping nodes in the linked list
				addAndMerge(node);
			}
		} catch (Exception e) {
			System.err.println("Error reading file: " + filename);
			throw new RuntimeException(e);
		}

		// Determine the longest gap available, and output the result
		System.out.println(String.format("The longest empty gap takes place between: %s", getLongestGap()));
	}

	private static Node getLongestGap() {
		long longestGapStart = 0;
		long longestGapEnd = 0;

		for (int idx = 0; idx < nodes.size() - 1; ++idx) {
			if (nodes.get(idx + 1).start - nodes.get(idx).end > longestGapEnd - longestGapStart) {
				longestGapStart = nodes.get(idx).end;
				longestGapEnd = nodes.get(idx + 1).start;
			}
		}

		return createNode(longestGapStart, longestGapEnd);
	}

	private static void addAndMerge(Node node) {
		for (int idx = 0; idx < nodes.size(); ++idx) {
			if (nodes.get(idx).start > node.start) {
				nodes.add(idx, node);
				merge();
				return;
			}
		}
		nodes.addLast(node);
		merge();
	}

	private static void merge() {
		int initialSize;
		do {
			initialSize = nodes.size();
			for (int idx = 0; idx < nodes.size() - 1; ++idx) {
				// if there is overlap between 2 nodes, combine them into 1
				if (nodes.get(idx).end >= nodes.get(idx + 1).start) {
					nodes.get(idx).end = nodes.get(idx).end > nodes.get(idx + 1).end ?
							nodes.get(idx).end : nodes.get(idx + 1).end;
					nodes.remove(idx + 1);
					break;
				}
			}
		//repeat this process until nodes has no more overlaps
		} while (initialSize != nodes.size());
	}

	private static void fillOffHours() {
		long time1;
		long time2;

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.set(Calendar.HOUR_OF_DAY, 8);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		// If program is run before 8 am, mark the period until 8 am to be un-schedule-able
		if (startTime < c.getTimeInMillis()) {
			addAndMerge(createNode(startTime, c.getTimeInMillis()));
		}

		boolean reachEndDate = false;
		// Mark all the "off hours" between 10 pm to 8 am to be un-schedule-able
		while (!reachEndDate) {
			c.set(Calendar.HOUR_OF_DAY, 22);
			// If program is run after 10pm, mark this day un-schedule-able from current time instead of 10 pm
			if (startTime > c.getTimeInMillis()) {
				c.setTime(new Date(startTime));
			}
			time1 = c.getTimeInMillis();

			c.add(Calendar.DATE, 1);
			c.set(Calendar.HOUR_OF_DAY, 8);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			time2 = c.getTimeInMillis();

			if (time1 >= endTime) {
				reachEndDate = true;
			} else {
				addAndMerge(createNode(time1, time2));
			}
		}
	}

	private static long getUnixTime(String dateString) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			System.err.println("Error parsing date: " + dateString);
			throw new RuntimeException(e);
		}
		long unixTime = date.getTime();

		return unixTime;
	}

	private static Node createNode(long start, long end) {
		long time1, time2;
		// When we create a new node, we cut off any time that is not within the boundaries of our search
		time1 = start > endTime ? endTime : start;
		time2 = end > endTime ? endTime : end;
		time1 = time1 < startTime ? startTime : time1;
		time2 = time2 < startTime ? startTime : time2;
		return new EventScheduler(). new Node(time1, time2);
	}

	/**
	 * Class to represent a block of time
	 */
	private class Node {
		long start;
		long end;

		private Node(long start, long end) {
			this.start = start;
			this.end = end;
		}

		@Override
		public String toString() {
			return String.format("{start:%s, end:%s}", new Date(start), new Date(end));
		}
	}
}

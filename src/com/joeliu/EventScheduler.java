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

		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		startTime = c.getTimeInMillis();
		c.add(Calendar.DATE, 7);
		c.set(Calendar.HOUR_OF_DAY, 22);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		endTime = c.getTimeInMillis();

		fillOffHours();

		BufferedReader fileReader;
		try {
			String line;
			fileReader = new BufferedReader(new FileReader(filename));

			while ((line = fileReader.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length != 3) {
					throw new RuntimeException(line + " is expected to have 3 values");
				}

				Node node = createNode(getUnixTime(tokens[1]), getUnixTime(tokens[2]));
				addAndMerge(node);
			}
		} catch (Exception e) {

		}

		System.out.println(String.format("Time periods in which someone is busy: %s", nodes));
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
				if (nodes.get(idx).end >= nodes.get(idx + 1).start) {
					nodes.get(idx).end = nodes.get(idx).end > nodes.get(idx + 1).end ?
							nodes.get(idx).end : nodes.get(idx + 1).end;
					nodes.remove(idx + 1);
					break;
				}
			}
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
		if (startTime < c.getTimeInMillis()) {
			addAndMerge(createNode(startTime, c.getTimeInMillis()));
		}

		boolean reachEndDate = false;
		while (!reachEndDate) {
			c.set(Calendar.HOUR_OF_DAY, 22);
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
		time1 = start > endTime ? endTime : start;
		time2 = end > endTime ? endTime : end;
		time1 = time1 < startTime ? startTime : time1;
		time2 = time2 < startTime ? startTime : time2;
		return new EventScheduler(). new Node(time1, time2);
	}

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

package com.joeliu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

public class Calendar {
	static LinkedList<Node> nodes;

	public static void main(String[] args) {
		nodes = new LinkedList<>();
		final String filename = "calendar.csv";

		long startTime = getUnixTime("2017-04-03 00:00:00"); //TODO: replace with current time
		long endTime = getUnixTime("2017-04-03 23:59:59"); //TODO: replace with 1 week after start time

		nodes.add(createNode(getUnixTime("2017-04-03 00:00:00"), getUnixTime("2017-04-03 08:00:00")));
		nodes.add(createNode(getUnixTime("2017-04-03 20:00:00"), getUnixTime("2017-04-03 23:59:59")));

		BufferedReader fileReader;
		try {
			String line;
			fileReader = new BufferedReader(new FileReader(filename));

			while ((line = fileReader.readLine()) != null) {
				String[] tokens = line.split(",");
				if (tokens.length != 3) {
					throw new RuntimeException(line + " is expected to have 3 values");
				}

				//TODO: remove debugging output
				System.out.print(tokens[1]);
				System.out.println(tokens[2]);

				long time1, time2;
				time1 = getUnixTime(tokens[1]) > endTime ? endTime : getUnixTime(tokens[1]);
				time2 = getUnixTime(tokens[2]) > endTime ? endTime : getUnixTime(tokens[2]);
				time1 = time1 < startTime ? startTime : time1;
				time2 = time2 < startTime ? startTime : time2;

				Node node = createNode(time1, time2);
				addAndMerge(node);
			}
		} catch (Exception e) {

		}

		System.out.println(nodes);
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
					nodes.get(idx).end = nodes.get(idx + 1).end;
					nodes.remove(idx + 1);
					break;
				}
			}
		} while (initialSize != nodes.size());
	}

	private static long getUnixTime(String dateString) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
		Date date;
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			System.err.println("Error parsing date: " + dateString);
			throw new RuntimeException(e);
		}
		long unixTime = date.getTime() / 1000;

		return unixTime;
	}

	private static Node createNode(long start, long end) {
		return new Calendar(). new Node(start, end);
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
			return String.format("{start:%s, end:%s}", new Date(start * 1000), new Date(end * 1000));
		}
	}
}

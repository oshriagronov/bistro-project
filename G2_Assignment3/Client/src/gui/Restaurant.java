package gui;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import communication.BistroResponse;
import communication.RequestFactory;
import logic.Table;

/**
 * Utility class that provides restaurant-related data and calculations used by
 * the GUI.
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Fetch opening/closing hours for a given date</li>
 * <li>Fetch existing reservations in a 4-hour range for each timeslot</li>
 * <li>Load and cache table sizes from the server</li>
 * <li>Build a mapping from timeslots to the sorted list of diner group
 * sizes</li>
 * </ul>
 *
 * <p>
 * Note: This class performs synchronous server calls through
 * {@code Main.client}.
 */
public class Restaurant {

	/**
	 * Cached list of table sizes sorted in ascending order.
	 * <p>
	 * The cache is lazily loaded on first access via {@link #getTableSizes()}.
	 */
	private static final List<Integer> tableSizes = new ArrayList<>();

	/**
	 * Builds a time-to-diners mapping for the given date.
	 * <p>
	 * For each valid start time (in 30-minute increments) between opening time and
	 * the last possible start time (closing minus 2 hours), the method queries the
	 * server for existing reservations in the next 4-hour range and stores the
	 * resulting diner group sizes sorted in ascending order.
	 *
	 * <p>
	 * The returned map is a {@link TreeMap} to keep timeslots ordered
	 * chronologically.
	 *
	 * @param date the date for which to build the diners-by-time mapping
	 * @return a map from timeslots to a sorted list of diner group sizes; empty if
	 *         opening hours are unavailable
	 */
	public static Map<LocalTime, List<Integer>> buildDinersByTime(LocalDate date) {
		Map<LocalTime, List<Integer>> map = new TreeMap<>();

		LocalTime[] hours = getOpeningTime(date);
		if (hours == null) {
			return map;
		}

		LocalTime opening = hours[0];
		LocalTime closing = hours[1];

		// Last reservation start time is 2 hours before closing.
		// If closing is midnight, treat closing as 24:00 and last start as 22:00.
		LocalTime lastStart = closing.equals(LocalTime.MIDNIGHT) ? LocalTime.of(22, 0) : closing.minusHours(2);

		for (LocalTime t = opening; !t.isAfter(lastStart); t = t.plusMinutes(30)) {
			map.put(t, new ArrayList<>());
		}

		for (LocalTime t : map.keySet()) {
			Main.client.accept(RequestFactory.getOrderIn4HoursRange(date, t));
			Object data = Main.client.getResponse().getData();

			if (data instanceof List<?>) {
				List<Integer> diners = (List<Integer>) data;
				diners.sort(Integer::compareTo);
				map.put(t, diners);
			}
		}

		return map;
	}

	/**
	 * Determines whether the restaurant can accommodate the given diner groups in a
	 * single time slot using the currently loaded {@link #tablesSizes}.
	 * <p>
	 * The algorithm assumes:
	 * </p>
	 * <ul>
	 * <li>{@link #tablesSizes} is sorted in ascending order.</li>
	 * <li>Each diner group requires one table with capacity
	 * {@code >= group size}.</li>
	 * <li>Tables are used at most once per time slot.</li>
	 * </ul>
	 *
	 * @param diners a sorted list of diner group sizes occupying that time slot
	 * @return {@code true} if all groups can be seated; otherwise {@code false}
	 */
	public static boolean isAvailable(List<Integer> diners, List<Integer> tablesSizes) {
		int i = 0;

		if (diners.size() > tablesSizes.size()) {
			return false;
		}

		for (int num : diners) {
			boolean found = false;

			while (i < tablesSizes.size()) {
				if (num <= tablesSizes.get(i)) {
					found = true;
					i++;
					break;
				}
				i++;
			}

			if (i == tablesSizes.size() && !found) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Loads the restaurant tables from the server and refreshes the cached list of
	 * table sizes.
	 * <p>
	 * The resulting sizes list is sorted in ascending order.
	 */
	private static void loadTables() {
		Main.client.accept(RequestFactory.getTables());
		BistroResponse response = Main.client.getResponse();
		Object data = response.getData();

		tableSizes.clear();

		if (data instanceof List<?>) {

			List<Table> list = (List<Table>) data;

			for (Table t : list) {
				tableSizes.add(t.getTable_size());
			}

			tableSizes.sort(Integer::compareTo);
		}
	}

	/**
	 * Returns a copy of the cached table sizes list.
	 * <p>
	 * If the cache is empty, it is loaded from the server first.
	 *
	 * @return a new list containing all cached table sizes in ascending order
	 */
	public static List<Integer> getTableSizes() {
		if (tableSizes.isEmpty()) {
			loadTables();
		}
		return new ArrayList<>(tableSizes);
	}

	public static boolean isAvailable(List<Integer> diners, List<Integer> tablesSizes) {
		int i = 0;
		if (diners.size() > tablesSizes.size()) {
			return false;
		}

		for (int num : diners) {
			boolean found = false;

			while (i < tablesSizes.size()) {
				if (num <= tablesSizes.get(i)) {
					found = true;
					i++;
					break;
				}
				i++;
			}
			if (i == tablesSizes.size() && !found) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Fetches the opening and closing times for a given date.
	 *
	 * @param date the date for which to fetch opening hours
	 * @return an array {@code [opening, closing]} if available; otherwise
	 *         {@code null}
	 */
	public static LocalTime[] getOpeningTime(LocalDate date) {
		Main.client.accept(RequestFactory.getOpeningHours(date));
		Object data = Main.client.getResponse().getData();
		return (data instanceof LocalTime[]) ? (LocalTime[]) data : null;
	}
}

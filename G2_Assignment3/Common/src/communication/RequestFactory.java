package communication;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;

import logic.SpecialDay;
import logic.WeekDay;
import logic.WeeklySchedule;

/**
 * Factory class for creating {@link BistroRequest} objects in a safe and
 * consistent manner.
 * <p>
 * This class centralizes the construction of client-to-server requests and
 * hides the details of {@link BistroCommand} values and payload structures from
 * the UI layer.
 * </p>
 *
 * <p>
 * Responsibilities:
 * <ul>
 * <li>Provide intention-revealing methods for each supported operation</li>
 * <li>Validate input parameters before request creation</li>
 * <li>Ensure payloads are constructed in the correct format</li>
 * <li>Reduce duplication and prevent command/payload mismatches</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class follows the <b>Factory Method</b> design pattern and is
 * implemented as a utility class (static methods only).
 * </p>
 */
public final class RequestFactory {

	/**
	 * Private constructor to prevent instantiation.
	 */
	private RequestFactory() {
	}

	/**
	 * Creates a {@link BistroRequest} with an explicit payload.
	 *
	 * @param cmd     the command to send to the server
	 * @param payload the payload object associated with the command
	 * @return a fully constructed {@link BistroRequest}
	 */
	public static BistroRequest withPayload(BistroCommand cmd, Object payload) {
		return new BistroRequest(cmd, payload);
	}

	/**
	 * Creates a {@link BistroRequest} without a payload.
	 *
	 * @param cmd the command to send to the server
	 * @return a {@link BistroRequest} with a {@code null} payload
	 */
	public static BistroRequest noPayload(BistroCommand cmd) {
		return new BistroRequest(cmd, null);
	}

	// -------------------------------------------------------------------------
	// Workers / employees
	// -------------------------------------------------------------------------

	/**
	 * Creates a request to authenticate a worker by username and password.
	 * <p>
	 * The payload is wrapped in {@link WorkerLoginRequest}.
	 * </p>
	 *
	 * @param username worker username
	 * @param password worker raw password
	 * @return a {@link BistroRequest} for {@link BistroCommand#WORKER_LOGIN}
	 */
	public static BistroRequest workerLogin(String username, String password) {
		return withPayload(BistroCommand.WORKER_LOGIN, new WorkerLoginRequest(username, password));
	}

	// -------------------------------------------------------------------------
	// Tables
	// -------------------------------------------------------------------------

	/**
	 * Creates a request to retrieve all tables from the system.
	 *
	 * @return a {@link BistroRequest} for {@link BistroCommand#GET_TABLES}
	 */
	public static BistroRequest getTables() {
		return noPayload(BistroCommand.GET_TABLES);
	}

	/**
	 * Creates a request to add a new table with the specified size.
	 *
	 * @param size number of seats for the new table
	 * @return a {@link BistroRequest} for {@link BistroCommand#ADD_TABLE}
	 * @throws IllegalArgumentException if {@code size <= 0}
	 */
	public static BistroRequest addTable(int size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Table size must be positive");
		}
		return withPayload(BistroCommand.ADD_TABLE, size);
	}

	/**
	 * Creates a request to delete an existing table by its table number.
	 *
	 * @param tableNumber the table number to delete
	 * @return a {@link BistroRequest} for {@link BistroCommand#DELETE_TABLE}
	 * @throws IllegalArgumentException if {@code tableNumber <= 0}
	 */
	public static BistroRequest deleteTable(int tableNumber) {
		if (tableNumber <= 0) {
			throw new IllegalArgumentException("Table number must be positive");
		}
		return withPayload(BistroCommand.DELETE_TABLE, tableNumber);
	}

	/**
	 * Creates a request to change the size of an existing table.
	 *
	 * @param tableNumber the table number to update
	 * @param newSize     the new number of seats
	 * @return a {@link BistroRequest} for {@link BistroCommand#CHANGE_TABLE_SIZE}
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public static BistroRequest changeTableSize(int tableNumber, int newSize) {
		if (tableNumber <= 0) {
			throw new IllegalArgumentException("Table number must be positive");
		}
		return withPayload(BistroCommand.CHANGE_TABLE_SIZE, new TableSizeUpdate(tableNumber, newSize));
	}

	// -------------------------------------------------------------------------
	// Current diners
	// -------------------------------------------------------------------------

	/**
	 * Creates a request to load the current diners per table.
	 *
	 * @return a {@link BistroRequest} for {@link BistroCommand#LOAD_DINERS}
	 */
	public static BistroRequest loadCurrentDiners() {
		return noPayload(BistroCommand.LOAD_DINERS);
	}

	// -------------------------------------------------------------------------
	// Orders / reservations
	// -------------------------------------------------------------------------

	/**
	 * Creates a request to cancel an existing reservation.
	 *
	 * @param resId reservation ID to cancel
	 * @return a {@link BistroRequest} for {@link BistroCommand#CANCEL_RESERVATION}
	 * @throws IllegalArgumentException if {@code resId <= 0}
	 */
	public static BistroRequest cancelReservation(int resId) {
		if (resId <= 0) {
			throw new IllegalArgumentException("Reservation id must be positive");
		}
		return withPayload(BistroCommand.CANCEL_RESERVATION, resId);
	}

	/**
	 * Creates a request to change the status of an existing reservation.
	 *
	 * @param phone  phone number associated with the reservation
	 * @param resId  reservation ID
	 * @param status new reservation status
	 * @return a {@link BistroRequest} for {@link BistroCommand#CHANGE_STATUS}
	 * @throws IllegalArgumentException if any argument is invalid
	 */
	public static BistroRequest changeStatus(String phone, int resId, logic.Status status) {
		if (phone == null || phone.isBlank()) {
			throw new IllegalArgumentException("Phone is required");
		}
		if (resId <= 0) {
			throw new IllegalArgumentException("Reservation id must be positive");
		}
		if (status == null) {
			throw new IllegalArgumentException("Status is required");
		}
		return withPayload(BistroCommand.CHANGE_STATUS, new StatusUpdate(phone, resId, status));
	}

	/**
	 * Creates a request to retrieve active reservations by phone number.
	 *
	 * @param phone the phone number to search by
	 * @return a {@link BistroRequest} for
	 *         {@link BistroCommand#GET_ACTIVE_RESERVATIONS_BY_PHONE}
	 * @throws IllegalArgumentException if {@code phone} is null or blank
	 */
	public static BistroRequest getActiveReservationsByPhone(String phone) {
		if (phone == null || phone.isBlank()) {
			throw new IllegalArgumentException("Phone is required");
		}
		return withPayload(BistroCommand.GET_ACTIVE_RESERVATIONS_BY_PHONE, phone);
	}

	/**
	 * Creates a request to retrieve reservations by email address.
	 * <p>
	 * The server is expected to return all reservations whose {@code email} matches
	 * the provided value.
	 * </p>
	 *
	 * @param email the email address to search by
	 * @return a {@link BistroRequest} for
	 *         {@link BistroCommand#GET_RESERVATIONS_BY_EMAIL}
	 * @throws IllegalArgumentException if {@code email} is null or blank
	 */
	public static BistroRequest getReservationsByEmail(String email) {
		if (email == null || email.isBlank()) {
			throw new IllegalArgumentException("Email is required");
		}
		return withPayload(BistroCommand.GET_RESERVATIONS_BY_EMAIL, email.trim());
	}
	
	// -------------------------------------------------------------------------
	// Subscribers
	// -------------------------------------------------------------------------
	public static BistroRequest getSubscriberById(int id) {
		return withPayload(BistroCommand.GET_SUB, id);
	}
	

	// -------------------------------------------------------------------------
	// Accept table
	// -------------------------------------------------------------------------

	/**
	 * Creates a request to retrieve a table assignment using phone number and
	 * confirmation code.
	 *
	 * @param phone customer's phone number
	 * @param code  confirmation code
	 * @return a {@link BistroRequest} for
	 *         {@link BistroCommand#GET_TABLE_BY_PHONE_AND_CODE}
	 * @throws IllegalArgumentException if parameters are invalid
	 */
	public static BistroRequest getTableByIdentifierAndCode(String identifier, String code) {
		if (identifier == null || identifier.isBlank()) {
			throw new IllegalArgumentException("Phone is required");
		}
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("Code is required");
		}

		ArrayList<String> search = new ArrayList<>(2);
		search.add(identifier);
		search.add(code);

		return withPayload(BistroCommand.GET_TABLE_BY_IDENTIFIER_AND_CODE, search);
	}

	/**
	 * Creates a request to retrieve a forgotten confirmation code for a given phone
	 * number.
	 *
	 * @param phone customer's phone number
	 * @return a {@link BistroRequest} for
	 *         {@link BistroCommand#FORGOT_CONFIRMATION_CODE}
	 * @throws IllegalArgumentException if {@code phone} is null or blank
	 */
	public static BistroRequest forgotConfirmationCode(String phone) {
		if (phone == null || phone.isBlank()) {
			throw new IllegalArgumentException("Phone is required");
		}
		return withPayload(BistroCommand.FORGOT_CONFIRMATION_CODE, phone);
	}

	// -------------------------------------------------------------------------
	// Reports
	// -------------------------------------------------------------------------

	/**
	 * Creates a request to retrieve the monthly arrival timing report (on-time,
	 * late, cancelled) for the given year-month.
	 *
	 * @param ym the year-month to generate the report for
	 * @return a {@link BistroRequest} for {@link BistroCommand#GET_TIMINGS}
	 * @throws IllegalArgumentException if {@code ym} is null
	 */
	public static BistroRequest getMonthlyArrivalTimesReport(YearMonth ym) {
		if (ym == null) {
			throw new IllegalArgumentException("YearMonth is required");
		}
		return withPayload(BistroCommand.GET_TIMINGS, ym);
	}

	/**
	 * Creates a request to retrieve the monthly average staying time report for
	 * completed reservations in the given year-month.
	 *
	 * @param ym the year-month to generate the report for
	 * @return a {@link BistroRequest} for {@link BistroCommand#GET_STAYING_TIMES}
	 * @throws IllegalArgumentException if {@code ym} is null
	 */
	public static BistroRequest getMonthlyStayingTimesReport(YearMonth ym) {
		if (ym == null) {
			throw new IllegalArgumentException("YearMonth is required");
		}
		return withPayload(BistroCommand.GET_STAYING_TIMES, ym);
	}

	// -------------------------------------------------------------------------
	// Schedule
	// -------------------------------------------------------------------------

	public static BistroRequest getWeeklySchedule() {
		return noPayload(BistroCommand.LOAD_WEEKLY_SCHEDULE);
	}

	public static BistroRequest getSpecialDates(int limit) {
		return new BistroRequest(BistroCommand.LOAD_SPECIAL_DATES, limit);
	}

	public static BistroRequest updateRegularDayTimes(DayOfWeek day, LocalTime open, LocalTime close) {
		WeeklySchedule payload = new WeeklySchedule(day, open, close);
		return new BistroRequest(BistroCommand.UPDATE_REGULAR_SCHEDULE, payload);
	}

	public static BistroRequest updateSpecialDayTimes(LocalDate day, LocalTime open, LocalTime close) {
		SpecialDay payload = new SpecialDay(day, open, close);
		return new BistroRequest(BistroCommand.UPDATE_SPECIAL_DAY, payload);
	}

	public static BistroRequest getOpeningHours(LocalDate date) {

		return withPayload(BistroCommand.GET_OPENING_HOURS, date);
	}

	public static BistroRequest getOrderIn4HoursRange(LocalDate date, LocalTime time) {
		return withPayload(BistroCommand.GET_ORDERS_IN_RANGE, new OrdersInRangeRequest(date, time));
	}

}

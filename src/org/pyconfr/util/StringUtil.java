package org.pyconfr.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.pyconfr.pojo.Person;
import org.pyconfr.schedules.Main;


/**
 * String utilities specific to the fosdem schedule application.
 * 
 * @author sandbender
 */
public class StringUtil {

	/** State constants used by {@link #niceify(String)} */
	private static final int STATE_NORMAL = 0;
	private static final int STATE_IN_WHITESPACE = 1;
	private static final int STATE_IN_FIRST_RETURN = 2;
	private static final int STATE_IN_SUBSEQUENT_RETURNS = 4;
	
	

	private static final int RETURN_STATES = STATE_IN_FIRST_RETURN | STATE_IN_SUBSEQUENT_RETURNS;

	/**
	 * Takes a string an makes it nice for displaying. For use on unclean
	 * strings that come from XML: single returns are removed, multiple returns
	 * are kept. Tabs are replaced by spaces and duplicate spaces are joined
	 * into one space.
	 * 
	 * @param value
	 *            An unclean string value
	 * @return A niceified version of the value
	 */
	public static String niceify(final String value) {
		if (value == null) return "";
		
		final StringBuffer sb = new StringBuffer(value.length());
		final int length = value.length();
		int state = STATE_NORMAL;
		int i = 0;
		char c;
		while (i < length) {
			c = value.charAt(i++);
			switch (c) {
			case ' ':
			case '\t':
				if ((state & STATE_IN_WHITESPACE) == 0)
					sb.append(' ');
				state = STATE_IN_WHITESPACE;
				break;
			case '\n':
				switch (state & RETURN_STATES) {
				case STATE_IN_SUBSEQUENT_RETURNS:
					sb.append('\n');
					break;
				case STATE_IN_FIRST_RETURN:
					sb.append("\n\n");
					state = STATE_IN_SUBSEQUENT_RETURNS;
					break;
				default:
					if ((state & STATE_IN_WHITESPACE) == 0)
						sb.append(' ');
					state = STATE_IN_FIRST_RETURN | STATE_IN_WHITESPACE;
					break;
				}
				break;
			default:
				sb.append(c);
				state = STATE_NORMAL;
				break;
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the persons in the list as a comma separated string of their the
	 * names.
	 * 
	 * @param persons
	 *            A list of persons
	 * @return A string with their names
	 */
	public static String personsToString(final List<Person> persons) {
		final StringBuffer sb = new StringBuffer();
		boolean first = true;
		for (final Person person : persons) {
			if (!first)
				sb.append(", ");
			sb.append(person.getName());
			first = false;
		}
		return sb.toString();
	}
	
	
	public static String datesToString(final Date start, final int duration) {
		return new SimpleDateFormat("HH:mm").format(start)
			+ " - " + duration + " min";
		
	}
	
	public static String dateTimeToString(final Date date) {
		if (date == null) return "Never";
		return DateFormat.getDateTimeInstance().format(date);
	}

	/**
	 * Clean up a string to remove dots and lowercase.
	 * This is usually needed for url paths for the map download
	 * @param name 
	 * @return
	 */
	public static String roomNameToURL(String name) {
		return Main.ROOM_IMG_URL_BASE+name.replace(".", "").toLowerCase();
	}

}

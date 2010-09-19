package org.pyconfr.pojo;
/**
 *  This file is part of the FOSDEM Android application.
 *  http://android.fosdem.org
 *  
 *  Thisis open source software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  It is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this software.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  @author Christophe Vandeplas <christophe@vandeplas.com>
 */


import java.util.ArrayList;
import java.util.Collection;

public class Track {

	private String name;
	private ArrayList<Event> events = new ArrayList<Event>();
	
	public Track(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<Event> getEvents() {
		return events;
	}
	public void setEvents(ArrayList<Event> events) {
		this.events = events;
	}
	public void addEvent(Event event) {
		this.events.add(event);
	}
	
	public void addEvents(Collection<Event> events){
		this.events.addAll(events);
	}
	

	public String getType() {
		// FOSDEM specific parts
		String[] mainTracks = {
				"Keynotes",
				"Security",
				"Scalability",
				"Monitoring",
				"Various",
				"Database",
				"Javascript"
				};
		String[] sameAsTrack =  {
				"Lightning Talks",
				"Certification",
				"Hackerspace",
				"Agora",
				"Classe Numérique",
				"Hackerspace",
				"Assemblée Générale",
				"Pause",
				"Petit-déj"
				};
		//private final String[] devrooms; = else 
		
		// Main Tracks
		for (String s: mainTracks) {
			if (0==name.compareTo(s)) return "Main Track";
	    }
		// 
		for (String s: sameAsTrack) {
			if (0==name.compareTo(s)) return name;
	    }
		// Rest are Devrooms
		return "Developer Room";
	}
}

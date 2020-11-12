package net.jmb.tuto.spring.databinding.formatters;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.format.Formatter;

public final class DateFormatter implements Formatter<Date> {

    private String preferredPattern;
    private String[] otherPatterns;

    public DateFormatter(String preferredPattern, String... otherPatterns) {
        this.preferredPattern = preferredPattern;
        this.otherPatterns = otherPatterns;
    }

    public String print(Date date, Locale locale) {
        if (date == null) {
            return "";
        }
        return getDateFormat(locale).format(date);
    }

    public Date parse(String formatted, Locale locale) throws ParseException {
    	Date result = null;
    	if (formatted.length() > 0) {
        	result = getAllDateFormats(locale).stream()
        		.map(dateFormat -> {
        			try {
        				return dateFormat.parse(formatted);
        			} catch (ParseException e) {
        				return null;
        			}        		
        		})
        		.filter(date -> date != null)
        		.findFirst().get();
        }        
        return result;
    }

    protected DateFormat getDateFormat(Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(preferredPattern, locale);
        dateFormat.setLenient(false);
        return dateFormat;
    }
    
    @SuppressWarnings("serial")
	protected List<DateFormat> getAllDateFormats(Locale locale) {
    	
    	List<String> allPatterns = new ArrayList<String>() {{ add(preferredPattern); }};
    	if (otherPatterns != null) {
    		allPatterns.addAll(Arrays.asList(otherPatterns));
    	}
    	return allPatterns.stream()
    			.map(pattern -> new SimpleDateFormat(pattern))
    			.collect(Collectors.toList());
    }
}

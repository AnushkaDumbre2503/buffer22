package utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeUtils {
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DEFAULT_FORMATTER);
    }
    
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DATE_FORMATTER);
    }
    
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(TIME_FORMATTER);
    }
    
    public static String formatDuration(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return "N/A";
        
        long hours = ChronoUnit.HOURS.between(start, end);
        long minutes = ChronoUnit.MINUTES.between(start, end) % 60;
        long seconds = ChronoUnit.SECONDS.between(start, end) % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }
    
    public static String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        
        LocalDateTime now = LocalDateTime.now();
        long seconds = ChronoUnit.SECONDS.between(dateTime, now);
        
        if (seconds < 60) {
            return seconds + " seconds ago";
        }
        
        long minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        }
        
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        }
        
        long days = hours / 24;
        if (days < 30) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        }
        
        long months = days / 30;
        if (months < 12) {
            return months + " month" + (months == 1 ? "" : "s") + " ago";
        }
        
        long years = months / 12;
        return years + " year" + (years == 1 ? "" : "s") + " ago";
    }
    
    public static boolean isWithinMinutes(LocalDateTime dateTime, int minutes) {
        if (dateTime == null) return false;
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(minutes);
        return dateTime.isAfter(cutoff);
    }
    
    public static boolean isWithinHours(LocalDateTime dateTime, int hours) {
        if (dateTime == null) return false;
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return dateTime.isAfter(cutoff);
    }
    
    public static boolean isWithinDays(LocalDateTime dateTime, int days) {
        if (dateTime == null) return false;
        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
        return dateTime.isAfter(cutoff);
    }
    
    public static LocalDateTime minutesAgo(int minutes) {
        return LocalDateTime.now().minusMinutes(minutes);
    }
    
    public static LocalDateTime hoursAgo(int hours) {
        return LocalDateTime.now().minusHours(hours);
    }
    
    public static LocalDateTime daysAgo(int days) {
        return LocalDateTime.now().minusDays(days);
    }
    
    public static LocalDateTime minutesFromNow(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }
    
    public static LocalDateTime hoursFromNow(int hours) {
        return LocalDateTime.now().plusHours(hours);
    }
    
    public static LocalDateTime daysFromNow(int days) {
        return LocalDateTime.now().plusDays(days);
    }
    
    public static long getMinutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.MINUTES.between(start, end);
    }
    
    public static long getHoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.HOURS.between(start, end);
    }
    
    public static long getDaysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }
    
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(LocalDateTime.now().toLocalDate());
    }
    
    public static boolean isYesterday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        return dateTime.toLocalDate().equals(LocalDateTime.now().minusDays(1).toLocalDate());
    }
    
    public static String formatCompactDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        
        if (isToday(dateTime)) {
            return "Today " + formatTime(dateTime);
        } else if (isYesterday(dateTime)) {
            return "Yesterday " + formatTime(dateTime);
        } else {
            return formatDateTime(dateTime);
        }
    }
}

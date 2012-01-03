package org.springframework.roo.addon.jsf;

/**
 * Enum representing PrimeFaces media players.
 * 
 * @author Alan Stewart
 * @since 1.2.0
 */
public enum MediaPlayer {
    WINDOWS("asx", "asf", "avi", "wma", "wmv"), QUICKTIME("aif", "aiff", "aac",
            "au", "bmp", "gsm", "mov", "mid", "midi", "mpg", "mpeg", "mp4",
            "m4a", "psd", "qt", "qtif", "qif", "qti", "snd", "tif", "tiff",
            "wav", "3g2", "3pg"), FLASH("flv", "mp3", "swf"), REAL("ra", "ram",
            "rm", "rpm", "rv", "smi", "smil");

    private String[] mediaTypes;

    private MediaPlayer(final String... mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public String[] getMediaTypes() {
        return mediaTypes;
    }
}

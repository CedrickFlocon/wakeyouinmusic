package org.neige.wakeyouinmusic.android.models;

public class DefaultRingtone extends Ringtone {

    private String uri;
    private boolean isErrorRingtone;

    public boolean isErrorRingtone() {
        return isErrorRingtone;
    }

    public void setIsErrorRingtone(boolean isErrorRingtone) {
        this.isErrorRingtone = isErrorRingtone;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DefaultRingtone)) {
            return false;
        }
        return getUri().equals(((DefaultRingtone) o).getUri());
    }
}

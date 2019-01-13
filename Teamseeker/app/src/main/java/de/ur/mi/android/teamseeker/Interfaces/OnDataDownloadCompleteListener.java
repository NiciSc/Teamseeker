package de.ur.mi.android.teamseeker.interfaces;

import java.util.List;

public interface OnDataDownloadCompleteListener<T> {
    void onDataDownloadComplete(List<T> data, int resultCode);
}

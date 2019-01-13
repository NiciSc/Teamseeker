package de.ur.mi.android.teamseeker.interfaces;

import java.util.List;

public interface OnDataUploadCompleteListener<T> {
    void onDataUploadComplete(T data, int resultCode);
}

package de.ur.mi.android.teamseeker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import de.ur.mi.android.teamseeker.Interfaces.OnDataUpdateReceivedListener;
import de.ur.mi.android.teamseeker.filter.Filter;
import de.ur.mi.android.teamseeker.interfaces.OnDataDownloadCompleteListener;
import de.ur.mi.android.teamseeker.interfaces.OnDataUploadCompleteListener;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/*
Source:
https://docs.oracle.com/javase/tutorial/extra/generics/methods.html
Firebas Documentation
 */
public final class DatabaseManager {

    private static final String FIREBASE_STORAGE_PROFILEPICTURE_PATH = "images/profilepictures/";
    public static final int DOCUMENT_EXISTS = 44;
    public static final int DOCUMENT_EXISTS_NOT = 45;

    public static final int SOURCE_LOCAL = 0;
    public static final int SOURCE_DB = 1;

    public static final String DB_KEY_EVENT = "collection:events";
    public static final String DB_KEY_USER = "collection:users";

    private static FirebaseFirestore database;
    private static StorageReference storageReference;
    private static FirebaseStorage storage;

    public static void initialize() {
        database = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    //region upload to firebase
    public static void uploadImageToStorage(String localImagePath, String associatedID, final de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener onCompleteListener) {
        StorageReference fileRef = storageReference.child(FIREBASE_STORAGE_PROFILEPICTURE_PATH + associatedID);
        try {
            InputStream stream = new FileInputStream(new File(localImagePath));
            UploadTask uploadTask = fileRef.putStream(stream);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    onCompleteListener.onComplete(RESULT_OK);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    onCompleteListener.onComplete(RESULT_CANCELED);
                }
            });
        } catch (FileNotFoundException e) {
            onCompleteListener.onComplete(RESULT_CANCELED);
        }
    }

    public static <T> void addData(final T data, String databaseKey, final OnDataUploadCompleteListener<T> onDataUploadCompleteListener) {
        final DocumentReference documentReference = database.collection(databaseKey).document();

        documentReference.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    onDataUploadCompleteListener.onDataUploadComplete(data, RESULT_OK);
                } else {
                    onDataUploadCompleteListener.onDataUploadComplete(data, RESULT_CANCELED);
                }
            }
        });
    }

    public static <T> void updateData(final String databaseKey, String idField, String id, final T updatedValue, final de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener onCompleteListener) {
        database.collection(databaseKey).whereEqualTo(idField, id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                    database.collection(databaseKey).document(task.getResult().getDocuments().get(0).getId()).set(updatedValue).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (onCompleteListener != null) {
                                onCompleteListener.onComplete(RESULT_OK);
                            }
                        }
                    });
                }
            }
        });
    }
    //endregion

    //region download from firebase
    public static void downloadImageFromStorage(String associatedID, final OnDataDownloadCompleteListener<Bitmap> onDataDownloadCompleteListener) {
        StorageReference fileRef = storageReference.child(FIREBASE_STORAGE_PROFILEPICTURE_PATH + associatedID);
        final long ONE_MEGABYTE = 1024 * 1024;
        fileRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ArrayList<Bitmap> bitmapList = new ArrayList<>();
                bitmapList.add(image);
                onDataDownloadCompleteListener.onDataDownloadComplete(bitmapList, RESULT_OK);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onDataDownloadCompleteListener.onDataDownloadComplete(null, RESULT_CANCELED);
            }
        });
    }

    public static <T> void getDocumentsById(final Class<T> dataClass, final String databaseKey, ArrayList<String> ids, @NonNull final OnDataDownloadCompleteListener<T> onDataDownloadCompleteListener) {
        CollectionReference collectionReference = database.collection(databaseKey);
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (String id : ids) {
            tasks.add(collectionReference.document(id).get());
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                ArrayList<T> dataList = new ArrayList<>();
                for (Object document : objects) {
                    dataList.add(((DocumentSnapshot) document).toObject(dataClass));
                }
                if (onDataDownloadCompleteListener != null) {
                    onDataDownloadCompleteListener.onDataDownloadComplete(dataList, RESULT_OK);
                }
            }
        });
    }

    public static <T> void getData(final Class<T> dataClass, String databaseKey, String idField, String id, final OnDataDownloadCompleteListener<T> onDataDownloadCompleteListener) {
        database.collection(databaseKey).whereEqualTo(idField, id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                    //There can only be one document with a given id so it's safe to return index 0
                    T data = task.getResult().getDocuments().get(0).toObject(dataClass);
                    List<T> dataList = new ArrayList<>();
                    dataList.add(data);
                    onDataDownloadCompleteListener.onDataDownloadComplete(dataList, RESULT_OK);
                } else {
                    onDataDownloadCompleteListener.onDataDownloadComplete(null, RESULT_CANCELED);
                }
            }
        });
    }

    public static void getUsers(String databaseKey, final ArrayList<String> userIDs,
                                final OnDataDownloadCompleteListener<UserData> onDataDownloadCompleteListener) {
        CollectionReference collectionReference = database.collection(databaseKey);
        ArrayList<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (final String id : userIDs) {
            tasks.add(collectionReference.whereEqualTo(UserData.USERID_KEY, id).get());
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                ArrayList<UserData> userData = new ArrayList<>();
                for (Object query : objects) {
                    for (DocumentSnapshot document : ((QuerySnapshot) query).getDocuments()) {
                        userData.add(document.toObject(UserData.class));
                    }
                }
                onDataDownloadCompleteListener.onDataDownloadComplete(userData, RESULT_OK);
            }
        });
    }

    /**
     * Used to get multiple Events with the applied filters
     *
     * @param databaseKey
     * @param onDataDownloadCompleteListener
     * @param filter                         use static strings from EventData as Keys and use fitting datatypes or the app will crash
     */
    public static void getEventData(final Context context, String databaseKey, final Filter filter, final OnDataDownloadCompleteListener<EventData> onDataDownloadCompleteListener) {

        //source for merging query results: https://stackoverflow.com/questions/50118345/firestore-merging-two-queries-locally
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        final List<EventData> results = new ArrayList<>();
        CollectionReference collectionReference = database.collection(databaseKey);
        if (filter.containsFilterType(Filter.FILTER_GETALL) || (filter.containsFilterType(Filter.FILTER_MAXRADIUS) && filter.getFilters().size() == 1)) {
            tasks.add(collectionReference.get());
        } else if (!filter.isEmpty()) {
            Iterator iterator = filter.getFilters().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry) iterator.next();

                String key = (String) entry.getKey();
                Object value = entry.getValue();

                switch (key) {
                    case Filter.FILTER_MINAGE:
                        tasks.add(collectionReference.whereGreaterThanOrEqualTo(EventData.MINAGE_KEY, value).get());
                        break;
                    case Filter.FILTER_TYPE:
                        tasks.add(collectionReference.whereEqualTo(EventData.EVENTTYPE_KEY, value).get());
                        break;
                    case Filter.FILTER_NAME:
                        tasks.add(collectionReference.whereEqualTo(EventData.EVENTNAME_KEY, value).get());
                        break;
                    case Filter.FILTER_ID:
                        tasks.add(collectionReference.whereEqualTo(EventData.EVENTID_KEY, value).get());
                        break;
                    case Filter.FILTER_DATE:
                        tasks.add(collectionReference.whereEqualTo(EventData.EVENTDATE_KEY, value).get());
                        break;
                    default:
                        break;
                }
            }
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                if (objects.size() == 0) {
                    onDataDownloadCompleteListener.onDataDownloadComplete(null, RESULT_OK);
                    return;
                    //No events found matching filter or filter is empty
                }
                for (Object query : objects) {
                    ArrayList<EventData> eventsFromQuery = new ArrayList<>();
                    for (DocumentSnapshot document : ((QuerySnapshot) query).getDocuments()) {
                        eventsFromQuery.add(document.toObject(EventData.class));
                    }
                    if (results.isEmpty()) {
                        results.addAll(eventsFromQuery);
                    } else {
                        results.retainAll(eventsFromQuery);
                    }
                }
                if (filter.containsFilterType(Filter.FILTER_ISFULL)) {
                    final ArrayList<EventData> fullEvents = new ArrayList<>();
                    for (EventData eventData : results) {
                        if (eventData.getMaxParticipants() <= eventData.getParticipants().size()) {
                            fullEvents.add(eventData);
                        }
                    }
                    results.removeAll(fullEvents);
                }
                if (filter.containsFilterType(Filter.FILTER_MAXRADIUS)) {
                    final ArrayList<EventData> eventsInRange = new ArrayList<>();
                    MyLocationManager.getDeviceLocation(context, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            for (EventData event : results) {
                                LatLng eventCoord = new LatLng(event.getEventLatitude(), event.getEventLongitude());
                                LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                                double distance = MyLocationManager.distanceTo(eventCoord, position);
                                if (distance <= (double) (filter.getValue(Filter.FILTER_MAXRADIUS))) {
                                    eventsInRange.add(event);
                                }
                            }
                            /*ArrayList<EventData> retained = (ArrayList<EventData>)Utility.actuallyRetainAll(results, eventsInRange);
                            results.clear();
                            results.addAll(retained);*/
                            results.retainAll(eventsInRange);

                            onDataDownloadCompleteListener.onDataDownloadComplete(results, RESULT_OK);
                        }
                    });
                } else {
                    onDataDownloadCompleteListener.onDataDownloadComplete(results, RESULT_OK);
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onDataDownloadCompleteListener.onDataDownloadComplete(null, RESULT_CANCELED);
            }
        });
    }
    //endregion

    //region subscription methods
    public static void subscribeToEventUpdates(final String databaseKey, String idField, String id, final OnDataUpdateReceivedListener onDataUpdateReceivedListener) {
        unsubscribeEventUpdates(databaseKey, idField, id);
        database.collection(databaseKey).whereEqualTo(idField, id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                    database.collection(databaseKey).document(task.getResult().getDocuments().get(0).getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                            int source = documentSnapshot != null && documentSnapshot.getMetadata().hasPendingWrites() ? SOURCE_LOCAL : SOURCE_DB;
                            if (documentSnapshot != null && documentSnapshot.exists()) {
                                onDataUpdateReceivedListener.onDataUpdateReceived(source, documentSnapshot.toObject(EventData.class), RESULT_OK);
                            } else {
                                onDataUpdateReceivedListener.onDataUpdateReceived(source, null, RESULT_CANCELED);
                            }
                        }
                    });
                }
            }
        });
    }

    public static void unsubscribeEventUpdates(final String databaseKey, String idField, String id) {
        database.collection(databaseKey).whereEqualTo(idField, id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                    ListenerRegistration registration = database.collection(databaseKey).document(task.getResult().getDocuments().get(0).getId()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        }
                    });
                    registration.remove();
                }
            }
        });
    }
    //endregion

    //region utility methods
    public static void documentExists(String databaseKey, String idField, String associatedID, final de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener onCompleteListener) {
        database.collection(databaseKey).whereEqualTo(idField, associatedID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0 && task.getResult().getDocuments().get(0).exists()) {
                    onCompleteListener.onComplete(DOCUMENT_EXISTS);
                } else {
                    onCompleteListener.onComplete(DOCUMENT_EXISTS_NOT);
                }
            }
        });
    }

    public static void getDocumentIds(final String databaseKey, String idField, ArrayList<String> ids, @NonNull final OnDataDownloadCompleteListener<String> onDataDownloadCompleteListener) {
        CollectionReference collectionReference = database.collection(databaseKey);
        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (String id : ids) {
            tasks.add(collectionReference.whereEqualTo(idField, id).get());
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                ArrayList<String> documentIds = new ArrayList<>();
                for (Object query : objects) {
                    for (DocumentSnapshot document : ((QuerySnapshot) query).getDocuments()) {
                        documentIds.add(document.getId());
                    }
                }
                onDataDownloadCompleteListener.onDataDownloadComplete(documentIds, RESULT_OK);
            }
        });
    }

    public static void deleteDocument(final String databaseKey, String idField, String id, final de.ur.mi.android.teamseeker.Interfaces.OnCompleteListener onCompleteListener) {
        database.collection(databaseKey).whereEqualTo(idField, id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful() && task.getResult().getDocuments().size() > 0) {
                    //There can only be one document with a given id so it's safe to return index 0
                    database.collection(databaseKey).document(task.getResult().getDocuments().get(0).getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if (onCompleteListener != null) {
                                    onCompleteListener.onComplete(RESULT_OK);
                                } else {
                                    onCompleteListener.onComplete(RESULT_CANCELED);
                                }
                            }
                        }
                    });
                }
            }
        });
    }
    //endregion
}

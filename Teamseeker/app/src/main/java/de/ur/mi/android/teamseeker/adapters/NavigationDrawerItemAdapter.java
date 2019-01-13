package de.ur.mi.android.teamseeker.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.ur.mi.android.teamseeker.EventData;
import de.ur.mi.android.teamseeker.R;
import de.ur.mi.android.teamseeker.helpers.OverlayActivity;

public class NavigationDrawerItemAdapter extends BaseExpandableListAdapter {

    private Context context;
    private HashMap<String, List> menu;
    private String[] headers;

    public NavigationDrawerItemAdapter(Context context, String[] headers, HashMap<String, List> menu) {
        this.context = context;
        this.menu = menu;
        this.headers = headers;
    }

    public <T> void updateMenu(@OverlayActivity.DrawerItemDef String header, List<T> children) {
        menu.put(header, children);
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return headers.length;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getChildrenCount(int groupPosition) {
        String key = (String)getGroup(groupPosition);
        List children = menu.get(key);
        return children == null ? 0 : children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return headers[groupPosition];
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String key = (String) this.getGroup(groupPosition);
        List<Object> children = menu.get(key);
        if (children != null) {
            return children.get(childPosition);
        } else {
            return null;
        }
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerName = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.adapter_item_drawer_menuitem, null);
        }


        TextView textView_headerName = convertView.findViewById(R.id.textView_menuItemName);
        ImageView imageView_headerIcon = convertView.findViewById(R.id.imageView_itemIcon);

        textView_headerName.setText(headerName);
        textView_headerName.setTypeface(null, Typeface.BOLD);
        imageView_headerIcon.setImageDrawable(context.getResources().getDrawable(OverlayActivity.getIconForHeader(headerName)));

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (groupPosition) {
            case 0: //Map
                return null;
            case 1: //Create Event
                return null;
            case 2: //My Events
                if(convertView == null){
                    convertView = inflater.inflate(R.layout.adapter_item_drawer_myevents, null);
                }

                TextView textView_eventName = convertView.findViewById(R.id.textView_eventName);
                TextView textView_playerCount = convertView.findViewById(R.id.textView_playerCount);
                ImageView imageView_hostIcon = convertView.findViewById(R.id.imageView_hostIcon);

                EventData eventData = (EventData)getChild(groupPosition, childPosition);
                if(eventData == null){
                    return null;
                }
                textView_eventName.setText(eventData.getEventName());
                textView_playerCount.setText(String.valueOf(eventData.getParticipants().size()));
                if(eventData.getEventID().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                    imageView_hostIcon.setVisibility(View.VISIBLE);
                }

                return convertView;

            default:
                return null;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}

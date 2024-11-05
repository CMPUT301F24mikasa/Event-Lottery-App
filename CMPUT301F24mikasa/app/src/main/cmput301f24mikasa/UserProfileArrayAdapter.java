package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class UserProfileArrayAdapter extends ArrayAdapter<UserProfile> {
    private final Context context;
    private final List<UserProfile> userList;

    public UserProfileArrayAdapter(Context context, List<UserProfile> userList) {
        super(context, R.layout.activity_user_profile_list_item, userList);
        this.context = context;
        this.userList = userList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_user_profile_list_item, parent, false);

        TextView nameTextView = (TextView) rowView.findViewById(R.id.user_name);
        UserProfile user = userList.get(position);
        nameTextView.setText(user.getName());

        return rowView;
    }
}


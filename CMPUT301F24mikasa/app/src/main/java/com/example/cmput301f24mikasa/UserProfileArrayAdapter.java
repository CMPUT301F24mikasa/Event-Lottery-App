package com.example.cmput301f24mikasa;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Custom ArrayAdapter for displaying a list of UserProfile objects in a ListView.
 * This adapter inflates custom list item views and binds UserProfile data (such as name) to the views.
 */
public class UserProfileArrayAdapter extends ArrayAdapter<UserProfile> {
    private final Context context;
    private final List<UserProfile> userList;

    /**
     * Constructor for UserProfileArrayAdapter.
     *
     * @param context The context where the adapter is used (e.g., Activity).
     * @param userList The list of UserProfile objects to display.
     */
    public UserProfileArrayAdapter(Context context, List<UserProfile> userList) {
        super(context, R.layout.activity_user_profile_list_item, userList);
        this.context = context;
        this.userList = userList;
    }


    /**
     * Returns a view for a list item in the ListView.
     * This method is responsible for inflating the custom layout and binding data from
     * the UserProfile object to the views.
     *
     * @param position The position of the current item in the list.
     * @param convertView A recycled view (if available) to be reused.
     * @param parent The parent view group that the list item will be attached to.
     * @return The view to be displayed in the ListView for the current position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.activity_user_profile_list_item, parent, false);

        TextView nameTextView = (TextView) rowView.findViewById(R.id.user_name);

        UserProfile user = userList.get(position);
        //this sets what is displayed in the list
        nameTextView.setText(user.getName());

        return rowView;
    }
}



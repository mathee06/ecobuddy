package com.capstone.ecobuddy;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private static String LOG_TAG = MainActivity.class.getSimpleName();
    public static ArrayList<String> resultList;
    private ArrayList<String> filteredList = new ArrayList<>();

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    /**
     * For Directions, use connect() after instantiating the object to
     * actually get the data from a http request.
     */
    public static Directions testDirections;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setTitle(mTitle);

        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        LayoutInflater searchInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // inflate the view for the actionbar_search
        View view = searchInflater.inflate(R.layout.actionbar_search, null);
        // the view that contains the search "magnifier" icon
        final ImageView searchIcon = (ImageView) view.findViewById(R.id.search_icon);
        searchIcon.setImageResource(android.R.drawable.ic_menu_search);

        // the view that contains the clearlable autocomplete text view
        final ClearableAutoCompleteTextView searchBox = (ClearableAutoCompleteTextView) view.findViewById(R.id.search_box);

        searchBox.setVisibility(View.INVISIBLE);
        searchBox.setAdapter(new QueryAutoCompleteAdapter(this, android.R.layout.simple_dropdown_item_1line));
        searchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSearch(false);
            }
        });
        searchBox.setOnClearListener(new ClearableAutoCompleteTextView.OnClearListener() {
            @Override
            public void onClear() {
                toggleSearch(true);
            }
        });

        searchBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // handle clicks on search results here
                toggleSearch(true);

                // clear any markings from existing map
                MapsFragment.mMap.clear();

                final String endDestination = filteredList.get(position);
                Log.v(LOG_TAG, "USER SELECTED: " + endDestination);

                if (MapsFragment.getCurrentCoords() != null) {
                    new DrawRouteTask().execute(endDestination);

                } else {
                    // User location has not loaded yet
                    Toast.makeText(getApplicationContext(), "Please wait...", Toast.LENGTH_SHORT).show();
                }

                //songOBJ = Utility.toJSON(selection);
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment fragmentByID = fragmentManager.findFragmentById(R.id.container);
                String fragmentID = fragmentByID.toString();
                Log.v(LOG_TAG, "THE CURRENT FRAGMENT IS: " + fragmentByID.toString());
            }
        });

        Log.v(LOG_TAG, "VALUE OF LOG TAG IS: " + LOG_TAG);
        actionBar.setCustomView(view);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        Log.v(LOG_TAG, "SELECTED FRAGMENT AT POSITION: " + position);
        Fragment fragment;
        String fragmentTag;
        switch (position + 1) {
            case 1: //MapsFragment
                Log.v(LOG_TAG, "USER SELECTED MAPS FRAGMENT FROM NAV DRAWER");
                fragment = MapsFragment.newInstance();
                fragmentTag = MapsFragment.LOG_TAG;
                Utility.currentFragment = "MapsFragment";
                Log.v(LOG_TAG, "SET CURRENT FRAGMENT TO: " + Utility.currentFragment);
                replaceFragment(fragment, fragmentTag);
                break;
        }
    }

    private void replaceFragment(Fragment fragment, String fragmentTag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment, fragmentTag)
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            toggleSearch(true);
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // this toggles between the visibility of the search icon and the search box
    // to show search icon - reset = true
    // to show search box - reset = false
    protected void toggleSearch(boolean reset) {
        ClearableAutoCompleteTextView searchBox = (ClearableAutoCompleteTextView) findViewById(R.id.search_box);
        ImageView searchIcon = (ImageView) findViewById(R.id.search_icon);
        if (reset) {
            // hide search box and show search icon
            searchBox.setText("");
            searchBox.setVisibility(View.GONE);
            searchIcon.setVisibility(View.VISIBLE);
            // hide the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchBox.getWindowToken(), 0);
        } else {
            // hide search icon and show search box
            searchIcon.setVisibility(View.GONE);
            searchBox.setVisibility(View.VISIBLE);
            searchBox.requestFocus();
            // show the keyboard
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchBox, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public class QueryAutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {

        public QueryAutoCompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            if (!filteredList.isEmpty()) {
                return filteredList.size();
            } else {
                Log.v(LOG_TAG, "RESULT LIST IS EMPTY!");
            }
            return 0;
        }

        @Override
        public String getItem(int index) {
            return filteredList.get(index).toString();
        }

        // Called each time the adapter needs to populate another row in the view
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(getContext());

            // convertView is essentially each respective row
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.search_item, parent, false);
            }

            final View rowView = convertView;
            try {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView destinationLocation = (TextView) rowView.findViewById(R.id.search_item_destination);

                        if (!filteredList.isEmpty()) {
                            try {
                                String location = filteredList.get(position);
                                destinationLocation.setText(location);
                                rowView.setTag(location);

                            } catch (IndexOutOfBoundsException IE) {
                                Log.v(LOG_TAG, "INDEX OUT OF BOUNDS EXCEPTION: " + IE);
                            } catch (Exception e) {
                                Log.v(LOG_TAG, "EXCEPTION: " + e);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                Log.v(LOG_TAG, "EXCEPTION CAUGHT AT GET VIEW: " + e);
            }
            return rowView;
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                public FilterResults performFiltering(final CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    // Constraint is the input given by the user
                    if (constraint != null) {
                        // Retrieve the autocomplete results.

                        new AutoCompleteTask().execute(constraint.toString());
                        filteredList = resultList;
                        if(resultList != null) {
                            Log.v(LOG_TAG, "Filtered resultList: " + filteredList.toString());

                            // Assign the data to the FilterResults
                            filterResults.values = filteredList;
                            filterResults.count = filteredList.size();
                        }
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(final CharSequence constraint, final FilterResults results) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (results != null && results.count > 0) {
                                //Log.v(LOG_TAG, "CONSTRAINT IS: " + constraint.toString())
                                notifyDataSetChanged();
                            } else {
                                notifyDataSetInvalidated();
                            }
                        }
                    });

                }};
            return filter;
        }
    }

    /**
     * Draw Route Task
     * - Draws detailed route on the map (snaps to the road).
     */
    public class DrawRouteTask extends AsyncTask<String, Void, ArrayList<LatLng>> {

        private String LOG_TAG = DrawRouteTask.class.getSimpleName();


        @Override
        protected ArrayList<LatLng> doInBackground(String... input) {
            Log.v(LOG_TAG, "DRAWING DIRECTIONS TO DESTINATION: " + input[0]);
            testDirections = new Directions(MapsFragment.getCurrentCoords(), input[0]);
            testDirections.connect();

            return testDirections.getAllPolylinePointsFromSteps();
        }

        @Override
        protected void onPostExecute(ArrayList<LatLng> points) {
            PolylineOptions routeOptions = new PolylineOptions();
            routeOptions.addAll(points);
            routeOptions.color(Color.BLUE);
            MapsFragment.mMap.addPolyline(routeOptions);
        }
    }
}

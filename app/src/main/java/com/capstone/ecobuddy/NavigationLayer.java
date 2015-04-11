package com.capstone.ecobuddy;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * Created by Amir on 2015-04-10.
 */
public class NavigationLayer {

    // Field variables
    private static Activity main;
    private static RelativeLayout navLayer;

    /**
     * Instantiate and pass it the system context
     * @param main
     */
    public NavigationLayer(Activity main) {

        // Get the main activity
        this.main = main;

        // Inflate the nav layer on the screen
        inflateNavigationLayer();

        // Get the navigation layer on the screen
        this.navLayer = (RelativeLayout) main.findViewById(R.id.navigation_layer);

        // Hide it first
        hideNavLayer();
    }

    /**
     * Inflate navigation layer xml on the screen
     */
    private void inflateNavigationLayer() {
        LayoutInflater layoutInflater;
        FrameLayout container;

        layoutInflater = (LayoutInflater) main.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        container = (FrameLayout) main.findViewById(R.id.container);
        layoutInflater.inflate(R.layout.navigation_layer, container);
    }

    /**
     * Bring the navigation layer to the front so it is visible to the user
     */
    public void showNavLayer() {
        navLayer.bringToFront();
        navLayer.setVisibility(View.VISIBLE);
    }

    /**
     * Hide the navigation layer from the user
     */
    public void hideNavLayer() {
        navLayer.setVisibility(View.INVISIBLE);
    }
}

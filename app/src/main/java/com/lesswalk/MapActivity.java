package com.lesswalk;

import android.animation.Animator;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapActivity.class.getSimpleName();
    private GoogleMap      mMap;
    private RelativeLayout map_menu;
    private MapMenu        mapMenu;

    private class MapMenu {
        private final ViewGroup container;
        EditText from;
        EditText to;
        Button   go;
        Button   x;
        //Button   v;

        MapMenu(ViewGroup c) {
            container = c;
            from = container.findViewById(R.id.map_from);
            to = container.findViewById(R.id.map_to);
            go = container.findViewById(R.id.map_go);
            x = container.findViewById(R.id.map_x);
            //v = MapActivity.this.findViewById(R.id.map_v);
        }

        private MapMenu setOnClick(View view, View.OnClickListener onClickListener) {
            view.setOnClickListener(onClickListener);
            return this;
        }

        MapMenu go(View.OnClickListener onClickListener) {
            return setOnClick(go, onClickListener);
        }

        MapMenu x(View.OnClickListener onClickListener) {
            return setOnClick(x, onClickListener);
        }

//        MapMenu v(View.OnClickListener onClickListener) {
//            return setOnClick(v, onClickListener);
//        }

        boolean isDown = true;

        public void toggle() {
            container.animate()
                    .translationY(isDown ? -container.getHeight()*8/10 : container.getTop())
                    .setDuration(isDown ? 400 : 250)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {
                        }
                        @Override
                        public void onAnimationEnd(Animator animator) {
                            //ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) container.getLayoutParams();
                            //marginLayoutParams.topMargin += yTranslateBy;
                            //container.setLayoutParams(marginLayoutParams);
                            isDown = !isDown;
                        }
                        @Override
                        public void onAnimationCancel(Animator animator) {
                        }
                        @Override
                        public void onAnimationRepeat(Animator animator) {
                        }
                    })
                    .start();


        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        map_menu = findViewById(R.id.map_menu);
        map_menu.setVisibility(View.GONE);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

//        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        map_menu.setVisibility(View.VISIBLE);
        mapMenu = new MapMenu(map_menu)
                .go(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String text = String.format("%s --> %s", mapMenu.from.getText().toString(), mapMenu.to.getText().toString());
                        Log.d(TAG, text);
                        Toast.makeText(MapActivity.this, "GO!", Toast.LENGTH_SHORT).show();
                        mapMenu.toggle();
                    }
                }).x(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mapMenu.from.setText("");
                        mapMenu.to.setText("");
                    }
                });
//                mapMenu.v(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        mapMenu.toggle();
//                    }
//                });
    }
}

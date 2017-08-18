package com.lesswalk;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lesswalk.bases.BaseCarusselActivity;
import com.lesswalk.database.AWS;
import com.lesswalk.editor_pages.CarusselEditorMainItem;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectAddressCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectPhotoTipCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectTextTipCallback;
import com.lesswalk.editor_pages.bases.EditObjects2dManager;
import com.lesswalk.editor_pages.bases.ImageView;
import com.lesswalk.maps.MapData;
import com.lesswalk.maps.MapUtils;
import com.lesswalk.pagescarussel.ICarusselMainItem;
import com.lesswalk.views.MyCameraView;

import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class EditorActivity extends BaseCarusselActivity implements EditObjects2dManager, OnMapReadyCallback {
	private static final String            TAG                  = EditorActivity.class.getSimpleName();
	private static final int               MAX_ADDRESS_RESULTS  = 10;
	private static final float             MIN_DIST_SAME_MARKER = 5.5f;
	private static final int BOUNDS_PADDING                     = 5;
    private static final float MIN_BOUNDS_R2                    = 80f;
    private              ICarusselMainItem carusselMainItem     = null;
	private              RelativeLayout    addFamilyView        = null;
	private              LinearLayout      editorTextTipView    = null;
	private              LinearLayout      editorTakePhotoMenu  = null;
	private              LinearLayout      manualAddress        = null;
	private              Button            backButton           = null;
	private              Button            saveButton           = null;
	private              Vector<View>      additionViews        = null;
	private              Vector<View>      currentDisplayed     = null;
	private              GoogleMap         mMap                 = null;
	private              RelativeLayout    map_menu             = null;
	private SupportMapFragment mapFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addAdditionLayouts();

		// Obtain the SupportMapFragment and get notified when the map is ready to be used.
		mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);

		currentDisplayed = new Vector<View>();
	}

	private void addAdditionLayouts() {
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//
		if (additionViews == null) additionViews = new Vector<View>();
		//
		additionViews.add(addFamilyView = (RelativeLayout) layoutInflater.inflate(R.layout.layout_add_family, null));
		additionViews.add(editorTextTipView = (LinearLayout) layoutInflater.inflate(R.layout.editor_text_tip, null));
		additionViews.add(manualAddress = (LinearLayout) layoutInflater.inflate(R.layout.layout_set_address, null));
		additionViews.add(editorTakePhotoMenu = (LinearLayout) layoutInflater.inflate(R.layout.editor_take_picture_menu, null));

		for (View v : additionViews) {
			getScreen().addView(v, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			v.setVisibility(View.GONE);
		}

		backButton = (Button) findViewById(R.id.bc_back_button);
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				onBackAction();
			}
		});

		saveButton = (Button) findViewById(R.id.bc_right_button);
		saveButton.setText("Save");
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				File dir = new File(getCacheDir(), "signature");
				String filename = "content.json";
				String uuid = carusselMainItem.save(dir, filename);

				if (uuid != null) {
					AWS.OnRequestListener onRequestListener = new AWS.OnRequestListener() {
						@Override
						public void onStarted() {
							Log.d("elazarkin19", "onStarted");
						}

						@Override
						public void onFinished() {
							Log.d("elazarkin19", "onFinished");
							Toast.makeText(EditorActivity.this, "save success!", Toast.LENGTH_SHORT).show();
							finish();
						}

						@Override
						public void onError(int errorId) {
							Toast.makeText(EditorActivity.this, "save unsuccess - please check error!", Toast.LENGTH_SHORT).show();
							Log.d("elazarkin19", "onError: " + errorId);
						}
					};

					getService().saveSignature(uuid, dir, onRequestListener);
				}
			}
		});
	}

	@Override
	public void onLoadCarusselItems() {
		carusselMainItem = new CarusselEditorMainItem(this, this);

		getCarusselSurface().addCarusselMainItem(carusselMainItem);
	}

	@Override
	public int getContentView() {
		return R.layout.activity_editor;
	}

	@Override
	public void getManualAddressText(final EditObjectAddressCallback callback, String country, String city, String street, String street_num) {
		final Button done = (Button) (manualAddress.findViewById(R.id.editor_manual_text_done_bt));
		final EditText countryET = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_country_et));
		final EditText cityET = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_city_et));
		final EditText streetET = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_street_et));
		final EditText street_numET = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_street_num_et));

		countryET.setText(country);
		cityET.setText(city);
		streetET.setText(street);
		street_numET.setText(street_num);

		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				manualAddress.setVisibility(View.GONE);
//				getCarusselSurface().bringToFront();
//				getCarusselSurface().setVisiable(true);

				removeAdditionDislays();

				if (callback != null) {
					callback.onReturn(countryET.getText().toString(), cityET.getText().toString(), streetET.getText().toString(), street_numET.getText().toString());
				}
			}
		});

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getCarusselSurface().setVisiable(false);
				manualAddress.bringToFront();
				manualAddress.setVisibility(View.VISIBLE);

				currentDisplayed.add(manualAddress);
			}
		});
	}

	@Override
	public void getTipText(final EditObjectTextTipCallback callback, final String tip) {
		final Button done = (Button) (editorTextTipView.findViewById(R.id.editor_text_tip_done_bt));
		final TextView watch_tv = (TextView) (editorTextTipView.findViewById(R.id.editor_text_tip_watch_tv));
		final TextView recog_tv = (TextView) (editorTextTipView.findViewById(R.id.editor_text_tip_recog_tv));
		final TextView shortcut_tv = (TextView) (editorTextTipView.findViewById(R.id.editor_text_tip_shortcut_tv));
		final TextView forget_tv = (TextView) (editorTextTipView.findViewById(R.id.editor_text_tip_forget_tv));
		final EditText editor = (EditText) (editorTextTipView.findViewById(R.id.editor_text_tip_editor));

		TextView allInOne[] = {watch_tv, recog_tv, shortcut_tv, forget_tv};

		for (int i = 0; i < allInOne.length; i++) {
			allInOne[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					String text = ((TextView) v).getText().toString();
					editor.setText(text.subSequence(0, text.length() - 3));
				}
			});
		}

		done.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				editorTextTipView.setVisibility(View.GONE);
//				getCarusselSurface().bringToFront();
//				getCarusselSurface().setVisiable(true);
				removeAdditionDislays();
				if (callback != null) {
					callback.onReturn(editor.getText().toString());
				}
			}
		});

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getCarusselSurface().setVisiable(false);
				editor.setText(tip);
				editorTextTipView.bringToFront();
				editorTextTipView.setVisibility(View.VISIBLE);
				currentDisplayed.add(editorTextTipView);
			}
		});
	}

	private void openCameraForTakePicture(final EditObjectPhotoTipCallback callback) {
		final RelativeLayout cameraArea = (RelativeLayout) (editorTakePhotoMenu.findViewById(R.id.editor_take_photo_camera_area));
		final RelativeLayout menuArea = (RelativeLayout) (editorTakePhotoMenu.findViewById(R.id.editor_take_photo_menu_area));

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final MyCameraView cameraView = new MyCameraView(EditorActivity.this);
				final ImageView imageView = new ImageView(EditorActivity.this);
				//
				getCarusselSurface().setVisiable(false);
				editorTakePhotoMenu.bringToFront();
				menuArea.setVisibility(View.INVISIBLE);

				cameraView.setFrameCallback(imageView.getFrameCallback());
				cameraArea.addView(cameraView, new RelativeLayout.LayoutParams(1, 1));
				cameraArea.addView(imageView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				imageView.bringToFront();

				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						removeAdditionDislays();
						if (callback != null) {
							callback.onReturn(imageView.getBitmap());
						}

//						cameraArea.removeView(cameraView);
//						editorTakePhotoMenu.setVisibility(View.GONE);
//
//						cameraArea.removeAllViews();
//						getCarusselSurface().bringToFront();
//						getCarusselSurface().setVisiable(true);
					}
				});
			}
		});
	}

	@Override
	public void getTipPhoto(final EditObjectPhotoTipCallback callback) {
		final Button take_photo = (Button) (editorTakePhotoMenu.findViewById(R.id.editor_take_photo_open_camera));
		final Button cancel = (Button) (editorTakePhotoMenu.findViewById(R.id.editor_take_photo_cancel));
		final RelativeLayout cameraArea = (RelativeLayout) (editorTakePhotoMenu.findViewById(R.id.editor_take_photo_camera_area));
		final RelativeLayout menuArea = (RelativeLayout) (editorTakePhotoMenu.findViewById(R.id.editor_take_photo_menu_area));

		take_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				openCameraForTakePicture(callback);
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeAdditionDislays();
				if (callback != null) {
					callback.onReturn(null);
				}
			}
		});

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				getCarusselSurface().setVisiable(false);
				editorTakePhotoMenu.bringToFront();
				menuArea.setVisibility(View.VISIBLE);
				editorTakePhotoMenu.setVisibility(View.VISIBLE);

				currentDisplayed.add(editorTakePhotoMenu);
				currentDisplayed.add(menuArea);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void mainServiceConnected() {
		carusselMainItem.setService(getService());
	}

	private void onBackAction() {
		if (currentDisplayed != null && currentDisplayed.size() > 0) {
			removeAdditionDislays();
			//
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onBackPressed() {
		onBackAction();
	}

	private void removeAdditionDislays() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				final RelativeLayout cameraArea = (RelativeLayout) (editorTakePhotoMenu.findViewById(R.id.editor_take_photo_camera_area));

				for (View v : currentDisplayed) {
					v.setVisibility(View.INVISIBLE);
				}

				if (cameraArea.getChildCount() > 0) cameraArea.removeAllViews();

				currentDisplayed.removeAllElements();
				getCarusselSurface().setVisiable(true);
			}
		});
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

//		View mapFragmentView = mapFragment.getView();
//		if (mapFragmentView != null) {
//			mapFragmentView.setVisibility(View.GONE);
//		}
		FragmentManager fm = getSupportFragmentManager();
		fm.beginTransaction().hide(mapFragment).commit();
	}

	@Override
	public void openMapForResult(final EditManagerCallbacks.MapListener listener) {
		//TODO elad
        final ArrayList<LatLng> latLngList = new ArrayList<>();
		MapData inMapData = listener.getMapData();
		final MapData mapData = (inMapData != null) ? inMapData : new MapData();
		final FragmentManager fm = getSupportFragmentManager();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				fm.beginTransaction().show(mapFragment).commit();
			}
		});
        latLngList.clear();
		mMap.clear();
		//
		LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
		//
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String provider = service.getBestProvider(criteria, false);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				== PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
				== PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			Location location = service.getLastKnownLocation(provider);
			if (location!=null) {
				LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
				latLngList.add(currentLatLng);
				CircleOptions circleOptions = new CircleOptions();
				circleOptions.center(currentLatLng);
				circleOptions.fillColor(Color.parseColor("#66446688"));
				circleOptions.radius(40);
				mMap.addCircle(circleOptions);
				boundsBuilder.include(currentLatLng);
			}
		}
		//getCurrentLocation and zoom to range enclosing my location and latLngList
		if (mapData.inAddress!=null){
			List<Address> addressList = MapUtils.searchAddress(getApplicationContext(), mapData.inAddress, MAX_ADDRESS_RESULTS);
			if (addressList != null) {
				for (Address address : addressList) {
					LatLng markerLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    latLngList.add(markerLatLng);
					mMap.addMarker(new MarkerOptions().position(markerLatLng).title(address.getAddressLine(0)));
					boundsBuilder.include(markerLatLng);
					//mMap.moveCamera(CameraUpdateFactory.newLatLng(markerLatLng));
				}
			}

        }
		LatLngBounds bounds = null;
		try {
			bounds = boundsBuilder.build();
		}catch (Exception e){
			e.printStackTrace();
		}
		if (bounds!=null) {
			Location ne = new Location("ne");
			ne.setLatitude(bounds.northeast.latitude);
			ne.setLongitude(bounds.northeast.longitude);
			Location sw = new Location("sw");
			ne.setLatitude(bounds.southwest.latitude);
			sw.setLongitude(bounds.southwest.longitude);
			float boundsR2 = sw.distanceTo(ne);
			CameraUpdate cu;
			if (boundsR2 < MIN_BOUNDS_R2 && latLngList.size() > 1) {
				cu = CameraUpdateFactory.newLatLngZoom(latLngList.get(0), 10f);
			} else {
				cu = CameraUpdateFactory.newLatLngBounds(bounds, BOUNDS_PADDING);
			}
			//  move the map:
			//mMap.moveCamera(cu);
			//  animate the map:
			mMap.animateCamera(cu);
		}
        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                mapData.latLng = marker.getPosition();
                fm.beginTransaction().hide(mapFragment).commit();
                listener.onResult(mapData);
            }
        });
		mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
			@Override
			public void onMapLongClick(LatLng latLng) {
				Address address = MapUtils.getAddress(getApplicationContext(), latLng);
				latLngList.clear();
                mMap.clear();
                LatLng markerLatLng = null;
                float markerDistFromItsAddress = MIN_DIST_SAME_MARKER;
				if (address != null){
                    markerLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                    latLngList.add(markerLatLng);
                    mMap.addMarker(new MarkerOptions().position(markerLatLng).title(address.getAddressLine(0))
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
					float[] results = new float[1];
					Location.distanceBetween(
							address.getLatitude(), address.getLongitude(),
							latLng.latitude, latLng.longitude,
							results);
                    markerDistFromItsAddress = results[0];
					Log.d(TAG, String.format("dist: %3.3f", markerDistFromItsAddress));
				}
				//
				if (null == markerLatLng || markerDistFromItsAddress > MIN_DIST_SAME_MARKER){
                    latLngList.add(latLng);
					mMap.addMarker(new MarkerOptions().position(latLng).title(mapData.inAddress)
							.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
				}
			}
		});
    }
}

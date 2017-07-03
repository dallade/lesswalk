package com.lesswalk;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.lesswalk.bases.BaseCarusselActivity;
import com.lesswalk.editor_pages.CarusselEditorMainItem;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectAddressCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectPhotoTipCallback;
import com.lesswalk.editor_pages.bases.EditManagerCallbacks.EditObjectTextTipCallback;
import com.lesswalk.editor_pages.bases.EditObjects2dManager;
import com.lesswalk.editor_pages.bases.ImageView;
import com.lesswalk.pagescarussel.ICarusselMainItem;
import com.lesswalk.views.MyCameraView;

import java.util.Vector;

public class EditorActivity extends BaseCarusselActivity implements EditObjects2dManager
{
	private ICarusselMainItem carusselMainItem    = null;
	private RelativeLayout    addFamilyView       = null;
	private LinearLayout      editorTextTipView   = null;
	private LinearLayout      editorTakePhotoMenu = null;
	private LinearLayout      manualAddress       = null;
	private Button            backButton          = null;
	private Button            saveButton          = null;
	private Vector<View>      additionViews       = null;
	private Vector<View>      currentDisplayed    = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		addAdditionLayouts();

		currentDisplayed = new Vector<View>();
	}
	
	private void addAdditionLayouts() 
	{
		LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//
		if(additionViews == null) additionViews = new Vector<View>();
		//
		additionViews.add(addFamilyView=(RelativeLayout) layoutInflater.inflate(R.layout.layout_add_family, null));
		additionViews.add(editorTextTipView=(LinearLayout) layoutInflater.inflate(R.layout.editor_text_tip, null));
		additionViews.add(manualAddress=(LinearLayout) layoutInflater.inflate(R.layout.layout_set_address, null));
		additionViews.add(editorTakePhotoMenu=(LinearLayout) layoutInflater.inflate(R.layout.editor_take_picture_menu, null));
		
		for(View v:additionViews)
		{
			getScreen().addView(v, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
			v.setVisibility(View.GONE);
		}

		backButton = (Button) findViewById(R.id.bc_back_button);
		backButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onBackAction();
			}
		});

		saveButton = (Button) findViewById(R.id.bc_right_button);
		saveButton.setText("Save");
		saveButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				String uuid = carusselMainItem.save();

				if(uuid != null)
				{

				}
			}
		});
	}

	@Override
	public void onLoadCarusselItems() 
	{
		carusselMainItem = new CarusselEditorMainItem(this);
		
        getCarusselSurface().addCarusselMainItem(carusselMainItem);
	}

	@Override
	public int getContentView() 
	{
		return R.layout.activity_editor;
	}

	@Override
	public void getManualAddressText(final EditObjectAddressCallback callback, String country, String city, String street, String street_num) 
	{
		final Button   done          = (Button)   (manualAddress.findViewById(R.id.editor_manual_text_done_bt));
		final EditText countryET     = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_country_et));
		final EditText cityET        = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_city_et));
		final EditText streetET      = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_street_et));
		final EditText street_numET  = (EditText) (manualAddress.findViewById(R.id.editor_manual_text_get_street_num_et));

		countryET.setText(country);
		cityET.setText(city);
		streetET.setText(street);
		street_numET.setText(street_num);
		
		done.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
//				manualAddress.setVisibility(View.GONE);
//				getCarusselSurface().bringToFront();
//				getCarusselSurface().setVisiable(true);

				removeAdditionDislays();

				if(callback != null)
				{
					callback.onReturn(countryET.getText().toString(), cityET.getText().toString(), streetET.getText().toString(), street_numET.getText().toString());
				}
			}
		});
		
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				getCarusselSurface().setVisiable(false);
				manualAddress.bringToFront();
				manualAddress.setVisibility(View.VISIBLE);

				currentDisplayed.add(manualAddress);
			}
		});
	}
	
	@Override
	public void getTipText(final EditObjectTextTipCallback callback, final String tip) 
	{
		final Button   done          = (Button)(editorTextTipView.findViewById(R.id.editor_text_tip_done_bt));
		final TextView watch_tv      = (TextView)(editorTextTipView.findViewById(R.id.editor_text_tip_watch_tv));
		final TextView recog_tv      = (TextView)(editorTextTipView.findViewById(R.id.editor_text_tip_recog_tv));
		final TextView shortcut_tv   = (TextView)(editorTextTipView.findViewById(R.id.editor_text_tip_shortcut_tv));
		final TextView forget_tv     = (TextView)(editorTextTipView.findViewById(R.id.editor_text_tip_forget_tv));
		final EditText editor        = (EditText)(editorTextTipView.findViewById(R.id.editor_text_tip_editor));
		
		TextView allInOne[] = {watch_tv, recog_tv, shortcut_tv, forget_tv};
		
		for(int i = 0; i < allInOne.length; i++)
		{
			allInOne[i].setOnClickListener(new OnClickListener() 
			{
				@Override
				public void onClick(View v) 
				{
					String text = ((TextView)v).getText().toString();
					editor.setText(text.subSequence(0, text.length()-3));
				}
			});
		}
		
		done.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
//				editorTextTipView.setVisibility(View.GONE);
//				getCarusselSurface().bringToFront();
//				getCarusselSurface().setVisiable(true);
				removeAdditionDislays();
				if(callback != null)
				{
					callback.onReturn(editor.getText().toString());
				}
			}
		});
		
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				getCarusselSurface().setVisiable(false);
				editor.setText(tip);
				editorTextTipView.bringToFront();
				editorTextTipView.setVisibility(View.VISIBLE);
				currentDisplayed.add(editorTextTipView);
			}
		});
	}
	
	private void openCameraForTakePicture(final EditObjectPhotoTipCallback callback)
	{
		final RelativeLayout cameraArea = (RelativeLayout)(editorTakePhotoMenu.findViewById(R.id.editor_take_photo_camera_area));
		final RelativeLayout menuArea   = (RelativeLayout)(editorTakePhotoMenu.findViewById(R.id.editor_take_photo_menu_area));
		
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				final MyCameraView cameraView = new MyCameraView(EditorActivity.this);
				final ImageView    imageView  = new ImageView(EditorActivity.this);
				//
				getCarusselSurface().setVisiable(false);
				editorTakePhotoMenu.bringToFront();
				menuArea.setVisibility(View.INVISIBLE);
				
				cameraView.setFrameCallback(imageView.getFrameCallback());
				cameraArea.addView(cameraView, new RelativeLayout.LayoutParams(1, 1));
				cameraArea.addView(imageView, new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
				imageView.bringToFront();

				imageView.setOnClickListener(new OnClickListener() 
				{
					@Override
					public void onClick(View v) 
					{
						removeAdditionDislays();
						if(callback != null)
						{
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
	public void getTipPhoto(final EditObjectPhotoTipCallback callback) 
	{
		final Button         take_photo = (Button)(editorTakePhotoMenu.findViewById(R.id.editor_take_photo_open_camera));
		final Button         cancel     = (Button)(editorTakePhotoMenu.findViewById(R.id.editor_take_photo_cancel));
		final RelativeLayout cameraArea = (RelativeLayout)(editorTakePhotoMenu.findViewById(R.id.editor_take_photo_camera_area));
		final RelativeLayout menuArea   = (RelativeLayout)(editorTakePhotoMenu.findViewById(R.id.editor_take_photo_menu_area));
		
		take_photo.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				openCameraForTakePicture(callback);
			}
		});
		
		cancel.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				removeAdditionDislays();
				if(callback != null)
				{
					callback.onReturn(null);
				}
			}
		});
		
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
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
	protected void onResume() 
	{
		super.onResume();
	}

	@Override
	protected void mainServiceConnected()
	{
		carusselMainItem.setService(getService());
	}

	private void onBackAction()
	{
		if(currentDisplayed != null && currentDisplayed.size() > 0)
		{
			removeAdditionDislays();
			//
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onBackPressed()
	{
		onBackAction();
	}

	private void removeAdditionDislays()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				final RelativeLayout cameraArea = (RelativeLayout)(editorTakePhotoMenu.findViewById(R.id.editor_take_photo_camera_area));

				for(View v:currentDisplayed)
				{
					v.setVisibility(View.INVISIBLE);
				}

				if(cameraArea.getChildCount() > 0) cameraArea.removeAllViews();

				currentDisplayed.removeAllElements();
				getCarusselSurface().setVisiable(true);
			}
		});
	}
}

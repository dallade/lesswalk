package com.lesswalk.contact_page.navigation_menu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.Log;

import com.lesswalk.R;
import com.lesswalk.bases.BaseInterRendererLayout;
import com.lesswalk.bases.IContactManager;
import com.lesswalk.bases.ImageObject3D;
import com.lesswalk.bases.RectObject3D;

import java.util.Vector;

/**
 * Created by root on 1/28/16.
 */
public class NavigationContactLayout extends BaseInterRendererLayout
{
	private static final String TAG = NavigationContactLayout.class.getSimpleName();
	private static final boolean DEBUG_CONTACTS = false;

	private static final int avatar_resources[] =
	{
			R.drawable.avatar_0_1x,
			R.drawable.avatar_1_1x,
			R.drawable.avatar_2_1x
	};

	public interface NavigationContactLayoutCallback
	{
		void onContactClicked(String name, String number);
	}

	private static final int HANDLER_ATTR_VER_POS_INDEX   = 0;
	private static final int HANDLER_ATTR_TEX_COORD_INDEX = 1;
	private static final int HANDLER_ATTR_SIZE            = 2;
	private static final int HANDLER_UNIF_TEXTURE_INDEX   = 0;
	private static final int HANDLER_UNIF_FIX_MAT_INDEX   = 1;
	private static final int HANDLER_UNIF_SIZE            = 2;
	private static final int ANGLE_BETWEEN_CONTACTS       = 30;

	private int program        = -1;
	private int attrHandlers[] = null;
	private int unifHandlers[] = null;

	private static final float CENTER_X = -0.5f;
	private static final float CENTER_Y = 0.0f;
	private static final float RADIUS = 1.0f;

	private static final float MIN_DEGREE_ANGLE = 60.0f;

	private float basicTransformMatrix[] = null;
	private float currentDegree = 90;

	private Context                         context        = null;
	private IContactManager                 contactManager = null;
	private Vector<NavigationContact>       contacts       = null;
	private Vector<NavigationContact>       work_contacts  = null;
	private String                          contactFilter  = "";
	private String                          last_filter    = "";
	private NavigationContactLayoutCallback callback       = null;

	private static Bitmap avatars[] = null;

	public NavigationContactLayout(Context context, int w, int h, RendererLayoutParams params)
	{
		super(w, h, params);
		//
		this.context = context;

		if(avatars == null)
		{
			avatars = new Bitmap[avatar_resources.length];
			for(int i = 0; i < avatars.length; i++)
			{
				avatars[i] = BitmapFactory.decodeResource(context.getResources(), avatar_resources[i]).copy(Config.ARGB_8888, true);
				ImageObject3D.CircleCut(avatars[i], Color.argb(255, 255, 255, 255));
			}
		}
	}

	public void setNavigationContactLayoutCallback(NavigationContactLayoutCallback callback)
	{
		this.callback = callback;
	}
	
	public NavigationContactLayout(Context context)
	{
		this(context, 0, 0, null);
	}
	
	public void init(Context context)
	{
		program = RectObject3D.createProgram
		(
			RectObject3D.loadShaderFromGlslFile(context.getResources(), R.raw.navigation_contact_vertex_shader),
			RectObject3D.loadShaderFromGlslFile(context.getResources(), R.raw.navigation_contact_fragment_shader)
		);
		RectObject3D.checkGlError("program");
		Log.d("elazarkin4", "NavigationContactLayout.init program=" + program + " ");
		attrHandlers = new int[getAttrHandlerAmmount()];
		unifHandlers = new int[getUnifHandlerAmmount()];
		loadHandlers(attrHandlers, unifHandlers, program);
	}

	@Override
	public void setWhParams(int _w, int _h, RendererLayoutParams _params)
	{
		super.setWhParams(_w, _h, _params);

		if (_params != null)
		{
			if (basicTransformMatrix == null) basicTransformMatrix = new float[9];

			createBasicTransformMatrix
			(
				basicTransformMatrix, 
				(params.getEndWeightX() - params.getStartWeightX()) * w,
				(params.getEndWeightY() - params.getStartWeightY()) * h, 
				1.0f
			);
		}
	}

	private void createBasicTransformMatrix(float mat[], float w, float h, float scale)
	{
		mat[0] = scale * h / w;
		mat[1] = 0.0f;
		mat[2] = 0.0f;
		mat[3] = 0.0f;
		mat[4] = scale * 1.0f;
		mat[5] = 0.0f;
		mat[6] = 0.0f;
		mat[7] = 0.0f;
		mat[8] = 1.0f;
	}

	public void resetNavigationMenu()
	{
		if (contacts == null)
			return;

		for (int i = 0; i < contacts.size(); i++)
		{
			GLES20.glDeleteTextures(1, new int[] { contacts.elementAt(i).getTextureID() }, 0);
		}
		//
		contacts.removeAllElements();

		contacts = null;
	}

	protected int getAttrHandlerAmmount()
	{
		return HANDLER_ATTR_SIZE;
	}

	protected int getUnifHandlerAmmount()
	{
		return HANDLER_UNIF_SIZE;
	}

	protected void loadHandlers(int attrHandlers[], int uniformHandler[], int program)
	{
		String attrNames[] = new String[getAttrHandlerAmmount()];
		String unifNames[] = new String[getUnifHandlerAmmount()];
		
		// Add program to OpenGL ES environment
		GLES20.glUseProgram(program);
		RectObject3D.checkGlError("glUseProgram");
		attrNames[HANDLER_ATTR_VER_POS_INDEX] = "vPosition";
		attrNames[HANDLER_ATTR_TEX_COORD_INDEX] = "a_TexCoordinate";

		for (int i = 0; i < attrNames.length; i++)
		{
			attrHandlers[i] = GLES20.glGetAttribLocation(program, attrNames[i]);
			Log.d("elazarkin4", "NavigationContactLayout.init loadHandlers=" + attrHandlers[i] + " " + attrNames[i]);
		}

		unifNames[HANDLER_UNIF_FIX_MAT_INDEX] = "u_FixMat";
		unifNames[HANDLER_UNIF_TEXTURE_INDEX] = "u_Texture";

		for (int i = 0; i < unifNames.length; i++)
		{
			uniformHandler[i] = GLES20.glGetUniformLocation(program, unifNames[i]);
			Log.d("elazarkin4", "NavigationContactLayout.init loadHandlers=" + uniformHandler[i] + " " + unifNames[i]);
		}
	}

	@Override
	public void draw()
	{
		int degree = 0;
		
		GLES20.glUseProgram(program);

		GLES20.glViewport
		(
			(int) (params.getStartWeightX() * w + 0.5f), 
			(int) (params.getStartWeightY() * h + 0.5f),
			(int) ((params.getEndWeightX() - params.getStartWeightX()) * w + 0.5f),
			(int) ((params.getEndWeightY() - params.getStartWeightY()) * h + 0.5f)
		);
		
		if (contactManager == null || contactManager.getContactAmmount() <= 0)
		{
			return;
		}

		if (contacts == null) contacts = new Vector<NavigationContact>();

		if (contacts.size() < contactManager.getContactAmmount())
		{
			Vector<CarusselContact> contacts_original = new Vector<CarusselContact>();
			contactManager.fillContactVector(contacts_original);

			mergeContacts(contacts_original, contacts, 50);
			
			if(contacts.size() < contactManager.getContactAmmount()) last_filter = null;
			else last_filter = "";
		}

		//
		if(work_contacts == null)
		{
			work_contacts = new Vector<NavigationContact>();
		}
		
		if(last_filter == null || !last_filter.equals(contactFilter))
		{
			if(last_filter != null) currentDegree = MIN_DEGREE_ANGLE;
			
			work_contacts.removeAllElements();
			if(contactFilter.length() <= 0)
			{
				work_contacts.addAll(contacts);
			}
			else
			{
				for(NavigationContact c:contacts)
				{
					if(c.getName().contains(""+contactFilter))
					{
						work_contacts.add(c);
					}
				}
			}
			last_filter = contactFilter;
		}
		
		for (int i = 0; i < work_contacts.size(); i++)
		{
			degree = indexToAngle(i);
			if (degree > currentDegree - 90 && degree < currentDegree + 90)
			{
				GLES20.glUniformMatrix3fv
				(
					unifHandlers[HANDLER_UNIF_FIX_MAT_INDEX], 1, false,
					getTransformationMatrix(degree), 0
				);
				work_contacts.elementAt(i).drawSomeSelf
				(
					attrHandlers[HANDLER_ATTR_VER_POS_INDEX],
					attrHandlers[HANDLER_ATTR_TEX_COORD_INDEX], 
					unifHandlers[HANDLER_UNIF_TEXTURE_INDEX]
				);
			}
		}
		// Disable vertex array
		// GLES20.glDisableVertexAttribArray(mPositionHandle);
	}

	private float[] getTransformationMatrix(int degree)
	{
		float angle_rad = 0.0f;
		float transformationMatrix[] = new float[basicTransformMatrix.length];

		System.arraycopy(basicTransformMatrix, 0, transformationMatrix, 0, basicTransformMatrix.length);

		angle_rad = (float) Math.toRadians(degree - currentDegree);
		transformationMatrix[6] = (float) (RADIUS * Math.cos(angle_rad)) + CENTER_X;
		transformationMatrix[7] = (float) (-RADIUS * Math.sin(angle_rad)) + CENTER_Y;
		return transformationMatrix;
	}

	private int indexToAngle(int i)
	{
		return i * ANGLE_BETWEEN_CONTACTS;
	}

	private void addContact(CarusselContact contact, Vector<NavigationContact> container)
	{
		try
		{
			Bitmap bit = BitmapFactory.decodeStream(contact.getPictureIS()).copy(Config.ARGB_8888, true);
			ImageObject3D.CircleCut(bit, Color.argb(255, 255, 255, 255));
			contacts.add(new NavigationContact(bit, contact.getName(), contact.getNumber(), 128, 40));
			bit.recycle();
		}
		catch (Exception e)
		{
			contacts.add(new NavigationContact(avatars[contacts.size()%avatars.length], contact.getName(), contact.getNumber(), 128, 40));
		}
	}

	private void mergeContacts
	(
		Vector<CarusselContact> contacts_original,
		Vector<NavigationContact> contacts,
		int MAX_IN_ONE_TIME
	)
	{
		int i = 0, j = 0;
		Vector<NavigationContact> backup = null;
		String original, local;

		int counter = 0;

		backup = new Vector<NavigationContact>(contacts);

		contacts.removeAllElements();

		while (i < contacts_original.size() - 1 || j < backup.size())
		{
			int compare;
			String title = "";

			if (j >= backup.size())
			{
				for (; i < contacts_original.size(); i++)
				{
					addContact(contacts_original.elementAt(i), contacts);
					counter++;
					if (counter > MAX_IN_ONE_TIME) i = contacts_original.size();
				}
				title = "add tile group contacts:";

				original = "group";
				local = "group";
			} 
			else
			{

				original = contacts_original.elementAt(i).getName();
				local = backup.elementAt(j).getName();
				compare = original.compareTo(local);
				if (compare < 0)
				{
					title = "add new contact:";
					addContact(contacts_original.elementAt(i), contacts);
					if (i < contacts_original.size() - 1) i++;
				} 
				else
				{
					title = "add existed contact:";
					contacts.add(backup.elementAt(j));
					if (j < backup.size()) j++;
					if (compare == 0 && i < contacts_original.size() - 1) i++;
				}
			}

			if (DEBUG_CONTACTS)Log.d
			(
				TAG, 
				String.format("%s: %s(%s) %d/%d i=%d/%d j=%d/%d", 
				title, 
				original, local, 
				contacts.size(), contacts_original.size(), 
				i, contacts_original.size(), 
				j, backup.size())
			);
		}
	}

	@Override
	public String getRendererName()
	{
		return this.getClass().getName();
	}

	@Override
	public void movedAction(float lastX, float lastY, float x, float y)
	{
		float y_step = 0.0f;
		float angle_rad = 0.0f;
		float angle_deg = 0.0f;

		if (work_contacts == null || work_contacts.size() <= 0) return;

		y_step = yStepToRendererYStep(y - lastY);

		angle_rad = (float) Math.atan2(y_step, RADIUS);
		angle_deg = (float) Math.toDegrees(angle_rad);

		currentDegree -= 2 * angle_deg;

		if
		(
			currentDegree < MIN_DEGREE_ANGLE
			||
			(work_contacts.size()-1)*ANGLE_BETWEEN_CONTACTS < MIN_DEGREE_ANGLE*2
		)
		{
			currentDegree = MIN_DEGREE_ANGLE;
		}
		else if
		(
			(work_contacts.size()-1)*ANGLE_BETWEEN_CONTACTS > MIN_DEGREE_ANGLE
			&& 
			currentDegree > (work_contacts.size() - 1) * ANGLE_BETWEEN_CONTACTS - MIN_DEGREE_ANGLE
		)
		{
			currentDegree = (work_contacts.size() - 1) * ANGLE_BETWEEN_CONTACTS - MIN_DEGREE_ANGLE;
		}
	}

	@Override
	public void clickedAction(float x, float y)
	{
		int degree = 0;
		float transformMatrix[] = null;
		float corners[] = null;

		Log.d(TAG, "clicked " + x + "x" + y);

		for (int i = 0; i < work_contacts.size(); i++)
		{
			degree = indexToAngle(i);
			if (degree > currentDegree - 90 && degree < currentDegree + 90)
			{
				transformMatrix = getTransformationMatrix(degree);

				corners = work_contacts.elementAt(i).getCorners(transformMatrix);

				if (onObject(x, y, corners) && callback != null)
				{
					callback.onContactClicked(work_contacts.elementAt(i).getName(), work_contacts.elementAt(i).getPhoneNumber());
					break;
				}
			}
		}
		// context.startActivity(new Intent(context, PlayerView.class));
	}

	public void setContactManager(IContactManager manager)
	{
		this.contactManager = manager;
	}

	public void setContactFilter(String text)
	{
		contactFilter  = text;
	}

	public String getContactFilter()
	{
		return contactFilter;
	}
}

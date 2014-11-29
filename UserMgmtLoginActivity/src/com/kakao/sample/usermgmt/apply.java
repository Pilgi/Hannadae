package com.kakao.sample.usermgmt;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;

import android.R.string;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.*;
import android.support.v4.app.*;
import android.util.*;
import android.view.*;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;

public class apply extends Fragment implements OnClickListener {
	
	String urlForm="";
	
	private Spinner mSpinner = null;
	private ArrayAdapter<string> mSpinnerAdapter = null;
	boolean mInitSpinner;
	
	private Spinner hSpinner = null;
	private ArrayAdapter<string> hSpinnerAdapter = null;
	boolean hInitSpinner;
	
	private Spinner dSpinner = null;
	private ArrayAdapter<string> dSpinnerAdapter = null;
	boolean dInitSpinner;
	
	private TextView mTimeDisplay;
	private Button mPickTime;
	
	private int mHour;					//��� ��
	private int mMinute;				//��� ��

	String type="taxi";				//��� Ÿ�� in String
	String home="양덕";				//����� in String
	String des="양덕";					//������ in String
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.apply, container, false);

		Button button = (Button) v.findViewById(R.id.bt_ok);
		button.setOnClickListener(this);
		
		mSpinner = (Spinner) v.findViewById(R.id.ride_spinner);
		mSpinnerAdapter = (ArrayAdapter<string>)mSpinner.getAdapter();
		mSpinner.setAdapter(mSpinnerAdapter);
		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				if(mInitSpinner==false){
					mInitSpinner = true;
					return;
				}
				type=(String) mSpinner.getItemAtPosition(position);
			}
			public void onNothingSelected(AdapterView<?> parent){
			}
		});
		
		hSpinner = (Spinner) v.findViewById(R.id.Home_spinner);
		hSpinnerAdapter = (ArrayAdapter<string>)hSpinner.getAdapter();
		hSpinner.setAdapter(hSpinnerAdapter);
		hSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				if(hInitSpinner==false){
					hInitSpinner = true;
					return;
				}
				home=(String) hSpinner.getItemAtPosition(position);
			}
			public void onNothingSelected(AdapterView<?> parent){
			}
		});
		
		dSpinner = (Spinner) v.findViewById(R.id.Des_spinner);
		dSpinnerAdapter = (ArrayAdapter<string>)dSpinner.getAdapter();
		dSpinner.setAdapter(dSpinnerAdapter);
		dSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id){
				if(dInitSpinner==false){
					dInitSpinner = true;
					return;
				}
				des=(String) dSpinner.getItemAtPosition(position);
			}
			public void onNothingSelected(AdapterView<?> parent){
			}
		});
		
		mTimeDisplay = (TextView)v.findViewById(R.id.timeDisplay);
		mPickTime = (Button)v.findViewById(R.id.pickTime);
		mPickTime.setOnClickListener(this);
		
		Calendar c = new GregorianCalendar();
		mHour = c.get(Calendar.HOUR_OF_DAY);
		mMinute = c.get(Calendar.MINUTE);
		//UpdateNow();
		
		return v;
	}
	
	//"http://203.252.118.23/party.php?type=택시&name=daeho&car_number=3030
		//&departure_time=2014-11-28 02:03:11&departure_point=오석"
	
	
	@Override
	public void onClick(View v) {
		String sResult = "Error";
		
		urlForm+="http://gh.handong.edu/party.php?"
				+ "type="+type+"&"
				+ "name="+"daeho"+"&"
				+ "car_number="+"0000"+"&"
				+ "departure_time=2014-11-28 02:03:11"+"&"
				+ "departure_point="+home;
				//+ "destination="+ des;
		mTimeDisplay.setText(urlForm);
		switch (v.getId()) {
		
		case R.id.bt_ok:
			try 
	        {
				URL text = new URL(urlForm);
				HttpURLConnection conn = (HttpURLConnection)text.openConnection();
				conn.setRequestMethod("POST");
				conn.setDoOutput(true);
//				
//				 String body = "name=" + "daeho"+"&"+"car_num"+"3030";
//				 OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
//	             osw.write(body);
//	             osw.flush();    
//	             
	             InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8"); 
	                BufferedReader reader = new BufferedReader(tmp); 
	                StringBuilder builder = new StringBuilder(); 
	                String str; 
	                
	                while ((str = reader.readLine()) != null) {
	                    builder.append(str);
	                } 
	                sResult = builder.toString();
				 
				conn.connect();
	        }

	        catch (Exception e) { }
			
			break;
		case R.id.pickTime:
			new TimePickerDialog(getActivity(), mTimeSetListener, mHour, mMinute, false).show();

		}

	}
	
	TimePickerDialog.OnTimeSetListener mTimeSetListener = 
			new TimePickerDialog.OnTimeSetListener(){
		public void onTimeSet(TimePicker view, int hourOfDay, int minute){
			mHour=hourOfDay;
			mMinute=minute;
			UpdateNow();
		}
	};
	
	void UpdateNow(){
		if(mMinute>=10)
			mTimeDisplay.setText(String.format("%d:%d", mHour, mMinute));
		else
			mTimeDisplay.setText(String.format("%d:0%d", mHour, mMinute));
	}

}

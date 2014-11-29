/**
 * Copyright 2014 Kakao Corp.
 *
 * Redistribution and modification in source or binary forms are not permitted without specific prior written permission. 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.sample.usermgmt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.UUID;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.kakao.APIErrorResult;
import com.kakao.LogoutResponseCallback;
import com.kakao.MeResponseCallback;
import com.kakao.PushMessageBuilder;
import com.kakao.PushRegisterHttpResponseHandler;
import com.kakao.PushSendHttpResponseHandler;
import com.kakao.PushService;
import com.kakao.PushToken;
import com.kakao.Session;
import com.kakao.UnlinkResponseCallback;
import com.kakao.UpdateProfileResponseCallback;
import com.kakao.UserManagement;
import com.kakao.UserProfile;
import com.kakao.helper.Logger;
import com.kakao.helper.SharedPreferencesCache;
import com.kakao.helper.Utility;
import com.kakao.widget.ProfileLayout;
import com.kakao.widget.PushActivity;

/**
 * 가입된 사용자가 보게되는 메인 페이지로 사용자 정보 불러오기/update, 로그아웃, 탈퇴 기능을 테스트 한다.
 */
public class UsermgmtMainActivity extends FragmentActivity implements OnClickListener{
	
    private UserProfile userProfile;
    private ExtraUserPropertyLayout extraUserPropertyLayout;
    private static final String GCM_PROJECT_ID_KEY = "com.kakao.sdk.GcmProjectId";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    protected static final String PROPERTY_DEVICE_ID = "device_id";

    private GoogleCloudMessaging gcm;
    private String regId;
    private int appVer;
    protected String deviceUUID;
	final String TAG = "MainActivity";

	int mCurrentFragmentIndex;
	public final static int FRAGMENT_APPLY = 0;
	public final static int FRAGMENT_MYPAGE = 1;
	public final static int FRAGMENT_SETTING = 2;
	public final static int FRAGMENT_LIST = 3;
	
    /**
     * 로그인 또는 가입창에서 넘긴 유저 정보가 있다면 저장한다.
     * @param savedInstanceState 기존 session 정보가 저장된 객체
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
		Button bt_mypageFragment = (Button) findViewById(R.id.bt_mypageFragment);
		bt_mypageFragment.setOnClickListener(this);
		Button bt_applyFragment = (Button) findViewById(R.id.bt_applyFragment);
		bt_applyFragment.setOnClickListener(this);
		Button bt_settingFragment = (Button) findViewById(R.id.bt_settingFragment);
		bt_settingFragment.setOnClickListener(this);
		Button bt_listFragment = (Button) findViewById(R.id.bt_listFragment);
		bt_listFragment.setOnClickListener(this);

		mCurrentFragmentIndex = FRAGMENT_APPLY;
		//create 할때 토큰 등록.
		fragmentReplace(mCurrentFragmentIndex);

		requestMe();

        if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = PushToken.getRegistrationId(this);
            appVer = Utility.getAppVersion(this);
            deviceUUID = getDeviceUUID();
            if (regId.isEmpty()) {
                registerPushToken(null);
            }
        } else {
            Logger.getInstance().w("No valid Google Play Services APK found.");
        }
    }
    private void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            protected void onSuccess(final UserProfile userProfile) {
                // 성공.
                Log.w(TAG,  "성공____!!!!!");
                if(userProfile!=null)
                	Log.w(TAG,userProfile.toString());
               //Token register.
                registerToken();
                //Login 된 유저의 data를 server에 저장.
                new JoinPhp().execute(userProfile); 
            }

            @Override
            protected void onNotSignedUp() {
                // 가입 페이지로 이동
                redirectSignupActivity();
            }

            @Override
            protected void onSessionClosedFailure(final APIErrorResult errorResult) {
                // 다시 로그인 시도
                redirectLoginActivity();
            }

            @Override
            protected void onFailure(final APIErrorResult errorResult) {
                // 실패
                Toast.makeText(getApplicationContext(), "failed to update profile. msg = " + errorResult, Toast.LENGTH_LONG).show();
            }
        });
    }
    protected void registerToken()
    {
        registerPushToken(new PushRegisterHttpResponseHandler() {
            @Override
            protected void onHttpSuccess(final Integer expiredAt) {
                super.onHttpSuccess(expiredAt);
                Toast.makeText(getApplicationContext(), "succeeded to register push token", Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onHttpSessionClosedFailure(APIErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            protected void onHttpFailure(APIErrorResult errorResult) {
                super.onHttpFailure(errorResult);
                Toast.makeText(getApplicationContext(), errorResult.toString(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        userProfile = UserProfile.loadFromCache();
        if(userProfile != null)
        {
        	//userProfile이 있으면 해야할 것ㄷ
        }
    }

    private void redirectLoginActivity() {

        Intent intent = new Intent(this, UserMgmtLoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void redirectSignupActivity() {
        Intent intent = new Intent(this, UsermgmtSignupActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 사용자의 정보를 변경 저장하는 API를 호출한다.buttonMe
     */
    private void onClickUpdateProfile() {
        final Map<String, String> properties = extraUserPropertyLayout.getProperties();

        UserManagement.requestUpdateProfile(new UpdateProfileResponseCallback() {
            @Override
            protected void onSuccess(final long userId) {
                UserProfile.updateUserProfile(userProfile, properties);
                if (userProfile != null)
                    userProfile.saveUserToCache();
                Toast.makeText(getApplicationContext(), "succeeded to update user profile", Toast.LENGTH_SHORT).show();
                Logger.getInstance().d("succeeded to update user profile" + userProfile);
            }

            @Override
            protected void onSessionClosedFailure(final APIErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            protected void onFailure(final APIErrorResult errorResult) {
                String message = "failed to update user profile. msg=" + errorResult;
                Logger.getInstance().d(message);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        }, properties);
    }

    private void onClickLogout() {
        UserManagement.requestLogout(new LogoutResponseCallback() {
            @Override
            protected void onSuccess(final long userId) {
                redirectLoginActivity();
            }

            @Override
            protected void onFailure(final APIErrorResult apiErrorResult) {
                Logger.getInstance().d("failed to sign up. msg=" + apiErrorResult);
                redirectLoginActivity();
            }
        });
    }

    private void onClickUnlink() {
        final String appendMessage = getString(R.string.com_kakao_confirm_unlink);
        new AlertDialog.Builder(this)
            .setMessage(appendMessage)
            .setPositiveButton(getString(R.string.com_kakao_ok_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UserManagement.requestUnlink(new UnlinkResponseCallback() {
                            @Override
                            protected void onSuccess(final long userId) {
                                redirectLoginActivity();
                            }

                            @Override
                            protected void onSessionClosedFailure(final APIErrorResult errorResult) {
                                redirectLoginActivity();
                            }

                            @Override
                            protected void onFailure(final APIErrorResult errorResult) {
                                Logger.getInstance().d("failure to unlink. msg = " + errorResult);
                                redirectLoginActivity();
                            }
                        });
                        dialog.dismiss();
                    }
                })
            .setNegativeButton(getString(R.string.com_kakao_cancel_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();

    }


	public void fragmentReplace(int reqNewFragmentIndex) {

		Fragment newFragment = null;

		Log.d(TAG, "fragmentReplace " + reqNewFragmentIndex);
		newFragment = getFragment(reqNewFragmentIndex);

		// replace fragment
		final FragmentTransaction transaction = getSupportFragmentManager()
				.beginTransaction();

		transaction.replace(R.id.ll_fragment, newFragment);

		// Commit the transaction
		transaction.commit();

	}

	private Fragment getFragment(int idx) {
		Fragment newFragment = null;

		switch (idx) {
		case FRAGMENT_APPLY:
			newFragment = new apply();
			break;
		case FRAGMENT_MYPAGE:
			newFragment = new my();
			break;
		case FRAGMENT_SETTING:
			newFragment = new ThreeFragment();
			break;
		case FRAGMENT_LIST:
			newFragment = new list();
			break;

		default:
			Log.d(TAG, "Unhandle case");
			break;
		}

		return newFragment;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {

		case R.id.bt_listFragment:
			mCurrentFragmentIndex = FRAGMENT_LIST;
			fragmentReplace(mCurrentFragmentIndex);
			break;
		case R.id.bt_mypageFragment:
			mCurrentFragmentIndex = FRAGMENT_MYPAGE;
			fragmentReplace(mCurrentFragmentIndex);
			break;
		case R.id.bt_applyFragment:
			registerToken();
			//mCurrentFragmentIndex = FRAGMENT_APPLY;
			//fragmentReplace(mCurrentFragmentIndex);
			break;
		case R.id.bt_settingFragment:
			sendPushMessageToMe();
			//mCurrentFragmentIndex = FRAGMENT_SETTING;
			//fragmentReplace(mCurrentFragmentIndex);
			break;

		}

	}

    private boolean checkPlayServices() {
    	Log.w("error","choeck play service");
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Logger.getInstance().w("This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * kakao_strings.xml에 등록한 gcm_project_number를 이용하여 푸시 토큰을 받고, 로그인 후 알수 있는 사용자 id와 해당 기기의 유일한 device id를 이용하여 푸시 토큰을 등록한다.
     * @param registerHttpResponseHandler 푸시 토큰 등록에 대한 콜백 처리를 직접하고 싶은 경우 handler를 넘겨준다. 넘겨주지 않는 경우는 기본 handler가 동작한다.
     */
    protected void registerPushToken(final PushRegisterHttpResponseHandler registerHttpResponseHandler) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(UsermgmtMainActivity.this);
                    }
                    String gcmProjectNumber = Utility.getMetadata(UsermgmtMainActivity.this, GCM_PROJECT_ID_KEY);
                    regId = gcm.register(gcmProjectNumber);
                    return Boolean.TRUE;
                } catch (IOException ex) {
                    Logger.getInstance().w("Error :" + ex.getMessage());
                    return Boolean.FALSE;
                }
            }

            @Override
            protected void onPostExecute(final Boolean registerationResult){
                if(registerationResult) {
                    if(registerHttpResponseHandler != null) {
                        registerHttpResponseHandler.setRegId(regId, appVer);
                        PushService.registerPushToken(registerHttpResponseHandler, regId, deviceUUID);
                    } else {
                        PushService.registerPushToken(new PushRegisterHttpResponseHandler(regId, appVer){

                            @Override
                            protected void onHttpSessionClosedFailure(APIErrorResult errorResult) {
                                redirectLoginActivity();
                            }

                            @Override
                            protected void onHttpFailure(APIErrorResult errorResult) {
                                super.onHttpFailure(errorResult);
                                Toast.makeText(getApplicationContext(), errorResult.toString(), Toast.LENGTH_LONG).show();
                            }
                        }, regId, deviceUUID);
                    }
                }
            }

        }.execute(null, null, null);
    }
    // 아래는 SharedPreferencesCache를 사용하는 예제 입니다.

    protected String getDeviceUUID() {
        if(deviceUUID != null)
            return deviceUUID;

        final SharedPreferencesCache cache = Session.getAppCache();
        final String id = cache.getString(PROPERTY_DEVICE_ID);

        if (id != null) {
            deviceUUID = id;
            return deviceUUID;
        } else {
            UUID uuid = null;
            final String androidId = Secure.getString(getContentResolver(), Secure.ANDROID_ID);
            try {
                if (!"9774d56d682e549c".equals(androidId)) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    final String deviceId = ((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }

            Bundle bundle = new Bundle();
            bundle.putString(PROPERTY_DEVICE_ID, uuid.toString());
            cache.save(bundle);

            deviceUUID = uuid.toString();
            return deviceUUID;
        }
    }
		//mainthread가 아닌 곳에서 통신하기 위해서.
		private class JoinPhp extends AsyncTask<UserProfile, Void, Void>
		{
	
			@Override
			protected Void doInBackground(UserProfile... params) {
				// TODO Auto-generated method stub
				UserProfile userProfile = params[0];
				String sResult = "";
	    		String urlForm ="http://gh.handong.edu/testphp/join.php?";
	    		urlForm += "id="+userProfile.getId()+"&";
	    		urlForm += "nickname="+URLEncoder.encode(userProfile.getNickname())+"&";
	    		urlForm += "thumbnail_image="+userProfile.getThumbnailImagePath()+"&";
	    		urlForm += "phone="+URLEncoder.encode(userProfile.getProperty("phone"))+"&";
    		urlForm += "name="+URLEncoder.encode(userProfile.getProperty("name"))+"&";
    		urlForm += "student_number="+URLEncoder.encode(userProfile.getProperty("student_number"))+"&";
    		urlForm += "car_number="+URLEncoder.encode(userProfile.getProperty("car_number"))+"&";
    		if(userProfile.getProperty("have_car").equals("있음"))
    			urlForm += "have_car=1";
    		else
    			urlForm += "have_car=0";
    			try 
    	        {
    				URL text = new URL(urlForm);
    				HttpURLConnection conn = (HttpURLConnection)text.openConnection();
    				conn.setRequestMethod("POST");
    				conn.setDoOutput(true);
    	             InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "euc-kr"); 
    	                BufferedReader reader = new BufferedReader(tmp); 
    	                StringBuilder builder = new StringBuilder(); 
    	                String str; 
    	                
    	                while ((str = reader.readLine()) != null) {
    	                    builder.append(str);
    	                } 
    	                sResult = builder.toString();
    				 
    				conn.connect();
    	        }
    	        catch (Exception e) {
    	        	Log.w("sign error", urlForm);
    	        	Log.w("sign error", e);
    	        }

			return null;

		}
		
	}

	    private void sendPushMessageToMe() {
	        final String testMessage = new PushMessageBuilder("{\"content\":\"테스트 메시지\", \"friend_id\":1, \"noti\":\"test\"}").toString();
	        if (testMessage == null) {
	            Logger.getInstance().w("failed to create push Message");
	        } else {
	            PushService.sendPushMessage(new PushSendHttpResponseHandler() {
	                @Override
	                protected void onHttpSuccess(Void resultObj) {
	                    Toast.makeText(getApplicationContext(), "succeeded to send message", Toast.LENGTH_SHORT).show();
	                }

	                @Override
	                protected void onHttpSessionClosedFailure(APIErrorResult errorResult) {
	                    redirectLoginActivity();
	                }

	                @Override
	                protected void onHttpFailure(APIErrorResult errorResult) {
	                    Toast.makeText(getApplicationContext(), errorResult.toString(), Toast.LENGTH_LONG).show();
	                }
	            }, testMessage, deviceUUID);
	        }
	    }

}

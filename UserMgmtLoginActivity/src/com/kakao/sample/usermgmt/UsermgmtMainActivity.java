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
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.kakao.APIErrorResult;
import com.kakao.LogoutResponseCallback;
import com.kakao.MeResponseCallback;
import com.kakao.UnlinkResponseCallback;
import com.kakao.UpdateProfileResponseCallback;
import com.kakao.UserManagement;
import com.kakao.UserProfile;
import com.kakao.helper.Logger;
import com.kakao.widget.ProfileLayout;

/**
 * 가입된 사용자가 보게되는 메인 페이지로 사용자 정보 불러오기/update, 로그아웃, 탈퇴 기능을 테스트 한다.
 */
public class UsermgmtMainActivity extends FragmentActivity implements OnClickListener{
	
    private UserProfile userProfile;
    private ExtraUserPropertyLayout extraUserPropertyLayout;

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
		fragmentReplace(mCurrentFragmentIndex);
		requestMe();
    }
    private void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            protected void onSuccess(final UserProfile userProfile) {
                // 성공.
                Log.w(TAG,  "성공");
                if(userProfile!=null)
                	Log.w(TAG,userProfile.toString());
                new JoinPhp().execute(userProfile);
             			
               //Login 된 유저의 data를 server에 저장.
               String test = userProfile.getNickname();
               Toast.makeText(getApplicationContext(), test, Toast.LENGTH_LONG).show();
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
			mCurrentFragmentIndex = FRAGMENT_APPLY;
			fragmentReplace(mCurrentFragmentIndex);
			break;
		case R.id.bt_settingFragment:
			mCurrentFragmentIndex = FRAGMENT_SETTING;
			fragmentReplace(mCurrentFragmentIndex);
			break;

		}

	}
	private class JoinPhp extends AsyncTask<UserProfile, Void, Void>
	{

		@Override
		protected Void doInBackground(UserProfile... params) {
			// TODO Auto-generated method stub
			UserProfile userProfile = params[0];
			String sResult = "Error";
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
}

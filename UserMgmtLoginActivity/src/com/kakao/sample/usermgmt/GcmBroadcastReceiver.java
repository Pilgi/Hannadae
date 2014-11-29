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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * com.google.android.c2dm.intent.RECEIVE intent를 처리하는 WakefulBroadcastReceiver 구현체.
 * BroadcastReceiver의 확장 버젼으로 메시지 처리 중에 기기가 sleep하여 메시지 처리가 정상적으로 처리되지 않는 경우를 막아준다.
 * @author MJ
 */
public class GcmBroadcastReceiver extends WakefulBroadcastReceiver {

    /**
     * 메시지가 전달되면 호출되고, 메시지 처리 중에 기기가 sleep하지 않도록 wake lock을 잡고 {@link com.kakao.sample.push.GcmIntentService} 서비스를 시작한다.
     * @param context 수행중인 컨테스트
     * @param intent 서비스를 시작시킬 때 넘겨줄 intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.w("이것","실행되나요??");
        try{
        Log.w("이것",context.getPackageName());
        Log.w("이것",GcmIntentService.class.getName());
    	ComponentName comp = new ComponentName(context.getPackageName(), GcmIntentService.class.getName());
    	Log.w("이것","Service ");
        startWakefulService(context, (intent.setComponent(comp)));
        setResultCode(Activity.RESULT_OK);
        }
        catch(Exception exception)
        {
        	Log.w("error",exception);
        }
    }
}

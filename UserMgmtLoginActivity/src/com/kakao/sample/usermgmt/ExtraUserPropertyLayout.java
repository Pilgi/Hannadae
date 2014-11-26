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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;

/**
 * 추가로 받고자 하는 사용자 정보를 나타내는 layout
 * 이름, 나이, 성별을 입력할 수 있다.
 * @author MJ
 */
public class ExtraUserPropertyLayout extends FrameLayout {
    // property key
    private  static final String NAME_KEY = "name";
    private  static final String PHONE_KEY = "phone";
    private  static final String STUDENT_KEY = "student_number";
    private  static final String CAR_KEY = "car_number";
    private  static final String HAVECAR_KEY = "have_car";

    private EditText name;
    private EditText student;
    private EditText phone;
    private EditText car_number;
    private Spinner have_car;

    public ExtraUserPropertyLayout(Context context) {
        super(context);
    }

    public ExtraUserPropertyLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtraUserPropertyLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow () {
        super.onAttachedToWindow();
        final View view = inflate(getContext(), R.layout.extra_user_property, this);
        name = (EditText) view.findViewById(R.id.name);
        student = (EditText) view.findViewById(R.id.student_number);
        phone = (EditText) view.findViewById(R.id.phone);
        have_car = (Spinner) view.findViewById(R.id.have_car);
        car_number = (EditText) view.findViewById(R.id.car_number);
    }

    Map<String, String> getProperties(){
        final String nickNameValue = name.getText().toString();
        final String studentValue = student.getText().toString();
        final String phoneValue = phone.getText().toString();
        final String have_carValue = String.valueOf(have_car.getSelectedItem());
        final String car_numberValue = car_number.getText().toString();

        Map<String, String> properties = new HashMap<String, String>();
        if(nickNameValue != null)
            properties.put(NAME_KEY, nickNameValue);
        if(studentValue != null)
            properties.put(STUDENT_KEY, studentValue);
        if(phoneValue != null)
        	properties.put(PHONE_KEY, phoneValue);
        if(car_numberValue != null)
        	properties.put(CAR_KEY, car_numberValue);
        if(have_carValue != null)
            properties.put(HAVECAR_KEY, have_carValue);
        if(nickNameValue.length()==0 || studentValue.length()==0 || phoneValue.length()==0)
        	return null;
        return properties;
    }

    void showProperties(final Map<String, String> properties) {
        final String nameValue = properties.get(NAME_KEY);
        if (nameValue != null)
            name.setText(nameValue);

        final String studentValue = properties.get(STUDENT_KEY);
        if (studentValue != null)
            student.setText(studentValue);
        
        final String phoneValue = properties.get(PHONE_KEY);
        if (phoneValue != null)
        	phone.setText(phoneValue);
        
        final String car_numberValue = properties.get(CAR_KEY);
        if (car_numberValue != null)
        	car_number.setText(car_numberValue);

        final String have_carValue = properties.get(HAVECAR_KEY);
        if (have_carValue != null) {
            ArrayAdapter<String> myAdap = (ArrayAdapter<String>) have_car.getAdapter(); //cast to an ArrayAdapter
            int spinnerPosition = myAdap.getPosition(have_carValue);
            have_car.setSelection(spinnerPosition);
        }
    }


}

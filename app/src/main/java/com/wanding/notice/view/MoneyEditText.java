package com.wanding.notice.view;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * EditText输入框内容输入变化监听
 * （主要用于金额输入）
 * 使用方法：
 * EditText et = findviewById(R.id.etNum);
 * MoneyEditText.setPricePoint(et);
 */
public class MoneyEditText {
	
	public static void setPricePoint(final EditText editText) {
		editText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//如果输入内容包含小数点,
				if (s.toString().contains(".")) {
					//小数点后位数只能输入两位
                    if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 3);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    editText.setText(s);
                    editText.setSelection(2);
                }
                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                        return;
                    }
                }
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}

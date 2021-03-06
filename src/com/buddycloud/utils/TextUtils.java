package com.buddycloud.utils;

import android.text.Html;
import android.text.Spanned;

public class TextUtils {

	public static Spanned anchor(String text) {
		if (text == null) {
			return null;
		}
		text = text.replaceAll("(\\S+://[^<>[:space:]]+[[:alnum:]/])", "<a href=\"$1\">$1</a>");
		text = text.replaceAll("\\n", "<br>");
		return Html.fromHtml(text);
	}
}

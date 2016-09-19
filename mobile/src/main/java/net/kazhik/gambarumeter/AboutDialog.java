package net.kazhik.gambarumeter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.TextView;

public class AboutDialog extends DialogFragment {
	public static AboutDialog newInstance() {
		
		AboutDialog frag = new AboutDialog();
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity activity = getActivity();
		PackageInfo pkgInfo = null;
		try {
			pkgInfo = activity.getPackageManager().getPackageInfo(
					activity.getPackageName(), PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return null;
		}
		
		Resources res = getResources();
		StringBuffer aboutText = new StringBuffer();
		aboutText.append(res.getString(R.string.app_name));
		aboutText.append("\n\n");
		aboutText.append("Version: " + pkgInfo.versionName);
		aboutText.append("\n");
		aboutText.append("Website: github.com/kazhik/Textalk");
		final SpannableString sstr = new SpannableString(aboutText.toString());
		Linkify.addLinks(sstr, Linkify.ALL);

		return new AlertDialog.Builder(getActivity())
				.setPositiveButton(android.R.string.ok, null)
				.setMessage(sstr)
				.create();
	}
	@Override
	public void onStart() {
		super.onStart();
		((TextView) getDialog().findViewById(android.R.id.message))
				.setMovementMethod(LinkMovementMethod.getInstance());
	}
}

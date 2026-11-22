package Ir.co.tfs.farazaman.layout;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.graphics.drawable.GradientDrawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import Ir.co.tfs.farazaman.R;

/** Temporary AndroidView boundary for the location listener while its owner screen is Compose. */
final class GpsStatusViewFactory {
    private GpsStatusViewFactory() {}

    static void populate(LinearLayout parent) {
        Context context = parent.getContext();
        parent.setOrientation(LinearLayout.HORIZONTAL);
        parent.setGravity(Gravity.CENTER_VERTICAL);
        parent.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        parent.setPadding(dp(context, 12), dp(context, 8), dp(context, 12), dp(context, 8));
        GradientDrawable background = new GradientDrawable();
        background.setColor(ContextCompat.getColor(context, R.color.app_surface));
        background.setCornerRadius(dp(context, 24));
        background.setStroke(dp(context, 1), ContextCompat.getColor(context, R.color.app_outline));
        parent.setBackground(background);

        TextView heading = text(context, R.id.gpsstatus_record_tvHeading, "");
        heading.setContentDescription(context.getString(R.string.various_heading));
        heading.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_compass_heading, 0, 0, 0);
        heading.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(
                ContextCompat.getColor(context, R.color.icon_gray)));
        heading.setCompoundDrawablePadding(dp(context, 4));
        parent.addView(heading, new LinearLayout.LayoutParams(dp(context, 76), LinearLayout.LayoutParams.MATCH_PARENT));

        TextView accuracy = text(context, R.id.gpsstatus_record_tvAccuracy, context.getString(R.string.various_waiting_gps_fix));
        parent.addView(accuracy, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));

        ImageView satellites = image(context, R.id.gpsstatus_record_imgSatIndicator, R.drawable.ic_gps_fix, R.string.acc_sat_indicator);
        satellites.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray));
        parent.addView(satellites, iconParams(context));

        ImageView recording = image(context, R.id.gpsstatus_record_animRec, R.drawable.ic_route_recording, R.string.acc_record_indicator);
        recording.setColorFilter(ContextCompat.getColor(context, R.color.icon_gray));
        parent.addView(recording, iconParams(context));
    }

    private static TextView text(Context context, int id, String value) {
        TextView view = new TextView(context);
        view.setId(id);
        view.setGravity(Gravity.CENTER);
        view.setText(value);
        view.setTextColor(ContextCompat.getColor(context, R.color.colorPrimaryTextLight));
        view.setTypeface(ResourcesCompat.getFont(context, R.font.iran_yekan));
        return view;
    }

    private static ImageView image(Context context, int id, int drawable, int description) {
        ImageView view = new ImageView(context);
        view.setId(id);
        view.setImageResource(drawable);
        view.setContentDescription(context.getString(description));
        return view;
    }

    private static LinearLayout.LayoutParams iconParams(Context context) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(context, 40), dp(context, 40));
        params.setMarginStart(dp(context, 4));
        return params;
    }

    private static int dp(Context context, int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}

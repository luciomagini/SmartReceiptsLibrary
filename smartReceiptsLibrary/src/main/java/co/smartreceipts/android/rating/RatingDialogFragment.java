package co.smartreceipts.android.rating;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import co.smartreceipts.android.R;
import co.smartreceipts.android.SmartReceiptsApplication;
import co.smartreceipts.android.analytics.Analytics;
import co.smartreceipts.android.analytics.events.Events;
import co.smartreceipts.android.rating.data.AppRatingManager;
import co.smartreceipts.android.rating.data.AppRatingPreferencesStorage;
import co.smartreceipts.android.utils.IntentUtils;

//// TODO: 03.03.2017 There was Analytics in the old AppRating class but it wasn't used. Is it needed here?

/**
 * Dialog Fragment which asks if user wants to rate the app
 */
public class RatingDialogFragment extends DialogFragment {

    private Analytics analytics;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.apprating_dialog_title, getApplicationName()))
                .setMessage(R.string.apprating_dialog_message)
                .setNegativeButton(R.string.apprating_dialog_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserSelectedNever);
                        dismiss();
                    }
                })
                .setNeutralButton(R.string.apprating_dialog_neutral, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserSelectedLater);
                        prorogueRatingPrompt();
                    }
                })
                .setPositiveButton(R.string.apprating_dialog_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        analytics.record(Events.Ratings.UserSelectedRate);
                        launchRatingIntent();
                    }
                });
        return builder.create();
    }

    private void launchRatingIntent() {
        Context context = getContext();
        if (context != null) {
            context.startActivity(IntentUtils.getRatingIntent(context));
        }
    }

    private void prorogueRatingPrompt() {
        Context context = getContext();
        if (context != null) {
            AppRatingManager ratingManager = AppRatingManager.getInstance(new AppRatingPreferencesStorage(context));
            ratingManager.prorogueRatingPrompt();
        }
    }

    private String getApplicationName() {
        final PackageManager packageManager = getContext().getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getContext().getPackageName(), 0);
            return packageManager.getApplicationLabel(applicationInfo).toString();
        } catch (final PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initAnalytics();
    }

    private void initAnalytics() {
        final SmartReceiptsApplication smartReceiptsApplication = ((SmartReceiptsApplication)getActivity().getApplication());
        analytics = smartReceiptsApplication.getAnalyticsManager();
    }
}

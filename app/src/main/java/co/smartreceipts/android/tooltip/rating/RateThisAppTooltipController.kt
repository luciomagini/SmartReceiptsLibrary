package co.smartreceipts.android.tooltip.rating

import android.content.Context
import androidx.annotation.AnyThread
import androidx.annotation.UiThread
import co.smartreceipts.android.R
import co.smartreceipts.android.analytics.Analytics
import co.smartreceipts.android.analytics.events.Events
import co.smartreceipts.android.di.scopes.FragmentScope
import co.smartreceipts.android.rating.AppRatingManager
import co.smartreceipts.android.tooltip.TooltipView
import co.smartreceipts.android.tooltip.TooltipController
import co.smartreceipts.android.tooltip.model.TooltipInteraction
import co.smartreceipts.android.tooltip.model.TooltipMetadata
import co.smartreceipts.android.tooltip.model.TooltipType
import co.smartreceipts.android.utils.log.Logger
import com.hadisatrio.optional.Optional
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

/**
 * An implementation of the [TooltipController] contract to display a "Rate This App" tooltip
 */
@FragmentScope
class RateThisAppTooltipController @Inject constructor(private val context: Context,
                                                       private val tooltipView: TooltipView,
                                                       private var router: RateThisAppTooltipRouter,
                                                       private val appRatingManager: AppRatingManager,
                                                       private val analytics: Analytics) : TooltipController {

    @UiThread
    override fun shouldDisplayTooltip(): Single<Optional<TooltipMetadata>> {
        return appRatingManager.checkIfNeedToAskRating()
                .map { shouldShow -> if (shouldShow) Optional.of(newTooltipMetadata()) else Optional.absent() }
                .doOnSuccess{
                    if (it.isPresent) {
                        analytics.record(Events.Ratings.RatingPromptShown)
                    }
                }
    }

    @AnyThread
    override fun handleTooltipInteraction(interaction: TooltipInteraction): Completable {
        return Completable.fromCallable {
            if (interaction == TooltipInteraction.YesButtonClick) {
                appRatingManager.dontShowRatingPromptAgain()
                analytics.record(Events.Ratings.UserAcceptedRatingPrompt)
                Logger.info(this, "User clicked 'Yes' on the rating tooltip")
            } else if (interaction == TooltipInteraction.NoButtonClick) {
                appRatingManager.dontShowRatingPromptAgain()
                analytics.record(Events.Ratings.UserDeclinedRatingPrompt)
                Logger.info(this, "User clicked 'No' on the rating tooltip")
            }
        }.subscribeOn(Schedulers.io())
    }

    @UiThread
    override fun consumeTooltipInteraction(): Consumer<TooltipInteraction> {
        return Consumer {
            when (it) {
                TooltipInteraction.YesButtonClick -> {
                    router.navigateToRatingOptions()
                    tooltipView.hideTooltip()
                }
                TooltipInteraction.NoButtonClick -> {
                    router.navigateToFeedbackOptions()
                    tooltipView.hideTooltip()
                }
                else -> Logger.warn(this, "Handling unknown tooltip interaction: {}", it)
            }
        }
    }

    private fun newTooltipMetadata() : TooltipMetadata {
        return TooltipMetadata(TooltipType.RateThisApp, context.getString(R.string.rating_tooltip_text))
    }

}
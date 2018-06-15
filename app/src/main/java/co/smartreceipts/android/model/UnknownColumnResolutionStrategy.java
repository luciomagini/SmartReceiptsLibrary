package co.smartreceipts.android.model;

import android.support.annotation.NonNull;

import co.smartreceipts.android.sync.model.SyncState;

/**
 * Defines what actions should be taken if we do not know how to generate a particular column
 * from a set of {@link ColumnDefinitions}.
 */
public interface UnknownColumnResolutionStrategy<T> {

    /**
     * Resolves a conflict in some manner for a given string name
     *
     * @param id the unique id for the column that could not be found
     * @param columnName the name that could not be found
     * @param syncState the associated {@link SyncState}
     * @param customOrderId the associated custom order id
     *
     * @return a {@link Column} that best represents the unknown name
     */
    @NonNull
    Column<T> resolve(int id, @NonNull String columnName, @NonNull SyncState syncState, long customOrderId);
}

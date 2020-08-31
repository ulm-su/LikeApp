package org.likeapp.likeapp.model;

import android.content.Context;

import org.likeapp.likeapp.adapter.ActivitySummariesAdapter;
import org.likeapp.likeapp.entities.BaseActivitySummary;
import org.likeapp.likeapp.impl.GBDevice;

import java.util.List;

public class ActivitySummaryItems {
    private final GBDevice device;
    private int activityKindFilter;
    List<BaseActivitySummary> allItems;
    ActivitySummariesAdapter itemsAdapter;
    private int current_position = 0;
    long dateFromFilter=0;
    long dateToFilter=0;


    public ActivitySummaryItems(Context context, GBDevice device, int activityKindFilter, long dateFromFilter, long dateToFilter, String nameContainsFilter) {
        this.device = device;
        this.activityKindFilter = activityKindFilter;
        this.dateFromFilter=dateFromFilter;
        this.dateToFilter=dateToFilter;
        this.itemsAdapter = new ActivitySummariesAdapter(context, device, activityKindFilter, dateFromFilter, dateToFilter, nameContainsFilter);
    }

    public BaseActivitySummary getItem(int position){
        current_position=position;
        return itemsAdapter.getItem(position);
    }

    public int getPosition(BaseActivitySummary item){
        return itemsAdapter.getPosition(item);
    }

    public List<BaseActivitySummary> getAllItems(){
        return itemsAdapter.getItems();
    }

    public BaseActivitySummary getNextItem(){
        if (current_position+1 < itemsAdapter.getCount()){
            current_position+=1;
            return itemsAdapter.getItem(current_position);
        }
        return null;
    }

    public BaseActivitySummary getPrevItem(){
        if (current_position-1 >= 0){
            current_position-=1;
            return itemsAdapter.getItem(current_position);
        }
        return null;
    }

    public int getCurrent_position(){
        return current_position;
    }

}

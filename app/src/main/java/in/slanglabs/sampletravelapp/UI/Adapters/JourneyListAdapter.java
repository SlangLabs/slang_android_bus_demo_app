package in.slanglabs.sampletravelapp.UI.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import in.slanglabs.sampletravelapp.Model.JourneyBusPlace;
import in.slanglabs.sampletravelapp.R;
import in.slanglabs.sampletravelapp.UI.ItemClickListener;
import in.slanglabs.sampletravelapp.UI.ViewHolder.JourneyViewHolder;

import java.util.ArrayList;
import java.util.List;

public class JourneyListAdapter extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ItemClickListener itemClickListener;

    public JourneyListAdapter(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    private List<JourneyBusPlace> list = new ArrayList<>();

    public void setList(List<JourneyBusPlace> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View orderItem = LayoutInflater
                .from(parent.getContext()).inflate(
                        R.layout.journey_list_item,
                        parent, false);
        return new JourneyViewHolder(orderItem,itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        JourneyViewHolder viewHolder = (JourneyViewHolder) holder;
        JourneyBusPlace journeyBusPlace = list.get(position);
        viewHolder.setData(journeyBusPlace);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

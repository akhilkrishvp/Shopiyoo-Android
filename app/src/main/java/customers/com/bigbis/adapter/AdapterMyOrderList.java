package customers.com.bigbis.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import customers.com.bigbis.R;
import customers.com.bigbis.activity.TrackerDetailActivity;
import customers.com.bigbis.model.OrderTracker;

public class AdapterMyOrderList extends RecyclerView.Adapter<AdapterMyOrderList.ItemHolder> {

    Activity activity;
    ArrayList<OrderTracker> orderTrackerArrayList;

    public AdapterMyOrderList(Activity activity, ArrayList<OrderTracker> orderTrackerArrayList) {
        this.activity = activity;
        this.orderTrackerArrayList = orderTrackerArrayList;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_my_order_item, null);
        ItemHolder cartItemHolder = new ItemHolder(v);
        return cartItemHolder;
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, final int position) {
        final OrderTracker order = orderTrackerArrayList.get(position);
        holder.txtOrderId.setText("Order ID #"+order.getOrder_id());
        String[] date = order.getDate_added().split("\\s+");
        holder.txtOrderDate.setText("Order at: "+date[0]);
        holder.txtTotalPaid.setText("Paid total: ₹ "+order.getFinal_total());
        holder.txtOrderStatus.setText(order.getStatus());

        holder.itemView.setOnClickListener(v -> activity.startActivity(new Intent(activity, TrackerDetailActivity.class).putExtra("model", order)));

        if (order.getStatus().equalsIgnoreCase("cancelled")) {
            holder.txtOrderStatus.setTextColor(ContextCompat.getColor(activity, R.color.red));
        } else {
            holder.txtOrderStatus.setTextColor(ContextCompat.getColor(activity, R.color.green_branded));
        }



    }

    public class ItemHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtOrderDate, txtTotalPaid, txtOrderStatus;

        public ItemHolder(View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.orderIdTV);
            txtOrderDate = itemView.findViewById(R.id.orderDateTV);
            txtTotalPaid = itemView.findViewById(R.id.totalPaidTV);
            txtOrderStatus = itemView.findViewById(R.id.orderStatusTV);


        }
    }


    @Override
    public int getItemCount() {

        return orderTrackerArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
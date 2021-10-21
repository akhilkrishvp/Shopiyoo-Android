package customers.com.shopiyoo.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import customers.com.shopiyoo.R;
import customers.com.shopiyoo.activity.OrderListActivity;
import customers.com.shopiyoo.adapter.AdapterMyOrderList;
import customers.com.shopiyoo.adapter.TrackerAdapter;
import customers.com.shopiyoo.helper.Session;
import customers.com.shopiyoo.model.OrderTracker;


public class OrderTrackerListFragment extends Fragment {

    RecyclerView recyclerView;
    TextView nodata;
    ProgressBar progressbar;
    Session session;
    private ArrayList<OrderTracker> orderTrackerArrayList;
    int pos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_ordertrackerlist, container, false);

        pos = getArguments().getInt("pos");
        session = new Session(getActivity());
        progressbar = v.findViewById(R.id.progressbar);
        recyclerView = v.findViewById(R.id.recycleview);
        nodata = (TextView) v.findViewById(R.id.nodata);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        switch (pos) {
            case 0:
                orderTrackerArrayList = OrderListActivity.orderTrackerslist;
                break;
            case 1:
                orderTrackerArrayList = OrderListActivity.processedlist;
                break;
            case 2:
                orderTrackerArrayList = OrderListActivity.shippedlist;
                break;
            case 3:
                orderTrackerArrayList = OrderListActivity.deliveredlist;
                break;
            case 4:
                orderTrackerArrayList = OrderListActivity.cancelledlist;
                break;
            case 5:
                orderTrackerArrayList = OrderListActivity.returnedList;
                break;
        }

        if (orderTrackerArrayList.size() == 0)
            nodata.setVisibility(View.VISIBLE);


        recyclerView.setAdapter(new AdapterMyOrderList(getActivity(), orderTrackerArrayList));
        progressbar.setVisibility(View.GONE);
        return v;
    }


    @Override
    public void onResume() {
        super.onResume();

    }
}

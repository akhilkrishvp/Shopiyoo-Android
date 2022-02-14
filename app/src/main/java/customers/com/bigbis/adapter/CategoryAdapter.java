package customers.com.bigbis.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import customers.com.bigbis.R;
import customers.com.bigbis.activity.ProductListActivity;
import customers.com.bigbis.activity.SubCategoryActivity;
import customers.com.bigbis.helper.RoundRectCornerImageView;
import customers.com.bigbis.helper.Utils;
import customers.com.bigbis.model.Category;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    public ArrayList<Category> categorylist;
    int layout;
    String from = "";
    Activity activity;


    public CategoryAdapter(Activity activity, ArrayList<Category> categorylist, int layout, String from) {
        this.categorylist = categorylist;
        this.layout = layout;
        this.activity = activity;
        this.from = from;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(view);
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Category model = categorylist.get(position);
        holder.txttitle.setText(model.getName());
        if(model.getId().equalsIgnoreCase("-1")){
           // holder.imgcategory.setImageResource(R.drawable.ic_view_all);
            holder.imgcategory.setVisibility(View.GONE);
            holder.viewAll.setVisibility(View.VISIBLE);
        }
        else {
            holder.viewAll.setVisibility(View.GONE);
            Glide.with(activity).load(model.getImage())
                    .apply(Utils.getImagePlaceholder()).into(holder.imgcategory);

           /* holder.imgcategory.setDefaultImageResId(R.drawable.placeholder);
            holder.imgcategory.setErrorImageResId(R.drawable.placeholder);
            holder.imgcategory.setImageUrl(model.getImage(), Constant.imageLoader);*/
        }
        holder.lytMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                if (from.equals("cate")) {
                    intent = new Intent(activity, SubCategoryActivity.class);
                } else if (from.equals("sub_cate")) {
                    intent = new Intent(activity, ProductListActivity.class);
                    intent.putExtra("from", "regular");
                }
                intent.putExtra("id", model.getId());
                intent.putExtra("name", model.getName());
                activity.startActivity(intent);
            }
        });
        holder.viewAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                intent = new Intent(activity, SubCategoryActivity.class);
                intent.putExtra("id", model.getId());
                intent.putExtra("name", model.getName());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categorylist.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView txttitle;
        RoundRectCornerImageView imgcategory;
        LinearLayout viewAll;
        LinearLayout lytMain;

        public ViewHolder(View itemView) {
            super(itemView);
            lytMain = itemView.findViewById(R.id.lytMain);
            imgcategory = itemView.findViewById(R.id.imgcategory);
            viewAll =  itemView.findViewById(R.id.viewAll);
            txttitle = itemView.findViewById(R.id.txttitle);
        }

    }
}

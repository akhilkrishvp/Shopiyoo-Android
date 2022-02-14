package customers.com.bigbis.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import customers.com.bigbis.R;
import customers.com.bigbis.adapter.CategoryAdapter;
import customers.com.bigbis.adapter.OfferAdapter;
import customers.com.bigbis.adapter.SectionAdapter;
import customers.com.bigbis.adapter.SliderAdapter;
import customers.com.bigbis.helper.ApiConfig;
import customers.com.bigbis.helper.AppController;
import customers.com.bigbis.helper.Constant;
import customers.com.bigbis.helper.DatabaseHelper;
import customers.com.bigbis.helper.Session;
import customers.com.bigbis.helper.VolleyCallback;
import customers.com.bigbis.model.Category;
import customers.com.bigbis.model.Slider;

public class MainActivity extends DrawerActivity {
    boolean doubleBackToExitPressedOnce = false;
    DatabaseHelper databaseHelper;
    public static Session session;

    Toolbar toolbar;
    public RelativeLayout layoutSearch;
    Activity activity;
    public LinearLayout lytBottom;
    Menu menu;
    String from;
    private RecyclerView categoryRecyclerView, sectionView, offerView;
    private ArrayList<Slider> sliderArrayList;
    public static ArrayList<Category> categoryArrayList, sectionList;
    public static ArrayList<Category> homeSubcategoryList = new ArrayList<>();
    private ViewPager mPager;
    private LinearLayout mMarkersLayout;
    private int size;
    private Timer swipeTimer;
    private Handler handler;
    private Runnable Update;
    private int currentPage = 0;
     int subcategory_listSize = 8;
    private LinearLayout lytCategory;
    NestedScrollView nestedScrollView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.activity_main, frameLayout);
        databaseHelper = new DatabaseHelper(MainActivity.this);
      //  databaseHelper.DeleteAllOrderData();
        session = new Session(MainActivity.this);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        //pull to refresh home page
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);

        activity = MainActivity.this;
        from = getIntent().getStringExtra("from");
        progressBar = findViewById(R.id.progressBar);
        lytBottom = findViewById(R.id.lytBottom);
        layoutSearch = findViewById(R.id.layoutSearch);
        layoutSearch.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

//        setAppLocal("your_app_language_code_here");

        categoryRecyclerView = findViewById(R.id.categoryrecycleview);
        categoryRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
        // categoryRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false));
        sectionView = findViewById(R.id.sectionView);
        sectionView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        sectionView.setNestedScrollingEnabled(false);

        offerView = findViewById(R.id.offerView);
        offerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        offerView.setNestedScrollingEnabled(false);

        nestedScrollView = findViewById(R.id.nestedScrollView);
        mMarkersLayout = findViewById(R.id.layout_markers);
        lytCategory = findViewById(R.id.lytCategory);
        mPager = findViewById(R.id.pager);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                ApiConfig.addMarkers(position, sliderArrayList, mMarkersLayout, MainActivity.this);
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        mPager.setPageMargin(5);
        if (session.isUserLoggedIn()) {
            tvName.setText(session.getData(Session.KEY_NAME));
            tvMobile.setText(session.getData(Session.KEY_MOBILE));
        } else {
            tvName.setText(getResources().getString(R.string.is_login));
        }


        drawerToggle = new ActionBarDrawerToggle
                (
                        this,
                        drawer, toolbar,
                        R.string.drawer_open,
                        R.string.drawer_close
                ) {
        };
        drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String token = instanceIdResult.getToken();
                if (!token.equals(session.getData(Session.KEY_FCM_ID))) {
                    UpdateToken(token, MainActivity.this);
                }
            }
        });

        if (AppController.isConnected(MainActivity.this)) {
            ApiConfig.GetSettings(MainActivity.this);
            GetSlider();
            GetCategoryById();
            SectionProductRequest();
            GetOfferImage();
            ApiConfig.displayLocationSettingsRequest(MainActivity.this);

            if (Constant.REFER_EARN_ACTIVE.equals("0")) {
                Menu nav_Menu = navigationView.getMenu();
                nav_Menu.findItem(R.id.refer).setVisible(false);
            }


        }
        ApiConfig.getLocation(MainActivity.this);

        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // your code
                if (AppController.isConnected(MainActivity.this)) {
                    ApiConfig.GetSettings(MainActivity.this);
                    GetSlider();
                   // GetCategoryById();
                    SectionProductRequest();
                    GetOfferImage();
                    ApiConfig.displayLocationSettingsRequest(MainActivity.this);

                    if (Constant.REFER_EARN_ACTIVE.equals("0")) {
                        Menu nav_Menu = navigationView.getMenu();
                        nav_Menu.findItem(R.id.refer).setVisible(false);
                    }
                }
                pullToRefresh.setRefreshing(false);
            }
        });
    }

//    public void setAppLocal(String languageCode) {
//        Resources resources = getResources();
//        DisplayMetrics dm = resources.getDisplayMetrics();
//        Configuration configuration = resources.getConfiguration();
//        configuration.setLocale(new Locale(languageCode.toLowerCase()));
//        resources.updateConfiguration(configuration, dm);
//    }

    public void GetOfferImage() {
        Map<String, String> params = new HashMap<String, String>();
        params.put(Constant.GET_OFFER_IMAGE, Constant.GetVal);
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        ArrayList<String> offerList = new ArrayList<>();
                        JSONObject objectbject = new JSONObject(response);
                        if (!objectbject.getBoolean(Constant.ERROR)) {
                            JSONArray jsonArray = objectbject.getJSONArray(Constant.DATA);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject object = jsonArray.getJSONObject(i);
                                offerList.add(object.getString(Constant.IMAGE));
                            }
                            offerView.setAdapter(new OfferAdapter(offerList, R.layout.offer_lyt));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, MainActivity.this, Constant.OFFER_URL, params, false);
    }



    private void GetCategoryById() {
     //   progressBar.setVisibility(View.VISIBLE);
        Map<String, String> params = new HashMap<>();
        params.put(Constant.CATEGORY_ID, "1");

        System.out.println("======params" + params.toString());
        categoryArrayList = new ArrayList<>();
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                progressBar.setVisibility(View.GONE);
                System.out.println("======sub cate " + response);
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                            if(jsonArray.length() < 8 ){
                                subcategory_listSize = jsonArray.length();
                            }
                            for (int i = 0; i < subcategory_listSize; i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                categoryArrayList.add(new Category(jsonObject.getString(Constant.ID),
                                        jsonObject.getString(Constant.NAME),
                                        jsonObject.getString(Constant.SUBTITLE),
                                        jsonObject.getString(Constant.IMAGE)));
                            }
                            categoryArrayList.add(new Category("-1",
                                    "View All",
                                    "",
                                    ""));
                            categoryRecyclerView.setAdapter(new CategoryAdapter(MainActivity.this, categoryArrayList, R.layout.lyt_category_main, "sub_cate"));

                        } else {
                            lytCategory.setVisibility(View.GONE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, MainActivity.this, Constant.SubcategoryUrl, params, true);
    }


    private void GetCategory() {

        Map<String, String> params = new HashMap<String, String>();
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                System.out.println("======cate " + response);
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        categoryArrayList = new ArrayList<>();
                        categoryArrayList.clear();
                        if (!object.getBoolean(Constant.ERROR)) {
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                categoryArrayList.add(new Category(jsonObject.getString(Constant.ID),
                                        jsonObject.getString(Constant.NAME),
                                        jsonObject.getString(Constant.SUBTITLE),
                                        jsonObject.getString(Constant.IMAGE)));

                            }
                            categoryRecyclerView.setAdapter(new CategoryAdapter(MainActivity.this, categoryArrayList, R.layout.lyt_category, "cate"));

                        } else {
                            lytCategory.setVisibility(View.GONE);
                        }
                        progressBar.setVisibility(View.GONE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, MainActivity.this, Constant.CategoryUrl, params, false);
    }

    public void SectionProductRequest() {  //json request for product search

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_ALL_SECTIONS, "1");
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {

                if (result) {
                    try {
                        // System.out.println("====res section " + response);
                        JSONObject object1 = new JSONObject(response);
                        Log.e("section response...",object1.toString());
                        if (!object1.getBoolean(Constant.ERROR)) {
                            sectionList = new ArrayList<>();
                            JSONArray jsonArray = object1.getJSONArray(Constant.SECTIONS);

                            for (int j = 0; j < jsonArray.length(); j++) {
                                Category section = new Category();
                                JSONObject jsonObject = jsonArray.getJSONObject(j);
                                section.setName(jsonObject.getString(Constant.TITLE));
                                Log.e("style...",jsonObject.getString(Constant.TITLE));
                                section.setStyle(jsonObject.getString(Constant.SECTION_STYLE));
                                section.setSubtitle(jsonObject.getString(Constant.SHORT_DESC));
                                JSONArray productArray = jsonObject.getJSONArray(Constant.PRODUCTS);
                                section.setProductList(ApiConfig.GetProductList(productArray));
                                sectionList.add(section);
                            }
                            sectionView.setVisibility(View.VISIBLE);
                            SectionAdapter sectionAdapter = new SectionAdapter(MainActivity.this, sectionList);
                            sectionView.setAdapter(sectionAdapter);

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, MainActivity.this, Constant.FeaturedProductUrl, params, false);
    }


    private void GetSlider() {

        Map<String, String> params = new HashMap<>();
        params.put(Constant.GET_SLIDER_IMAGE, Constant.GetVal);
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {

                    sliderArrayList = new ArrayList<>();
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            JSONArray jsonArray = object.getJSONArray(Constant.DATA);
                            size = jsonArray.length();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                sliderArrayList.add(new Slider(jsonObject.getString(Constant.TYPE), jsonObject.getString(Constant.TYPE_ID), jsonObject.getString(Constant.NAME), jsonObject.getString(Constant.IMAGE)));
                            }
                            mPager.setAdapter(new SliderAdapter(sliderArrayList, MainActivity.this, R.layout.lyt_slider, "home"));
                            ApiConfig.addMarkers(0, sliderArrayList, mMarkersLayout, MainActivity.this);
                            handler = new Handler();
                            Update = new Runnable() {
                                public void run() {
                                    if (currentPage == size) {
                                        currentPage = 0;
                                    }
                                    try {
                                        mPager.setCurrentItem(currentPage++, true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            swipeTimer = new Timer();
                            swipeTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    handler.post(Update);
                                }
                            }, 5000, 5000);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, MainActivity.this, Constant.SliderUrl, params, false);

    }


    @Override
    public void onResume() {
        super.onResume();
        invalidateOptionsMenu();

    }


    public void OnClickBtn(View view) {
        int id = view.getId();

        if (id == R.id.lythome) {
            finish();
            startActivity(new Intent(MainActivity.this, MainActivity.class));

        } else if (id == R.id.lytcategory) {
            OnViewAllClick(view);
        } else if (id == R.id.lytfav) {

            startActivity(new Intent(MainActivity.this, FavouriteActivity.class));
        } else if (id == R.id.layoutSearch) {
            startActivity(new Intent(MainActivity.this, SearchActivity.class).putExtra("from", Constant.FROMSEARCH));
        } else if (id == R.id.lytcart) {
            OpenCart();
        }
    }


    public void OnViewAllClick(View view) {
        startActivity(new Intent(MainActivity.this, CategoryActivity.class));
    }


    public static void UpdateToken(final String token, Activity activity) {
        Map<String, String> params = new HashMap<>();
        params.put(Constant.TYPE, Constant.REGISTER_DEVICE);
        params.put(Constant.TOKEN, token);
        params.put(Constant.USER_ID, session.getData(Session.KEY_ID));
        ApiConfig.RequestToVolley(new VolleyCallback() {
            @Override
            public void onSuccess(boolean result, String response) {
                if (result) {
                    try {
                        JSONObject object = new JSONObject(response);
                        if (!object.getBoolean(Constant.ERROR)) {
                            session.setData(Session.KEY_FCM_ID, token);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, activity, Constant.RegisterUrl, params, false);


    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(navigationView))
            drawer.closeDrawers();
        else
            doubleBack();


    }

    public void doubleBack() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.exit_msg), Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_search).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(false);
        menu.findItem(R.id.menu_cart).setIcon(ApiConfig.buildCounterDrawable(databaseHelper.getTotalItemOfCart(), R.drawable.ic_cart, MainActivity.this));

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_cart:
                OpenCart();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void OpenCart() {
       /* databaseHelper.getCartList();
        databaseHelper.getTotalItemOfCart();*/
        startActivity(new Intent(MainActivity.this, CartActivity.class));

    }


}
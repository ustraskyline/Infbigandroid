package rednum.com.infbigand;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import rednum.com.infbigand.Net.NetProcess;
import rednum.com.infbigand.Security.AndroidDes3Util;
import rednum.com.infbigand.System.StatusBarUtil;
import rednum.com.infbigand.UI.FlowLayout;

public class MainActivity extends Activity implements View.OnClickListener {
    private ArrayList<String> hot_search_company = new ArrayList<>();
    private ArrayList<String> hot_search_project = new ArrayList<>();

    private String[] textBackgrounds = {
            "#649F79EE", "#64CD5C5C", "#64CDAD00", "#648B7D6B", "#644682B4", "#64668B8B", "#64548B54", "#64104E8B", "#641E90FF", "#64A020F0", "#64B03060",
            "#646B8E23", "#640000EE", "#64DC143C", "#649B30FF", "#6400BFFF", "#649400D3", "#64EE30A7", "#64CDCD00", "#64191970", "#64787878"
    };

    private FlowLayout hotCompany;
    private FlowLayout hotProject;

    private TextView searchCompany;
    private TextView searchProject;

    private EditText editCompany;
    private EditText editProject;

    private ImageView guidePage;
    private LinearLayout mainContent;
    private Animation anim1;
    private Animation anim2;

    private BroadcastReceiver networkChange; // 网络类型变化时的广播接收器
    private IntentFilter networkChangeFilter;

    private Handler handler;
    private LinkedList<TextView> companyTextViews;

    private RadioGroup authenType;
    private int index = 0; //默认情况下是政府认证，因此索引为0

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }
        setContentView(R.layout.activity_main);

//        Random random = new Random();
//        mColor = 0xff000000 | random.nextInt(0xffffff);
        StatusBarUtil.setColor(MainActivity.this, 0x00E8E8E8, 60);

        guidePage = findViewById(R.id.guide_page);
        mainContent = findViewById(R.id.main_content);

        anim1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.mainactivity_scale_in);
        anim1.setFillAfter(true);
        anim2 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.mainactivity_content_fade_in);
        anim1.setFillAfter(true);

        guidePage.startAnimation(anim1);
        mainContent.startAnimation(anim2);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0x123);
            }
        }, 4000);

        hotCompany = findViewById(R.id.hot_company_search);
        hotProject = findViewById(R.id.hot_project_search);
        searchCompany = findViewById(R.id.search_company);
        searchProject = findViewById(R.id.search_project);
        editCompany = findViewById(R.id.company_name);
        editProject = findViewById(R.id.project_name);

        authenType = findViewById(R.id.authentication_type);

//        hot_search_company.add("中铁十局集团有限公司");
//        hot_search_company.add("中交一公局桥隧工程有限公司");
//        hot_search_company.add("道隧集团工程有限公司");
//
//        hot_search_project.add("雅安至康定");
//        hot_search_project.add("汶川至马尔康");
//        hot_search_project.add("河池至百色公路");
//        hot_search_project.add("阳朔至鹿寨公路");


        companyTextViews = new LinkedList<>();

        searchCompany.setOnClickListener(this);
        searchProject.setOnClickListener(this);

        setFlowLayoutProperty(hotCompany, hot_search_company);
        setFlowLayoutProperty(hotProject, hot_search_project);

        authenType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.government_authen:
                        index = 0;

                        break;

                    case R.id.enterprise_authen:
                        index = 1;

                        break;
                }
            }
        });


        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 101:

                        removeFlowLayoutSubview(hotCompany);
                        setFlowLayoutProperty(hotCompany, hot_search_company);

                        break;

                    case 102:
                        removeFlowLayoutSubview(hotProject);
                        setFlowLayoutProperty(hotProject, hot_search_project);

                        break;

                    case 0x901:
                        Toast.makeText(getApplicationContext(), "无网络连接", Toast.LENGTH_SHORT).show();
                        break;

                    case 0x123:
                        guidePage.setVisibility(View.GONE);

                        break;
                }
            }
        };

        networkChangeFilter = new IntentFilter();
        networkChangeFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        networkChange = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (NetProcess.isNetworkAvailable(MainActivity.this) && NetProcess.isUseMobile(MainActivity.this)) {
                    Toast.makeText(getApplicationContext(), "当前使用手机流量，请注意流量使用情况", Toast.LENGTH_SHORT).show();
                }
            }
        };

        registerReceiver(networkChange, networkChangeFilter);
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private void setFlowLayoutProperty(final FlowLayout flowLayout, ArrayList<String> data) {
        TextView tv;
        companyTextViews.clear();
        for (int i = 0; i < data.size(); i++) {
            int ranHeight = dip2px(this, 30);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ranHeight);
            lp.setMargins(dip2px(this, 3), 0, dip2px(this, 3), 0);
            tv = new TextView(this);
            tv.setPadding(dip2px(this, 10), 0, dip2px(this, 10), 0);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            tv.setText(data.get(i));//设置显示的数据
            tv.setGravity(Gravity.CENTER_VERTICAL);
            tv.setLines(1);
            tv.setBackgroundResource(R.drawable.bg_tag);
            String rgb = textBackgrounds[(int) (Math.random() * textBackgrounds.length)];
            tv.setBackgroundResource(R.drawable.bg_tag);
            GradientDrawable myGrad = (GradientDrawable) tv.getBackground();
            myGrad.setColor(Color.parseColor(rgb));
            tv.setBackgroundDrawable(myGrad);
            flowLayout.addView(tv, lp);
            flowLayout.relayoutToCompress();

            final String content = tv.getText().toString();
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), CompanyInfoShowActivity.class);
                    try {
                        long timestamp = NetProcess.getCurrentTimeStamp();
                        if (flowLayout == hotCompany) {
                            intent.putExtra("url", "http://221.237.189.104:8080/titanweb/SolrTitanAction?method=name&company=" + URLEncoder.encode(content, "utf-8") + "&index=" + index + "&sign=" + AndroidDes3Util.encode(String.valueOf(timestamp)));
                        } else if (flowLayout == hotProject) {
                            intent.putExtra("url", "http://221.237.189.104:8080/titanweb/SolrTitanAction?method=load&project=" + URLEncoder.encode(content, "utf-8") + "&index=" + index + "&sign=" + AndroidDes3Util.encode(String.valueOf(timestamp)));
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    startActivity(intent);
                    overridePendingTransition(R.anim.new_enter_from_alpha, R.anim.old_exit_to_alpha);
                }
            });

            companyTextViews.add(tv);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_company:
                new Thread() {
                    @Override
                    public void run() {
                        String companyName = editCompany.getText().toString();
                        if (companyName != null && !"".equals(companyName)) {
                            if (NetProcess.isNetworkAvailable(MainActivity.this)) {
                                hot_search_company.clear();
                                hot_search_company = NetProcess.searchSpecifiedCompany(companyName, index);

                                handler.sendEmptyMessage(101);
                            } else {
                                handler.sendEmptyMessage(0x901);
                            }
                        }
                    }
                }.start();


                break;

            case R.id.search_project:
                new Thread() {
                    @Override
                    public void run() {
                        String projectName = editProject.getText().toString();
                        if (projectName != null && !"".equals(projectName)) {
                            if (NetProcess.isNetworkAvailable(MainActivity.this)) {
                                hot_search_project.clear();
                                hot_search_project = NetProcess.searchSpecifiedProject(projectName, index);

                                handler.sendEmptyMessage(102);
                            } else {
                                handler.sendEmptyMessage(0x901);
                            }
                        }
                    }
                }.start();

                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChange);  // 注销接收网络状况变化的广播接收器
    }


    private void removeFlowLayoutSubview(FlowLayout flowLayout) {
        for (int i = 0; i < companyTextViews.size(); i++) {
            flowLayout.removeView(companyTextViews.get(i));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
            View view = inflater.inflate(R.layout.exit_app_layout, null);
            TextView cancel = view.findViewById(R.id.cancel);

            builder.setView(view);
            final AlertDialog dialog = builder.create();

            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            TextView confirm = view.findViewById(R.id.confirm);
            confirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            dialog.show();

        }
        return true;
    }
}

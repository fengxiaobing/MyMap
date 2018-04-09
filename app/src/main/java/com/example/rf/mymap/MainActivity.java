package com.example.rf.mymap;

import android.location.Location;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

public class MainActivity extends AppCompatActivity implements
        AMapLocationListener, Runnable {
    MapView mapView;//找到地图控件
    private LocationManagerProxy aMapLocManager = null;
    private TextView myLocation;
    private AMapLocation aMapLocation;// 用于判断定位超时
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
//在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        AMap aMap = mapView.getMap();//初始化地图控制器对象

        UiSettings mUiSettings = aMap.getUiSettings();
//        //控制定位到当前按钮的显示和隐藏
//        mUiSettings.setMyLocationButtonEnabled(true);
        //控制缩放控件的显示和隐藏。
        mUiSettings.setZoomControlsEnabled(true);
        //控制指南针的显示和隐藏。
        mUiSettings.setCompassEnabled(true);
        //显示比例尺控件:例如1:10Km
        mUiSettings.setScaleControlsEnabled(true);

        init();
        //地图中心点
        LatLng marker1 = new LatLng(37.620802767, 114.9193);
        //设置中心点和缩放比例
        aMap.moveCamera(CameraUpdateFactory.changeLatLng(marker1));

        //标记
        LatLng latLng = new LatLng(37.620802767,114.9193);
        Marker marker = aMap.addMarker(new MarkerOptions()
                .draggable(true)//可拖拽
                .setFlat(true)//将Marker设置为贴地显示，可以双指下拉看效果
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher_background))//Marker图标
                .title("房源位置")//标题
                .position(latLng));

        //显示InfoWindow
        if(!marker.isInfoWindowShown()) {
            marker.showInfoWindow();
        }


        //Marker点击事件
        aMap.setOnMarkerClickListener(new AMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker arg0) {
                Toast.makeText(MainActivity.this, "点击", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        
        
        
    }

    private void init() {

        aMapLocManager = LocationManagerProxy.getInstance(this);
        aMapLocManager.setGpsEnable(false);
        /*
		 * mAMapLocManager.setGpsEnable(false);//
		 * 1.0.2版本新增方法，设置true表示混合定位中包含gps定位，false表示纯网络定位，默认是true Location
		 * API定位采用GPS和网络混合定位方式
		 * ，第一个参数是定位provider，第二个参数时间最短是2000毫秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
		 */
        aMapLocManager.requestLocationUpdates(
                LocationProviderProxy.AMapNetwork, 2000, 10, this);
        handler.postDelayed(this, 12000);// 设置超过12秒还没有定位到就停止定位
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();

    }


    @Override
    public void onLocationChanged(AMapLocation location) {
        if (location != null) {
            this.aMapLocation = location;// 判断超时机制
            Double geoLat = location.getLatitude();
            Double geoLng = location.getLongitude();
            String cityCode = "";
            String desc = "";
            Bundle locBundle = location.getExtras();
            if (locBundle != null) {
                cityCode = locBundle.getString("citycode");
                desc = locBundle.getString("desc");
            }
            String str = ("定位成功:(" + geoLng + "," + geoLat + ")"
                    + "\n精    度    :" + location.getAccuracy() + "米"
                    + "\n定位方式:" + location.getProvider() + "\n定位时间:"
                    + AMapUtil.convertToTime(location.getTime()) + "\n城市编码:"
                    + cityCode + "\n位置描述:" + desc + "\n省:"
                    + location.getProvince() + "\n市:" + location.getCity()
                    + "\n区(县):" + location.getDistrict() + "\n区域编码:" + location
                    .getAdCode());
            Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void run() {
        if (aMapLocation == null) {
            Toast.makeText(this, "12秒内还没有定位成功，停止定位", Toast.LENGTH_SHORT).show();
            stopLocation();// 销毁掉定位
        }
    }

    /**
     * 销毁定位
     */
    private void stopLocation() {
        if (aMapLocManager != null) {
            aMapLocManager.removeUpdates(this);
            aMapLocManager.destory();
        }
        aMapLocManager = null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}


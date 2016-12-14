package redlaboratory.littlelaboratory;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;

import redlaboratory.littlelaboratory.db.Experiment;
import redlaboratory.littlelaboratory.db.LittleLaboratoryDbHelper;
import redlaboratory.littlelaboratory.db.Measurement;
import redlaboratory.littlelaboratory.db.Series;

public class MainActivity extends AppCompatActivity /*implements NavigationView.OnNavigationItemSelectedListener*/ {

    public static class ExperimentListViewAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<Experiment> data;
        private int layout;

        public ExperimentListViewAdapter(Context context, int layout, ArrayList<Experiment> data) {
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.data = data;
            this.layout = layout;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Experiment getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(layout, parent, false);
            }

            Experiment projectListItem = data.get(position);

            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView description = (TextView) convertView.findViewById(R.id.description);
            TextView date = (TextView) convertView.findViewById(R.id.date);

            Calendar cal = projectListItem.getAddedDate();
            String dateStr = cal.get(Calendar.YEAR) + "년 " + (cal.get(Calendar.MONTH) + 1) + "월 " + cal.get(Calendar.DATE) + "일 " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND);

            title.setText(projectListItem.getTitle());
            description.setText(projectListItem.getDescription());
            date.setText(dateStr);

            return convertView;
        }

    }

    private static final int REQUEST_READ = 0;
    private static final int REQUEST_WRITE = 1;
    private static final int REQUEST_CAMERA = 2;

//    private ActionBarDrawerToggle toggle;

    private LittleLaboratoryDbHelper littleLaboratoryDbHelper;

    private ArrayList<Experiment> experiments;

    private void update() {
        experiments = littleLaboratoryDbHelper.selectExperiments();
        for (Experiment experiment : experiments) Log.i("LittleLaboratory", "Load experiment: " + experiment.toString());
        for (Measurement measurement : littleLaboratoryDbHelper.selectMeasurements()) Log.i("LittleLaboratory", "Load measurement: " + measurement.toString());
        for (Series series : littleLaboratoryDbHelper.selectSeries()) Log.i("LittleLaboratory", "Load Series: " + series.toString());

        ExperimentListViewAdapter adapter = new ExperimentListViewAdapter(this, R.layout.item_experiment, experiments);

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Experiment experiment = (Experiment) adapterView.getItemAtPosition(position);

                Intent intent = new Intent(MainActivity.this, ExperimentActivity.class);
                intent.putExtra("experimentId", experiment.getId());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton newExperimentFab = (FloatingActionButton) findViewById(R.id.fab);
        newExperimentFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewExperimentActivity.class);
                startActivityForResult(intent, 0);
            }
        });

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
//
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//                invalidateOptionsMenu();
//            }
//
//            @Override
//            public void onDrawerClosed(View view) {
//                super.onDrawerClosed(view);
//                invalidateOptionsMenu();
//            }
//
//        };
//        toggle.setDrawerIndicatorEnabled(true);
//        drawer.setDrawerListener(toggle);
//        toggle.syncState();
//
//        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setHomeButtonEnabled(true);

        littleLaboratoryDbHelper = new LittleLaboratoryDbHelper(getApplicationContext());
    }

    @Override
    protected void onResume() {
        super.onResume();

        int permissionReadCheck = PermissionChecker.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteCheck = PermissionChecker.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionCamera = PermissionChecker.checkSelfPermission(this, Manifest.permission.CAMERA);

        if (permissionReadCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ);
        } else {
            update();
        }
        if (permissionWriteCheck == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE);
        }
        if (permissionCamera == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        Log.i("LittleLaboratory", "MainActivity onResume");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                update();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Experiment experiment = (Experiment) data.getSerializableExtra("experiment");

            long id = littleLaboratoryDbHelper.insertExperiment(experiment.getTitle(), experiment.getDescription(), experiment.getAddedDate(), experiment.getMeasurements());
            experiment.setId(id);
            experiments.add(experiment);

            Snackbar.make(this.findViewById(R.id.app_bar_layout), experiment.getTitle() + " 추가됨", Snackbar.LENGTH_LONG).show();
        }
    }

//    @Override
//    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//
//        toggle.syncState();
//    }

//    @Override
//    public void onBackPressed() {
//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        if (drawer.isDrawerOpen(GravityCompat.START)) {
//            drawer.closeDrawer(GravityCompat.START);
//        } else {
//            super.onBackPressed();
//        }
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

//    @Override
//    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
//        int id = item.getItemId();

//        if (id == R.id.nav_camera) {
//            // Handle the camera action
//        } else if (id == R.id.nav_gallery) {
//
//        } else if (id == R.id.nav_slideshow) {
//
//        } else if (id == R.id.nav_manage) {
//
//        } else if (id == R.id.nav_share) {
//
//        } else if (id == R.id.nav_send) {
//
//        }

//        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);
//        return true;
//    }

}

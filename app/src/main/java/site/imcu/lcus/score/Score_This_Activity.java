package site.imcu.lcus.score;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.bilibili.magicasakura.widgets.TintProgressDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import site.imcu.lcus.R;
import site.imcu.lcus.utils.LoginUtils;

public class Score_This_Activity extends AppCompatActivity {

    private ScoreAdapter mScoreAdapter;
    private static final String TAG = "Score_This_Activity";
    private String markUrl= "http://210.44.112.125/bxqcjcxAction.do";
    String session;
    ProgressDialog progressDialog;
    List<Score> scoreList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score_m);
        setToolBar();
        Intent intent = getIntent();
        session= intent.getStringExtra("session");
        Log.d(TAG, "onCreate: "+session);
        getMark();
        show();
    }


    private  void setToolBar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void getMark() {


        Observable<String> observable = Observable.create(new ObservableOnSubscribe<String>() {

            @Override

            public void subscribe(@NonNull ObservableEmitter<String> e) throws Exception {

                e.onNext(LoginUtils.getData(session,markUrl));//获取本学期成绩

            }

        });

        Consumer<String> consumer= new Consumer<String>() {

            @Override

            public void accept(String html) throws Exception {
                scoreDao(html);
            }

        };

        observable.subscribeOn(Schedulers.io())

                .observeOn(AndroidSchedulers.mainThread())

                .subscribe(consumer);

    }

    private void scoreDao(String html) {
        Document document = Jsoup.parse(html);
        Element table = document.getElementById("user");
        Elements trs = table.select("tr");
        for(int i=1;i<trs.size();i++){
            Elements tds = trs.get(i).select("td");
            Score score = new Score();
            score.setCourseName(tds.get(2).text());
            score.setCredit(tds.get(4).text());
            score.setCourseAttr(tds.get(5).text());
            score.setMark(tds.get(9).text());
            score.setPosition(tds.get(10).text());
            score.setDetail("http://210.44.112.125"+getSrc(tds.get(12)));
            scoreList.add(score);
        }
        setRecycle();
        progressDialog.dismiss();
    }

    private String getSrc(Element element){
        Elements src = element.select("img");
        String onclick = src.attr("onclick");
        String []a=onclick.split("[']");
        return a[1];
    }

    private void setRecycle(){

        RecyclerView recyclerView=(RecyclerView)findViewById(R.id.score_recycle);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);

        mScoreAdapter = new ScoreAdapter(scoreList);

        recyclerView.setAdapter(mScoreAdapter);
        setListener();

    }
    private void show(){
        progressDialog = new ProgressDialog(Score_This_Activity.this);
        progressDialog.setTitle("本学期成绩");
        progressDialog.setMessage("加载中");
        progressDialog.setProgressStyle(TintProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void setListener(){
        mScoreAdapter.setOnItemClickListener(new ScoreAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(Score_This_Activity.this, Score_Detail_Activity.class);
                intent.putExtra("session",session);
                intent.putExtra("url",scoreList.get(position).getDetail());
                startActivity(intent);
            }
        });
    }

}

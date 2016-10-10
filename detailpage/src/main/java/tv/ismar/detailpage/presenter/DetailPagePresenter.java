package tv.ismar.detailpage.presenter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;

import cn.ismartv.injectdb.library.query.Select;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.database.BookmarkTable;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.network.exception.OnlyWifiException;
import tv.ismar.app.util.Utils;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.R;
import tv.ismar.detailpage.view.DetailPageActivity;

/**
 * Created by huibin on 8/19/16.
 */
public class DetailPagePresenter implements DetailPageContract.Presenter {
    private static final String TAG = DetailPagePresenter.class.getSimpleName();
    private final DetailPageContract.View mDetailView;
    private SkyService mSkyService;
    private Subscription apiItemSubsc;
    private Subscription bookmarksSubsc;
    private Subscription itemRelateSubsc;
    private Subscription removeBookmarksSubsc;
    private Subscription playCheckSubsc;

    private ItemEntity mItemEntity = new ItemEntity();
    private String mContentModel;
    private ItemEntity[] relatedItemList;
    private Context mContext;

    private DetailPageActivity detailPageActivity;

    public void setActivity(DetailPageActivity activity){
        detailPageActivity = activity;
    }

    public DetailPagePresenter(Context context, DetailPageContract.View detailView, String contentModel) {
        mContext = context;
        mContentModel = contentModel;
        mDetailView = detailView;
        mDetailView.setPresenter(this);
    }

    public String getContentModel() {
        return mContentModel;
    }

    @Override
    public void start() {
        mSkyService = ((BaseActivity) mDetailView.getContext()).mSkyService;
    }

    @Override
    public void fetchItem(String pk) {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }

        apiItemSubsc = mSkyService.apiItem(pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                        if (e.getClass() == OnlyWifiException.class) {
                            mDetailView.onHttpInterceptor(e);
                        } else {
                            mDetailView.onHttpFailure(e);
                        }
                    }

                    @Override
                    public void onNext(ItemEntity itemEntity) {
                        mItemEntity = itemEntity;
                        mDetailView.loadItem(itemEntity);
                    }
                });
    }


    @Override
    public void createBookmarks(String pk) {
        if (bookmarksSubsc != null && !bookmarksSubsc.isUnsubscribed()) {
            bookmarksSubsc.unsubscribe();
        }
        bookmarksSubsc = mSkyService.apiBookmarksCreate(pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mDetailView.notifyBookmark(true, false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });
    }

    @Override
    public void removeBookmarks(String pk) {
        if (removeBookmarksSubsc != null && !removeBookmarksSubsc.isUnsubscribed()) {
            removeBookmarksSubsc.unsubscribe();
        }
        removeBookmarksSubsc = mSkyService.apiBookmarksRemove(pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mDetailView.notifyBookmark(false, false);
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {

                    }
                });
    }

    @Override
    public void requestPlayCheck(String itemPk) {
        if (playCheckSubsc != null && !playCheckSubsc.isUnsubscribed()) {
            playCheckSubsc.unsubscribe();
        }

        playCheckSubsc = mSkyService.apiPlayCheck(itemPk, null, null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            PlayCheckEntity playCheckEntity = calculateRemainDay(responseBody.string());
                            mDetailView.notifyPlayCheck(playCheckEntity);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }


    private PlayCheckEntity calculateRemainDay(String info) {
        PlayCheckEntity playCheckEntity;
        switch (info) {
            case "0":
                playCheckEntity = new PlayCheckEntity();
                playCheckEntity.setRemainDay(0);
                break;
            default:
                playCheckEntity = new Gson().fromJson(info, PlayCheckEntity.class);
                int remainDay;
                try {
                    remainDay = Utils.daysBetween(Utils.getTime(), playCheckEntity.getExpiry_date()) + 1;
                } catch (ParseException e) {
                    remainDay = 0;
                }
                playCheckEntity.setRemainDay(remainDay);
                break;
        }
        return playCheckEntity;
    }

    @Override
    public void fetchItemRelate(String pk) {
        if (itemRelateSubsc != null && !itemRelateSubsc.isUnsubscribed()) {
            itemRelateSubsc.unsubscribe();
        }
        itemRelateSubsc = mSkyService.apiTvRelate(pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ItemEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(ItemEntity[] itemEntities) {
                        relatedItemList = itemEntities;
                        mDetailView.loadItemRelate(itemEntities);
                    }
                });
    }


    @Override
    public void stop() {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }
        if (bookmarksSubsc != null && !bookmarksSubsc.isUnsubscribed()) {
            bookmarksSubsc.unsubscribe();
        }
        if (itemRelateSubsc != null && !itemRelateSubsc.isUnsubscribed()) {
            itemRelateSubsc.unsubscribe();
        }
        if (removeBookmarksSubsc != null && !removeBookmarksSubsc.isUnsubscribed()) {
            removeBookmarksSubsc.unsubscribe();
        }
        if (playCheckSubsc != null && !playCheckSubsc.isUnsubscribed()) {
            playCheckSubsc.unsubscribe();
        }
    }

    @Override
    public void handleBookmark() {
        addFavorite();
    }

    @Override
    public void handlePlay() {
        if(detailPageActivity != null){
            detailPageActivity.goPlayer();
        }

    }


    @Override
    public void handlePurchase() {
        String pk = String.valueOf(mItemEntity.getPk());
        String jumpTo = String.valueOf(mItemEntity.getExpense().getJump_to());
        String cpid = String.valueOf(mItemEntity.getExpense().getCpid());
        new PageIntent().toPayment(mDetailView.getContext(), pk, jumpTo, cpid, "item");
    }


    @Override
    public void handleMoreRelate() {
        Intent intent = new Intent();
        if (relatedItemList != null && relatedItemList.length > 0) {
            intent.putExtra("related_item_json", new Gson().toJson(relatedItemList));
        }
        intent.putExtra("item_json", new Gson().toJson(mItemEntity));
        intent.setAction("tv.ismar.daisy.relateditem");
        mDetailView.getContext().startActivity(intent);
    }

    @Override
    public void handleEpisode() {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.dramalist");
        intent.putExtra("item_json", new Gson().toJson(mItemEntity));
        mDetailView.getContext().startActivity(intent);
    }

    private void addFavorite() {
        VodApplication vodApplication = (VodApplication) mContext.getApplicationContext();
        FavoriteManager favoriteManager = vodApplication.getModuleFavoriteManager();
        String url = mItemEntity.getItem_url();
        if (TextUtils.isEmpty(url)){
            url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItemEntity.getPk() + "/";
        }
        if (isFavorite()) {
            String isnet = "";
            if (isLogin()) {
                isnet = "yes";
                deleteFavoriteByNet();
            } else {
                isnet = "no";
            }
            favoriteManager.deleteFavoriteByUrl(url, isnet);
            mDetailView.notifyBookmark(false, true);
        } else {
            Favorite favorite = new Favorite();
            favorite.title = mItemEntity.getTitle();
            favorite.adlet_url = mItemEntity.getAdletUrl();
            favorite.content_model = mItemEntity.getContentModel();
            favorite.url = url;
            favorite.quality = mItemEntity.getQuality();
            favorite.is_complex = mItemEntity.getIsComplex();
            if (mItemEntity.getExpense() != null) {
                favorite.cpid = mItemEntity.getExpense().getCpid();
                favorite.cpname = mItemEntity.getExpense().getCpname();
                favorite.cptitle = mItemEntity.getExpense().getCptitle();
                favorite.paytype = mItemEntity.getExpense().pay_type;
            }

            if (isLogin()) {
                favorite.isnet = "yes";
                createBookmarks(String.valueOf(mItemEntity.getPk()));
            } else {
                favorite.isnet = "no";
            }

            favoriteManager.addFavorite(favorite, favorite.isnet);
            mDetailView.notifyBookmark(true, true);
        }
    }


    public boolean isFavorite() {
        VodApplication vodApplication = (VodApplication) mContext.getApplicationContext();
        FavoriteManager favoriteManager = vodApplication.getModuleFavoriteManager();
        String url = mItemEntity.getItem_url();
        if (TextUtils.isEmpty(url)){
            url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItemEntity.getPk() + "/";
        }
        Favorite favorite;
        if (isLogin()) {
            favorite = favoriteManager.getFavoriteByUrl(url, "yes");
        } else {
            favorite = favoriteManager.getFavoriteByUrl(url, "no");
        }

        if (favorite != null) {
            return true;
        } else {
            return false;
        }
    }

    private boolean isLogin() {
        if (TextUtils.isEmpty(IsmartvActivator.getInstance().getUsername())) {
            return false;
        } else {
            return true;
        }
    }

    private void deleteFavoriteByNet() {
        removeBookmarks(String.valueOf(mItemEntity.getPk()));
    }

}

package tv.ismar.detailpage.presenter;

import android.content.Intent;
import android.text.TextUtils;

import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import cn.ismartv.truetime.TrueTime;
import okhttp3.ResponseBody;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.AppConstant;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.VodApplication;
import tv.ismar.app.core.DaisyUtils;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.db.FavoriteManager;
import tv.ismar.app.entity.Favorite;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.ItemEntity;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.network.entity.QiyiCheckEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.detailpage.DetailPageContract;
import tv.ismar.detailpage.view.DetailPageActivity;
import tv.ismar.library.exception.ExceptionUtils;
import tv.ismar.statistics.PurchaseStatistics;

import static tv.ismar.app.core.PageIntentInterface.EXTRA_ITEM_JSON;
import static tv.ismar.app.core.PageIntentInterface.EXTRA_SOURCE;
import static tv.ismar.app.core.PageIntentInterface.FromPage.unknown;
import static tv.ismar.app.core.PageIntentInterface.PaymentInfo;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.item;

/**
 * Created by huibin on 8/19/16.
 */
public class DetailPagePresenter implements DetailPageContract.Presenter {
    private static final String TAG = DetailPagePresenter.class.getSimpleName();
    private final DetailPageContract.View mDetailView;
    private SkyService mSkyService;
    private SkyService mCarnationService;
    private Subscription apiItemSubsc;
    private Subscription bookmarksSubsc;
    private Subscription itemRelateSubsc;
    private Subscription removeBookmarksSubsc;
    private Subscription playCheckSubsc;
    private Subscription subscribeStatusSubsc;
    private Subscription subscribeSubsc;

    private ItemEntity mItemEntity = new ItemEntity();
    private String mContentModel;
    private ItemEntity[] relatedItemList;

    private DetailPageActivity detailPageActivity;
    private boolean isSubscribed = false;
    public boolean isFavorite = false;



    public DetailPagePresenter(DetailPageActivity activity, DetailPageContract.View detailView, String contentModel) {
        detailPageActivity = activity;
        mContentModel = contentModel;
        mDetailView = detailView;
        mDetailView.setPresenter(this);
    }

    public String getContentModel() {
        return mContentModel;
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }

    @Override
    public void start() {
        mSkyService = ((BaseActivity) mDetailView.getActivity()).mSkyService;
        mCarnationService = ((BaseActivity) mDetailView.getActivity()).mCarnationService;
    }

    @Override
    public void fetchItem(String pk) {
        if (apiItemSubsc != null && !apiItemSubsc.isUnsubscribed()) {
            apiItemSubsc.unsubscribe();
        }

        apiItemSubsc = mSkyService.apiItem(pk)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(detailPageActivity.new BaseObserver<ItemEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mDetailView.onError();
                        super.onError(e);

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
                .subscribe(detailPageActivity.new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mDetailView.notifyBookmark(true, false);
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
                .subscribe(detailPageActivity.new BaseObserver<ResponseBody>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        try {
                            PlayCheckEntity playCheckEntity = calculateRemainDay(responseBody.string());
                            if (TextUtils.isEmpty(playCheckEntity.getIqiyi_code())){
                                mDetailView.notifyPlayCheck(playCheckEntity);
                            }else {
                                apiQiyiCheck(playCheckEntity.getIqiyi_code(), playCheckEntity);
                            }
                        } catch (IOException e) {
                            ExceptionUtils.sendProgramError(e);
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void apiQiyiCheck(String qiyiCode, final PlayCheckEntity playCheckEntity){
        mCarnationService.apiQiyiCheck(qiyiCode)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(detailPageActivity.new BaseObserver<QiyiCheckEntity>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onNext(QiyiCheckEntity entity) {
                        if (entity.getCode() == 1){
                            playCheckEntity.setRemainDay(0);
                            mDetailView.notifyPlayCheck(playCheckEntity);
                        }else {
                            mDetailView.notifyPlayCheck(playCheckEntity);
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
                playCheckEntity = new GsonBuilder().create().fromJson(info, PlayCheckEntity.class);
                int remainDay;
                try {
                    remainDay = Utils.daysBetween(Utils.getTime(), playCheckEntity.getExpiry_date()) + 1;
                } catch (ParseException e) {
                    ExceptionUtils.sendProgramError(e);
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
                .subscribe(detailPageActivity.new BaseObserver<ItemEntity[]>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mDetailView.onError();
                        super.onError(e);
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
        if (detailPageActivity != null) {
            detailPageActivity.goPlayer();
        }

    }


    @Override
    public void handlePurchase() {
        ((DetailPageActivity) mDetailView.getActivity()).stopPreload();
        int pk = mItemEntity.getPk();
        int jumpTo = mItemEntity.getExpense().getJump_to();
        int cpid = mItemEntity.getExpense().getCpid();
        PaymentInfo paymentInfo = new PaymentInfo(item, pk, jumpTo, cpid);

        String userName = IsmartvActivator.getInstance().getUsername();
        String title = mItemEntity.getTitle();

        String clip = "";
        if (mItemEntity.getClip() != null) {
            clip = String.valueOf(mItemEntity.getClip().getPk());
        }
        new PurchaseStatistics().expenseVideoClick(String.valueOf(pk), userName, title, clip);
        new PageIntent().toPaymentForResult(mDetailView.getActivity(), unknown.name(), paymentInfo);
    }



    @Override
    public void handleMoreRelate() {
        ((DetailPageActivity) mDetailView.getActivity()).stopPreload();
        AppConstant.purchase_entrance_related_item = String.valueOf(mItemEntity.getItemPk());
        AppConstant.purchase_entrance_related_title = mItemEntity.getTitle();
        AppConstant.purchase_entrance_related_channel = AppConstant.purchase_channel;
        AppConstant.purchase_entrance_page="related";
        Intent intent = new Intent();
        if (relatedItemList != null && relatedItemList.length > 0) {
            intent.putExtra("related_item_json", new GsonBuilder().create().toJson(relatedItemList));
        }
        intent.putExtra("item_json", new GsonBuilder().create().toJson(mItemEntity));
        intent.setAction("tv.ismar.daisy.relateditem");
        mDetailView.getActivity().startActivity(intent);
        mDetailView.getActivity().finish();
    }

    @Override
    public void handleEpisode() {
        ((DetailPageActivity) mDetailView.getActivity()).stopPreload();
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.episode");
        intent.putExtra(EXTRA_ITEM_JSON, new GsonBuilder().create().toJson(mItemEntity));
        intent.putExtra(EXTRA_SOURCE, "detail");
        mDetailView.getActivity().startActivity(intent);

    }

    @Override
    public void handleSubscribe() {
        if (subscribeSubsc != null && !subscribeSubsc.isUnsubscribed()) {
            subscribeSubsc.unsubscribe();
        }

        subscribeSubsc = mSkyService.accountsItemSubscribe(mItemEntity.getItemPk(), mItemEntity.getContentModel())
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
                        mDetailView.showSubscribeDialog(responseBody);
//                        fetchSubscribeStatus(mItemEntity.getPk());
                    }
                });
    }

    @Override
    public void bookmarkCheck(int pk) {

        VodApplication vodApplication = (VodApplication) detailPageActivity.getApplicationContext();
        FavoriteManager favoriteManager = vodApplication.getModuleFavoriteManager();
        String url = mItemEntity.getItem_url();
        if (TextUtils.isEmpty(url)) {
            url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItemEntity.getPk() + "/";
        }
        Favorite favorite;
        if (isLogin()) {
            checkFavorite(mItemEntity.getPk());
        } else {
            favorite = favoriteManager.getFavoriteByUrl(url, "no");
            if (favorite != null) {
                isFavorite = true;
                mDetailView.notifyBookmarkCheck();
            } else {
                isFavorite = false;
                mDetailView.notifyBookmarkCheck();
            }
        }
    }

    private void addFavorite() {
        VodApplication vodApplication = (VodApplication) detailPageActivity.getApplicationContext();
        FavoriteManager favoriteManager = vodApplication.getModuleFavoriteManager();
        String url = mItemEntity.getItem_url();
        if (TextUtils.isEmpty(url)) {
            url = IsmartvActivator.getInstance().getApiDomain() + "/api/item/" + mItemEntity.getPk() + "/";
        }
        if (isFavorite) {
            String isnet = "";
            if (isLogin()) {
                isnet = "yes";
                deleteFavoriteByNet();
            } else {
                isnet = "no";
                favoriteManager.deleteFavoriteByUrl(url, isnet);
            }
            isFavorite = false;
            mDetailView.notifyBookmark(false, true);
        } else {
            DateFormat format=new SimpleDateFormat("MM-dd");
            format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            long time= TrueTime.now().getTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            Favorite favorite = new Favorite();
            favorite.time=format.format(calendar.getTime());
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
                ArrayList<Favorite> favorites = DaisyUtils.getFavoriteManager(mDetailView.getActivity()).getAllFavorites("no");
                if (favorites.size() > 49) {
                    favoriteManager.deleteFavoriteByUrl(favorites.get(favorites.size() - 1).url, "no");

                }
                favoriteManager.addFavorite(favorite, favorite.isnet);
            }
            isFavorite = true;
            mDetailView.notifyBookmark(true, true);
        }
    }


    public void checkFavorite(int pk) {
        SkyService skyService=SkyService.ServiceManager.getService();
        skyService.checkBookmark(IsmartvActivator.getInstance().getDeviceToken(),IsmartvActivator.getInstance().getAuthToken(),pk).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(Integer i) {
                        if(i==1){
                            isFavorite =true;
                            mDetailView.notifyBookmarkCheck();
                        }else{
                            isFavorite =false;
                            mDetailView.notifyBookmarkCheck();
                        }
                    }
                });

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

    public void setItemEntity(ItemEntity itemEntity) {
        mItemEntity = itemEntity;
    }


//    @Override
//    public void fetchSubscribeStatus(int pk){
//        if (subscribeStatusSubsc != null && !subscribeStatusSubsc.isUnsubscribed()) {
//            subscribeStatusSubsc.unsubscribe();
//        }
//        subscribeStatusSubsc = mSkyService.accountsItemSubscribeExists(pk)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Observer<AccountsItemSubscribeExistsEntity>() {
//                    @Override
//                    public void onCompleted() {
//
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//
//                    }
//
//                    @Override
//                    public void onNext(AccountsItemSubscribeExistsEntity entity) {
//                        if (!mItemEntity.is_order()){
//                            isSubscribed = false;
//                            mDetailView.notifySubscribeStatus(false);
//                        }else {
//                            if (entity != null && entity.getInfo() != null && entity.getInfo().getStatus() == 1){
//                                isSubscribed = true;
//                                mDetailView.notifySubscribeStatus(true);
//                            }else {
//                                isSubscribed = false;
//                                mDetailView.notifySubscribeStatus(false);
//                            }
//                        }
//                    }
//                });
//    }
}

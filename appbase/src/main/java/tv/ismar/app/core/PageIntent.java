package tv.ismar.app.core;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

/**
 * Created by huibin on 9/13/16.
 */
public class PageIntent implements PageIntentInterface {

    @Override
    public void toDetailPage(final Context context, final String source, final int pk) {

                Intent intent = new Intent();
                intent.setAction("tv.ismar.daisy.detailpage");
                intent.putExtra(EXTRA_PK, pk);
                intent.putExtra(EXTRA_SOURCE, source);
                intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_ITEM);
                if(context instanceof Activity){
                    ((Activity)context).startActivityForResult(intent,1);
                }else {
                    context.startActivity(intent);
                }



    }

    @Override
    public void toDetailPage(final Context context, final String source, final String root, final int pk) {

        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_PK, pk);
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_ITEM);
        intent.putExtra(EXTRA_TO, root);
        if(context instanceof Activity){
            ((Activity)context).startActivityForResult(intent,1);
        }else {
            context.startActivity(intent);
        }



    }

    @Override
    public void toDetailPage(Context context, String source, String json) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_ITEM_JSON, json);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_ITEM);
        context.startActivity(intent);
    }

    @Override
    public void toPackageDetail(final Context context, final String source, final int pk) {

                Intent intent = new Intent();
                intent.setAction("tv.ismar.daisy.detailpage");
                intent.putExtra(EXTRA_PK, pk);
                intent.putExtra(EXTRA_SOURCE, source);
                intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_PKG);
                if(context instanceof Activity){
                    ((Activity)context).startActivityForResult(intent,1);
                }else {
                     context.startActivity(intent);
                 }

    }

    @Override
    public void toPackageDetail(Context context, String source, String json) {

        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.detailpage");
        intent.putExtra(EXTRA_SOURCE, source);
        intent.putExtra(EXTRA_ITEM_JSON, json);
        intent.putExtra(EXTRA_TYPE, DETAIL_TYPE_PKG);
        context.startActivity(intent);
    }


//    @Override
//    public void toPayment(Context context, String fromPage, PaymentInfo paymentInfo) {
//        Intent intent = new Intent();
//        switch (paymentInfo.getJumpTo()) {
//            //直接支付
//            case PAYMENT:
//                intent.setAction("tv.ismar.pay.payment");
//                intent.putExtra(EXTRA_PK, paymentInfo.getPk());
//                intent.putExtra(EXTRA_PRODUCT_CATEGORY, paymentInfo.getCategory().toString());
//                break;
//            case PAY:
//                intent.setAction("tv.ismar.pay.pay");
//                intent.putExtra("item_id", paymentInfo.getPk());
//                break;
//            case PAYVIP:
//                intent.setAction("tv.ismar.pay.payvip");
//                intent.putExtra("cpid", paymentInfo.getCpid());
//                intent.putExtra("item_id", paymentInfo.getPk());
//                break;
//            default:
//                throw new IllegalArgumentException();
//        }
//        context.startActivity(intent);
//    }

    @Override
    public void toPaymentForResult(Activity activity, String fromPage, PaymentInfo paymentInfo) {
        Intent intent = new Intent();
        switch (paymentInfo.getJumpTo()) {
            case PAYMENT:
                intent.setAction("tv.ismar.pay.payment");
                intent.putExtra(EXTRA_PK, paymentInfo.getPk());
                intent.putExtra(EXTRA_PRODUCT_CATEGORY, paymentInfo.getCategory().toString());
                break;
            case PAY:
                intent.setAction("tv.ismar.pay.pay");
                intent.putExtra("item_id", paymentInfo.getPk());
                break;
            case PAYVIP:
                intent.setAction("tv.ismar.pay.payvip");
                intent.putExtra("cpid", paymentInfo.getCpid());
                intent.putExtra("item_id", paymentInfo.getPk());
                intent.putExtra("title", paymentInfo.getTitle());
                intent.putExtra("source", fromPage);
                break;
            default:
                throw new IllegalArgumentException();
        }
        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }


    public void toPlayPage(Context context, int pk, int sub_item_pk, Source source) {
        Log.i("toPlayPage","startpalyer");
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, sub_item_pk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source.getValue());
        context.startActivity(intent);
    }
    public void toPlayPage(Context context, int pk, int sub_item_pk, Source source,Source root) {
        Log.i("toPlayPage","startpalyer");
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, sub_item_pk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source.getValue());
        intent.putExtra(PageIntentInterface.EXTRA_TO, root.getValue());
        context.startActivity(intent);
    }
    @Override
    public void toPlayPageEpisode(Context context, int pk, int sub_item_pk, Source source, String contentMode) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Player");
        intent.putExtra(PageIntentInterface.EXTRA_PK, pk);
        intent.putExtra(PageIntentInterface.EXTRA_SUBITEM_PK, sub_item_pk);
        intent.putExtra(PageIntentInterface.EXTRA_SOURCE, source.getValue());
        intent.putExtra("contentMode",contentMode);
        context.startActivity(intent);
    }

    @Override
    public void toUserCenter(Context context) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.usercenter");
        context.startActivity(intent);
    }

    @Override
    public void toUserCenterLocation(Context context) {

    }

    @Override
    public void toPackageList(Context context, String source, int pk) {
        Intent intent=new Intent();
        intent.setAction("tv.ismar.daisy.packagelist");
        intent.putExtra("pk",pk);
        context.startActivity(intent);
    }

    public void toHistory(Context context,String frompage) {
        Intent intent = new Intent();
//        intent.setAction("tv.ismar.daisy.Channel");
//        intent.putExtra("channel", "histories");
        intent.setAction("tv.ismar.daisy.historyfavorite");
        intent.putExtra("fromPage",frompage);
        context.startActivity(intent);
    }

    public void toFavorite(Context context) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Channel");
        intent.putExtra("channel", "$bookmarks");
        context.startActivity(intent);
    }

    public void toSearch(Context context) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.searchpage.search");
        intent.putExtra("frompage","search");
        context.startActivity(intent);

    }

    @Override
    public void toFilmStar(Context context, String title, long pk) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.searchpage.filmstar");
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_PK, pk);
        context.startActivity(intent);
    }

    @Override
    public void toEpisodePage(Context context, String source, String itemJson) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.episode");
        intent.putExtra(EXTRA_ITEM_JSON, itemJson);
        intent.putExtra(EXTRA_SOURCE, source);
        context.startActivity(intent);
    }

    @Override
    public void toHelpPage(Context context) {
        Intent intent = new Intent();
        try {
            intent.setAction("cn.ismartv.speedtester.feedback");
            context.startActivity(intent);
        }catch (ActivityNotFoundException e) {
            intent.setAction("cn.ismar.sakura.launcher");
            context.startActivity(intent);
       }
    }

    @Override
    public void toSubject(Context context, String gather_type, int id, String title, String frompage,String channel) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.subject");
        intent.putExtra("gather_type", gather_type);
        intent.putExtra("itemid", id);
        intent.putExtra("itemtitle", title);
        intent.putExtra("fromPage", frompage);
        intent.putExtra("channel",channel);
        context.startActivity(intent);
    }

    @Override
    public void toPlayFinish(Fragment fragment, String channel, int id, int playScale, boolean hasHistory, String frompage) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.PlayFinished");
        intent.putExtra("channel",channel);
        intent.putExtra("item_id",id);
        intent.putExtra("play_scale",playScale);
        intent.putExtra("has_history",hasHistory);
        intent.putExtra("frompage",frompage);
        fragment.startActivityForResult(intent,0);
    }
    @Override
    public void toPlayFinish(Fragment fragment, String channel, int id, int playScale, boolean hasHistory, String frompage, String root) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.PlayFinished");
        intent.putExtra("channel",channel);
        intent.putExtra("item_id",id);
        intent.putExtra("play_scale",playScale);
        intent.putExtra("has_history",hasHistory);
        intent.putExtra("frompage",frompage);
        intent.putExtra(EXTRA_TO,root);
        fragment.startActivityForResult(intent,0);
    }


    @Override
    public void toListPage(Context context,String title, String channel, int style,String slug) {
        Intent intent = new Intent();
        intent.setAction("tv.ismar.daisy.Filter");
        intent.putExtra("title",title);
        intent.putExtra("channel",channel);
        intent.putExtra("style",style);
        intent.putExtra("section",slug);
        context.startActivity(intent);
    }
}

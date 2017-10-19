package tv.ismar.app.core;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by huibin on 9/8/16.
 */
public interface PageIntentInterface {
    String EXTRA_MODEL = "content_model";
    String EXTRA_PK = "pk";
    String EXTRA_ITEM_JSON = "item_json";
    // 电视剧等多集片子集pk,与文档相同
    String EXTRA_SUBITEM_PK = "sub_item_pk";
    String EXTRA_SOURCE = "fromPage";
    String EXTRA_TO = "to";
    String EXTRA_TYPE = "type";
    String EXTRA_TITLE= "title";
    String EXTRA_START_TIME= "time";
    String POSITION= "position";
    String TYPE= "type";
    String QIYIFLAG= "isqiyi";

    String EXTRA_PRODUCT_CATEGORY = "product_category";

    int PAYMENT_REQUEST_CODE = 0xd6;
    int PAYMENT_SUCCESS_CODE = 0x5c;
    int PAYMENT_FAILURE_CODE = 0xd2;


    int DETAIL_TYPE_PKG = 0x8a;
    int DETAIL_TYPE_ITEM = 0x37;

    int PAYMENT = 1;
    int PAY = 0;
    int PAYVIP = 2;


    void toDetailPage(Context context, String source, int pk);


    void toDetailPage(Context context, String source, String root, int pk);

    void toDetailPage(Context context, String source, String json);

    void toPackageDetail(Context context, String source, int pk);


    void toPackageDetail(Context context, String source, String json);

//    void toPayment(Context context, String fromPage, PaymentInfo paymentInfo);

    void toPaymentForResult(Activity context, String fromPage, PaymentInfo paymentInfo);

    void toPlayPage(Context context, int pk, int sub_item_pk, Source source);

    void toPlayPageEpisode(Context context, int pk, int sub_item_pk, Source source,String contentMode);
    void toUserCenter(Context context);

    void toUserCenterLocation(Context context);


    void toPackageList(Context context, String source, int pk);

    void toFilmStar(Context context, String title, long pk);

    void toEpisodePage(Context context, String source, String itemJson);

    void toHelpPage(Context context);

    void toSubject(Context context, String gather_type, int id, String title, String frompage,String channel);


    void toPlayFinish(Fragment fragment, String channel, int id, int playScale, boolean hasHistory, String frompage);

    void toPlayFinish(Fragment fragment, String channel, int id, int playScale, boolean hasHistory, String frompage, String root);


    void toListPage(Context context, String title, String channel, int style, String slug);

    class PaymentInfo {
        private ProductCategory category;
        private int pk;
        private int jumpTo;
        private int cpid;
        private String title;

        public PaymentInfo(ProductCategory category, int pk, int jumpTo, int cpid) {
            this.category = category;
            this.pk = pk;
            this.jumpTo = jumpTo;
            this.cpid = cpid;
        }
        public PaymentInfo(ProductCategory category, int pk, int jumpTo, int cpid,String title) {
            this.category = category;
            this.pk = pk;
            this.jumpTo = jumpTo;
            this.cpid = cpid;
            this.title=title;
        }
        public PaymentInfo(ProductCategory category, int pk, int jumpTo) {
            this.category = category;
            this.pk = pk;
            this.jumpTo = jumpTo;
        }

        public PaymentInfo(int pk, int jumpTo, int cpid,String title) {
            this.pk = pk;
            this.jumpTo = jumpTo;
            this.cpid = cpid;
            this.title=title;
        }

        public ProductCategory getCategory() {
            return category;
        }

        public int getPk() {
            return pk;
        }

        public int getJumpTo() {
            return jumpTo;
        }

        public int getCpid() {
            return cpid;
        }

        public String getTitle() {
            return title;
        }
    }


    enum FromPage {
        unknown
    }

    enum ProductCategory {
        item,
        Package,
        charge,
        subitem;

        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    }

}

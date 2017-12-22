package tv.ismar.app.network.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huibin on 10/11/2017.
 */

public class ExtraPaymentChannelEntity {

    private List<PaymentsBean> payments;

    public List<PaymentsBean> getPayments() {
//        List<PaymentsBean> list = new ArrayList<>();
//        for (int i = 0; i < 5; i++){
//            list.add(payments.get(0));
//        }
//        return list;
        return payments;
    }

    public void setPayments(List<PaymentsBean> payments) {
        this.payments = payments;
    }

    public static class PaymentsBean {
        /**
         * descriptions : ["银视通"]
         * source : chinatvpay
         * title : 银联支付
         * url : /api/paymentwayinfo/chinatvpay/item/1976933/
         */

        private String source;
        private String title;
        private String url;
        private List<String> descriptions;

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public List<String> getDescriptions() {
            return descriptions;
        }

        public void setDescriptions(List<String> descriptions) {
            this.descriptions = descriptions;
        }
    }
}

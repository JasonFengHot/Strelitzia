package tv.ismar.subject.Utils;
import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;

import okhttp3.ResponseBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import tv.ismar.account.IsmartvActivator;
import tv.ismar.app.BaseActivity;
import tv.ismar.app.core.PageIntent;
import tv.ismar.app.core.PageIntentInterface;
import tv.ismar.app.entity.Objects;
import tv.ismar.app.network.SkyService;
import tv.ismar.app.network.entity.PlayCheckEntity;
import tv.ismar.app.util.Utils;
import tv.ismar.statistics.PurchaseStatistics;

import static tv.ismar.app.core.PageIntentInterface.FromPage.unknown;
import static tv.ismar.app.core.PageIntentInterface.ProductCategory.item;

/**
 * Created by liucan on 2017/3/30.
 */

public class PayCheckUtil {
    private PlayCheckEntity playCheckEntity=null;

    public PlayCheckEntity calculateRemainDay(String info) {
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
                    remainDay = 0;
                }
                playCheckEntity.setRemainDay(remainDay);
                break;
        }
        return playCheckEntity;
    }
    public void handlePurchase(Activity context, Objects objects) {
        int pk = objects.pk;
        int jumpTo = objects.expense.jump_to;
        int cpid = objects.expense.cpid;
        PageIntentInterface.PaymentInfo paymentInfo = new PageIntentInterface.PaymentInfo(item, pk, jumpTo, cpid);

        String userName = IsmartvActivator.getInstance().getUsername();
        String title = objects.title;

        String clip = "";
//        if (objects.getClip() != null) {
//            clip = String.valueOf(objects.getClip().getPk());
//        }
        new PurchaseStatistics().expenseVideoClick(String.valueOf(pk), userName, title, clip);
        new PageIntent().toPaymentForResult(context, unknown.name(), paymentInfo);
    }
}

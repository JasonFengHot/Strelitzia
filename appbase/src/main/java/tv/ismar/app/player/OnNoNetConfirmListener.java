package tv.ismar.app.player;

/**
 * Created by longhai on 17-3-14.
 *
 * 播放器，断开网络，弹出设置网络后，返回，需要重新加载数据。电视机设置弹出Activity样式不一样
 */

public interface OnNoNetConfirmListener {

    void onNoNetConfirm();
}

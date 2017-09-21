package tv.ismar.app.database;

import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huibin on 21/09/2017.
 */

@Table(name = "banner_icon_mark", id = "_id")
public class BannerIconMarkTable extends Model{
    @Column
    public String pk;
    @Column
    public String image;
    @Column
    public String name;
}

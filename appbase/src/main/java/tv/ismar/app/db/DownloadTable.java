package tv.ismar.app.db;


import cn.ismartv.injectdb.library.Model;
import cn.ismartv.injectdb.library.annotation.Column;
import cn.ismartv.injectdb.library.annotation.Table;

/**
 * Created by huaijie on 6/24/15.
 */

@Table(name = "download", id = "_id")
public class DownloadTable extends Model {
    public static final String DOWNLOAD_PATH = "download_path";
    public static final String DOWNLOAD_STATE = "download_state";
    public static final String START_POSITION = "start_position";
    public static final String CONTENT_LENGTH = "content_length";

    @Column
    public String file_name;

    @Column
    public String url;

    @Column(unique = true, onUniqueConflict = Column.ConflictAction.IGNORE)
    public String download_path;

    @Column
    public long start_position;

    @Column
    public long content_length;

    @Column
    public String local_md5;

    @Column
    public String server_md5;

    @Column
    public String download_state;
}

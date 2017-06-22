package tv.ismar.library.downloader.model;

import com.google.gson.annotations.SerializedName;

import tv.ismar.library.injectdb.Model;
import tv.ismar.library.injectdb.annotation.Column;
import tv.ismar.library.injectdb.annotation.Table;

/**
 * Created by LongHai on 17-4-11.
 */

@Table(name = "file_download", id = "_id")
public class DownloadTable extends Model {

    public static final String DOWNLOAD_PATH = "download_path";

    @Column
    @SerializedName("hello")
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
